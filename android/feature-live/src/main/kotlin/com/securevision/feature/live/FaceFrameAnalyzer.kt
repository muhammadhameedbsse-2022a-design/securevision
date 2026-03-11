package com.securevision.feature.live

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.securevision.ml.common.DetectionResult
import com.securevision.ml.face.FaceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [ImageAnalysis.Analyzer] that converts each CameraX frame to a [Bitmap],
 * runs [FaceDetector] inference, and delivers the [DetectionResult] via [onResult].
 *
 * Processing is guarded by an [AtomicBoolean] so that at most one frame is in
 * the ML pipeline at any time. Frames arriving while a previous frame is still
 * being processed are silently dropped (back-pressure).
 */
class FaceFrameAnalyzer(
    private val faceDetector: FaceDetector,
    private val coroutineScope: CoroutineScope,
    private val onResult: (DetectionResult) -> Unit
) : ImageAnalysis.Analyzer {

    /** When `false`, every frame is immediately closed without processing. */
    @Volatile
    var isEnabled: Boolean = true

    private val isProcessing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        if (!isEnabled || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        coroutineScope.launch {
            try {
                val bitmap = imageProxy.toBitmap()
                if (bitmap != null) {
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    val result = faceDetector.classify(bitmap, rotation)
                    onResult(result)
                    if (!bitmap.isRecycled) bitmap.recycle()
                } else {
                    onResult(DetectionResult.Empty)
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
}
