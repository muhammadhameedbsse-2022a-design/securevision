package com.securevision.ml.common

/**
 * Normalised bounding box with coordinates in [0, 1] range relative to the image dimensions.
 */
data class BoundingBox(
    /** Left edge as a fraction of image width. */
    val left: Float,
    /** Top edge as a fraction of image height. */
    val top: Float,
    /** Right edge as a fraction of image width. */
    val right: Float,
    /** Bottom edge as a fraction of image height. */
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f
    val area: Float get() = width * height

    fun isValid(): Boolean =
        left >= 0f && top >= 0f && right <= 1f && bottom <= 1f && width > 0f && height > 0f

    companion object {
        /**
         * Creates a BoundingBox from absolute pixel coordinates, normalising against the image size.
         */
        fun fromAbsolute(
            leftPx: Float,
            topPx: Float,
            rightPx: Float,
            bottomPx: Float,
            imageWidth: Int,
            imageHeight: Int
        ): BoundingBox = BoundingBox(
            left = leftPx / imageWidth,
            top = topPx / imageHeight,
            right = rightPx / imageWidth,
            bottom = bottomPx / imageHeight
        )
    }
}
