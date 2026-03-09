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
import com.securevision.ml.common.BoundingBox
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
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    val detections: List<BoundingBox> = emptyList(),
    val fps: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class LiveViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var frameTimestamps = ArrayDeque<Long>(30)

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
                    if (_uiState.value.isRunning) {
                        processFrame()
                    }
                    imageProxy.close()
                }
            }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
            _uiState.update { it.copy(isLoading = false, isRunning = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun processFrame() {
        val now = System.currentTimeMillis()
        frameTimestamps.addLast(now)
        if (frameTimestamps.size > 30) frameTimestamps.removeFirst()

        val fps = if (frameTimestamps.size >= 2) {
            val duration = frameTimestamps.last() - frameTimestamps.first()
            if (duration > 0) (frameTimestamps.size - 1) * 1000f / duration else 0f
        } else 0f

        viewModelScope.launch {
            _uiState.update { it.copy(fps = fps) }
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
        _uiState.update { it.copy(cameraSelector = newSelector, detections = emptyList()) }
        cameraProvider?.unbindAll()
    }

    fun stopDetection() {
        cameraProvider?.unbindAll()
        _uiState.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}
