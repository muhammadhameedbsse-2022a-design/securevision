package com.securevision.feature.live.analyzer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.securevision.ml.attributes.AttributeClassifier
import com.securevision.ml.common.Detection
import com.securevision.ml.common.DetectionResult
import com.securevision.ml.face.FaceDetector
import com.securevision.ml.weapon.WeaponDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Orchestrates the ML detection pipeline:
 * 1. Face detection on every frame
 * 2. Weapon detection every [weaponFrameInterval] frames
 * 3. Attribute classification on detected face crops
 */
class FrameAnalyzer(
    private val faceDetector: FaceDetector,
    private val weaponDetector: WeaponDetector,
    private val attributeClassifier: AttributeClassifier,
    private val weaponFrameInterval: Int = 3
) {
    private var frameCount = 0L

    suspend fun initialize() {
        faceDetector.initialize()
        weaponDetector.initialize()
        attributeClassifier.initialize()
    }

    /**
     * Analyse a single camera frame. Returns all detections for the frame.
     */
    suspend fun analyze(imageProxy: ImageProxy): List<Detection> = withContext(Dispatchers.Default) {
        val bitmap = imageProxy.toBitmap420() ?: return@withContext emptyList()
        val rotation = imageProxy.imageInfo.rotationDegrees
        frameCount++

        val faceDeferred = async { faceDetector.classify(bitmap, rotation) }
        val weaponDeferred = if (frameCount % weaponFrameInterval == 0L) {
            async { weaponDetector.classify(bitmap, rotation) }
        } else null

        val results = mutableListOf<Detection>()

        when (val faceResult = faceDeferred.await()) {
            is DetectionResult.Success -> {
                for (face in faceResult.detections) {
                    val attrs = runAttributes(bitmap, face, rotation)
                    val enrichedLabel = buildString {
                        append(face.label)
                        if (attrs.isNotEmpty()) {
                            append(" (")
                            append(attrs.entries.joinToString(", ") { "${it.key}: ${it.value}" })
                            append(")")
                        }
                    }
                    results += face.copy(
                        label = enrichedLabel,
                        metadata = face.metadata + attrs
                    )
                }
            }
            else -> { /* empty, error, not-init – skip */ }
        }

        weaponDeferred?.let { deferred ->
            when (val weaponResult = deferred.await()) {
                is DetectionResult.Success -> results += weaponResult.detections
                else -> {}
            }
        }

        if (!bitmap.isRecycled) bitmap.recycle()
        results
    }

    /**
     * Crop the face region and run attribute classification on it.
     */
    private suspend fun runAttributes(
        bitmap: Bitmap,
        face: Detection,
        rotation: Int
    ): Map<String, String> {
        val box = face.boundingBox ?: return emptyMap()
        val x = (box.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val y = (box.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val w = (box.width * bitmap.width).toInt().coerceIn(1, bitmap.width - x)
        val h = (box.height * bitmap.height).toInt().coerceIn(1, bitmap.height - y)

        val crop = Bitmap.createBitmap(bitmap, x, y, w, h)
        return try {
            when (val result = attributeClassifier.classify(crop, rotation)) {
                is DetectionResult.Success -> result.detections.firstOrNull()?.metadata ?: emptyMap()
                else -> emptyMap()
            }
        } finally {
            if (!crop.isRecycled && crop != bitmap) crop.recycle()
        }
    }

    fun close() {
        faceDetector.close()
        weaponDetector.close()
        attributeClassifier.close()
    }
}

/**
 * Convert an ImageProxy (YUV_420_888) to an ARGB [Bitmap], downscaled for performance.
 */
private fun ImageProxy.toBitmap420(): Bitmap? {
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
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    return downscaleBitmap(original, MAX_ANALYSIS_DIM)
}

private fun downscaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= maxDim && h <= maxDim) return bitmap
    val scale = maxDim.toFloat() / maxOf(w, h)
    val newW = (w * scale).toInt().coerceAtLeast(1)
    val newH = (h * scale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    if (scaled != bitmap) bitmap.recycle()
    return scaled
}

private const val MAX_ANALYSIS_DIM = 480
