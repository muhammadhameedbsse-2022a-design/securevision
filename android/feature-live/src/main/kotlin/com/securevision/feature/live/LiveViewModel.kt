package com.securevision.feature.live

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevision.core.domain.model.BoundingBoxDomain
import com.securevision.core.domain.model.DetectionEvent
import com.securevision.core.domain.model.DetectionType
import com.securevision.core.domain.repository.DetectionRepository
import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.DetectionResult
import com.securevision.ml.face.FaceDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class LiveUiState(
    val isLoading: Boolean = true,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    val detections: List<BoundingBox> = emptyList(),
    val fps: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class LiveViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var frameTimestamps = ArrayDeque<Long>(30)

    /** Tracks whether detection was active before a lifecycle pause. */
    private var wasRunningBeforePause = false

    /** Guards against duplicate [bindCameraUseCases] calls. */
    private var isCameraBound = false

    private val faceDetector = FaceDetector(confidenceThreshold = 0.7f)
    private val isProcessing = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            faceDetector.initialize()
        }
    }

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(lifecycleOwner, previewView)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return
        val selector = _uiState.value.cameraSelector

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (_uiState.value.isRunning && !_uiState.value.isPaused &&
                        isProcessing.compareAndSet(false, true)
                    ) {
                        analyzeFrame(imageProxy)
                    } else {
                        imageProxy.close()
                    }
                }
            }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
            isCameraBound = true
            _uiState.update { it.copy(isLoading = false, isRunning = true) }
        } catch (e: Exception) {
            isCameraBound = false
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun analyzeFrame(imageProxy: ImageProxy) {
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                val bitmap = imageProxy.toBitmap()
                if (bitmap != null) {
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val result = faceDetector.classify(bitmap, rotation)

                    when (result) {
                        is DetectionResult.Success -> {
                            val filtered = result.detections
                                .filter { it.confidence >= faceDetector.confidenceThreshold }
                            val boxes = filtered.mapNotNull { it.boundingBox }
                            val processingTime = System.currentTimeMillis() - startTime

                            updateFps()
                            _uiState.update { it.copy(detections = boxes) }

                            // Persist detection events to Room
                            for (detection in filtered) {
                                val event = DetectionEvent(
                                    timestamp = System.currentTimeMillis(),
                                    cameraId = getCameraId(),
                                    detectionType = DetectionType.FACE_UNKNOWN,
                                    confidence = detection.confidence,
                                    boundingBox = detection.boundingBox?.let { box ->
                                        BoundingBoxDomain(
                                            left = box.left,
                                            top = box.top,
                                            right = box.right,
                                            bottom = box.bottom
                                        )
                                    },
                                    label = detection.label,
                                    processingTimeMs = processingTime,
                                    metadata = detection.metadata
                                )
                                detectionRepository.insertDetectionEvent(event)
                            }
                        }
                        is DetectionResult.Empty -> {
                            updateFps()
                            _uiState.update { it.copy(detections = emptyList()) }
                        }
                        is DetectionResult.Error -> {
                            updateFps()
                            _uiState.update { it.copy(detections = emptyList()) }
                        }
                        is DetectionResult.NotInitialized -> {
                            // Detector not ready yet; skip frame
                        }
                    }

                    if (!bitmap.isRecycled) bitmap.recycle()
                } else {
                    updateFps()
                }
            } finally {
                imageProxy.close()
                isProcessing.set(false)
            }
        }
    }

    // ImageProxy.toBitmap() via YUV→NV21→JPEG conversion.
    // Uses YuvImage which is marked deprecated but remains the simplest
    // way to convert CameraX YUV_420_888 frames without a third-party library.
    @Suppress("DEPRECATION")
    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 50, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun updateFps() {
        val now = System.currentTimeMillis()
        frameTimestamps.addLast(now)
        if (frameTimestamps.size > 30) frameTimestamps.removeFirst()

        val fps = if (frameTimestamps.size >= 2) {
            val duration = frameTimestamps.last() - frameTimestamps.first()
            if (duration > 0) (frameTimestamps.size - 1) * 1000f / duration else 0f
        } else 0f

        _uiState.update { it.copy(fps = fps) }
    }

    private fun getCameraId(): String {
        return if (_uiState.value.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            "back"
        } else {
            "front"
        }
    }

    fun toggleDetection() {
        _uiState.update { it.copy(isRunning = !it.isRunning) }
    }

    fun flipCamera() {
        val newSelector = if (_uiState.value.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        cameraProvider?.unbindAll()
        isCameraBound = false
        _uiState.update { it.copy(cameraSelector = newSelector, detections = emptyList(), isLoading = true) }
    }

    /** Called when the host lifecycle moves to ON_PAUSE. */
    fun onLifecyclePause() {
        wasRunningBeforePause = _uiState.value.isRunning
        _uiState.update { it.copy(isPaused = true) }
    }

    /** Called when the host lifecycle moves to ON_RESUME. */
    fun onLifecycleResume() {
        _uiState.update {
            it.copy(
                isPaused = false,
                isRunning = if (wasRunningBeforePause) true else it.isRunning
            )
        }
    }

    fun stopDetection() {
        cameraProvider?.unbindAll()
        isCameraBound = false
        _uiState.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
        faceDetector.close()
    }
}
