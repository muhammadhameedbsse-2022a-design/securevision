package com.securevision.feature.live

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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
import com.securevision.core.domain.usecase.SaveDetectionEventUseCase
import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.DetectionResult
import com.securevision.ml.face.FaceDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
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
    private val saveDetectionEventUseCase: SaveDetectionEventUseCase
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

    private val frameAnalyzer = FaceFrameAnalyzer(
        faceDetector = faceDetector,
        coroutineScope = viewModelScope,
        onResult = ::handleDetectionResult
    )

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
                analysis.setAnalyzer(cameraExecutor, frameAnalyzer)
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

    /**
     * Callback invoked by [FaceFrameAnalyzer] with each detection result.
     * Applies quality filtering via [DetectionMapper], updates UI state and
     * persists detection events to Room.
     */
    private fun handleDetectionResult(result: DetectionResult) {
        val startTime = System.currentTimeMillis()

        when (result) {
            is DetectionResult.Success -> {
                val filtered = DetectionMapper.filterByQuality(
                    detections = result.detections,
                    minConfidence = faceDetector.confidenceThreshold
                )
                val boxes = DetectionMapper.toBoundingBoxes(filtered)
                val processingTime = System.currentTimeMillis() - startTime

                updateFps()
                _uiState.update { it.copy(detections = boxes) }

                // Persist detection events to Room
                viewModelScope.launch {
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
                        saveDetectionEventUseCase(event)
                    }
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
        val newRunning = !_uiState.value.isRunning
        _uiState.update { it.copy(isRunning = newRunning) }
        syncAnalyzerEnabled()
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
        syncAnalyzerEnabled()
    }

    /** Called when the host lifecycle moves to ON_RESUME. */
    fun onLifecycleResume() {
        _uiState.update {
            it.copy(
                isPaused = false,
                isRunning = wasRunningBeforePause || it.isRunning
            )
        }
        syncAnalyzerEnabled()
    }

    fun stopDetection() {
        cameraProvider?.unbindAll()
        isCameraBound = false
        _uiState.update { it.copy(isRunning = false) }
        syncAnalyzerEnabled()
    }

    /** Keeps [FaceFrameAnalyzer.isEnabled] in sync with the current UI state. */
    private fun syncAnalyzerEnabled() {
        val state = _uiState.value
        frameAnalyzer.isEnabled = state.isRunning && !state.isPaused
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
        faceDetector.close()
    }
}
