package com.securevision.ml.face

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.Classifier
import com.securevision.ml.common.Detection
import com.securevision.ml.common.DetectionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Face detector backed by the ML Kit Face Detection API.
 * Detects faces in a [Bitmap], returning normalised [BoundingBox] instances along with
 * optional landmark & classification metadata (smiling probability, eye-open probability, etc.).
 */
class FaceDetector(
    override val confidenceThreshold: Float = 0.7f,
    private val enableLandmarks: Boolean = true,
    private val enableClassifications: Boolean = true
) : Classifier {

    private var detector: com.google.mlkit.vision.face.FaceDetector? = null
    override var isInitialized: Boolean = false
        private set

    override suspend fun initialize() {
        if (isInitialized) return

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(
                if (enableLandmarks) FaceDetectorOptions.LANDMARK_MODE_ALL
                else FaceDetectorOptions.LANDMARK_MODE_NONE
            )
            .setClassificationMode(
                if (enableClassifications) FaceDetectorOptions.CLASSIFICATION_MODE_ALL
                else FaceDetectorOptions.CLASSIFICATION_MODE_NONE
            )
            .setMinFaceSize(0.1f)
            .enableTracking()
            .build()

        detector = FaceDetection.getClient(options)
        isInitialized = true
    }

    override suspend fun classify(bitmap: Bitmap, rotation: Int): DetectionResult {
        if (!isInitialized || detector == null) return DetectionResult.NotInitialized

        val inputImage = InputImage.fromBitmap(bitmap, rotation)
        return suspendCancellableCoroutine { continuation ->
            detector!!.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        continuation.resume(DetectionResult.Empty)
                    } else {
                        val detections = faces.mapNotNull { face ->
                            face.toDetection(bitmap.width, bitmap.height, confidenceThreshold)
                        }
                        continuation.resume(
                            if (detections.isEmpty()) DetectionResult.Empty
                            else DetectionResult.Success(detections)
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(
                        DetectionResult.Error(
                            message = exception.message ?: "Face detection failed",
                            cause = exception
                        )
                    )
                }
        }
    }

    override fun close() {
        detector?.close()
        detector = null
        isInitialized = false
    }

    private fun Face.toDetection(
        imageWidth: Int,
        imageHeight: Int,
        threshold: Float
    ): Detection? {
        val rect = boundingBox
        val box = BoundingBox.fromAbsolute(
            leftPx = rect.left.toFloat(),
            topPx = rect.top.toFloat(),
            rightPx = rect.right.toFloat(),
            bottomPx = rect.bottom.toFloat(),
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )

        if (!box.isValid()) return null

        val metadata = buildMap<String, String> {
            trackingId?.let { put("trackingId", it.toString()) }
            headEulerAngleX.let { put("headPitch", String.format("%.2f", it)) }
            headEulerAngleY.let { put("headYaw", String.format("%.2f", it)) }
            headEulerAngleZ.let { put("headRoll", String.format("%.2f", it)) }
            smilingProbability?.let { prob ->
                if (prob >= 0f) put("smilingProbability", String.format("%.2f", prob))
            }
            leftEyeOpenProbability?.let { prob ->
                if (prob >= 0f) put("leftEyeOpenProbability", String.format("%.2f", prob))
            }
            rightEyeOpenProbability?.let { prob ->
                if (prob >= 0f) put("rightEyeOpenProbability", String.format("%.2f", prob))
            }
        }

        return Detection(
            label = "Face",
            confidence = 1.0f, // ML Kit face detection does not return a per-face confidence score
            boundingBox = box,
            metadata = metadata
        )
    }
}
