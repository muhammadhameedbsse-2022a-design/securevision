package com.securevision.ml.weapon

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.securevision.ml.common.BoundingBox
import com.securevision.ml.common.Classifier
import com.securevision.ml.common.Detection
import com.securevision.ml.common.DetectionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Weapon / dangerous-object detector built on top of ML Kit's Object Detection API.
 *
 * In a production deployment this would use a custom TFLite model fine-tuned on
 * weapon categories. The default ML Kit base model is used here to demonstrate
 * the pipeline; swap [ObjectDetectorOptions] for a custom-model variant when the
 * trained model asset is available.
 */
class WeaponDetector(
    override val confidenceThreshold: Float = 0.65f
) : Classifier {

    private var detector: ObjectDetector? = null
    override var isInitialized: Boolean = false
        private set

    // Labels considered as weapon-related from the base model's taxonomy.
    // A custom model would return explicit weapon labels instead.
    private val weaponCategories = setOf(
        "weapon", "gun", "pistol", "rifle", "knife", "blade",
        "firearm", "sharp object", "dangerous object", "hazardous item"
    )

    override suspend fun initialize() {
        if (isInitialized) return

        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        detector = ObjectDetection.getClient(options)
        isInitialized = true
    }

    override suspend fun classify(bitmap: Bitmap, rotation: Int): DetectionResult {
        if (!isInitialized || detector == null) return DetectionResult.NotInitialized

        val inputImage = InputImage.fromBitmap(bitmap, rotation)
        return suspendCancellableCoroutine { continuation ->
            detector!!.process(inputImage)
                .addOnSuccessListener { objects ->
                    val detections = objects.flatMap { detectedObject ->
                        val rect = detectedObject.boundingBox
                        val box = BoundingBox.fromAbsolute(
                            leftPx = rect.left.toFloat(),
                            topPx = rect.top.toFloat(),
                            rightPx = rect.right.toFloat(),
                            bottomPx = rect.bottom.toFloat(),
                            imageWidth = bitmap.width,
                            imageHeight = bitmap.height
                        )

                        detectedObject.labels
                            .filter { label ->
                                label.confidence >= confidenceThreshold &&
                                        (weaponCategories.any { cat ->
                                            label.text.lowercase().contains(cat)
                                        } || label.index == -1) // unknown object - include for review
                            }
                            .map { label ->
                                Detection(
                                    label = label.text.ifBlank { "Unknown Object" },
                                    confidence = label.confidence,
                                    boundingBox = box.takeIf { it.isValid() },
                                    metadata = mapOf(
                                        "trackingId" to (detectedObject.trackingId?.toString() ?: ""),
                                        "labelIndex" to label.index.toString()
                                    )
                                )
                            }
                    }

                    continuation.resume(
                        if (detections.isEmpty()) DetectionResult.Empty
                        else DetectionResult.Success(detections)
                    )
                }
                .addOnFailureListener { exception ->
                    continuation.resume(
                        DetectionResult.Error(
                            message = exception.message ?: "Weapon detection failed",
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
}
