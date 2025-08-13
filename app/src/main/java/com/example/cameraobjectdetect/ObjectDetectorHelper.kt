
package com.example.cameraobjectdetect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions

class ObjectDetectorHelper(context: Context) : AutoCloseable {
    private val detector: ObjectDetector

    init {
        val base = BaseOptions.builder()
            .setNumThreads(4)
            .build()

        val options = ObjectDetectorOptions.builder()
            .setBaseOptions(base)
            .setMaxResults(10)
            .setScoreThreshold(0.35f)
            .build()

        detector = ObjectDetector.createFromFileAndOptions(
            context,
            "efficientdet_lite0.tflite",
            options
        )
    }

    data class SimpleDet(
        val category: String,
        val score: Float,
        val boundingBox: RectF
    )

    fun detect(bitmap: Bitmap): List<SimpleDet> {
        val results: List<Detection> = detector.detect(bitmap)
        val out = mutableListOf<SimpleDet>()
        for (d in results) {
            val cat = d.categories().firstOrNull()
            if (cat != null) {
                val name = cat.categoryName
                if (name.equals("person", true) || name.equals("car", true) || name.equals("truck", true) ||
                    name.equals("bus", true) || name.equals("bicycle", true) || name.equals("motorcycle", true)) {
                    out.add(
                        SimpleDet(
                            category = name,
                            score = cat.score,
                            boundingBox = RectF(d.boundingBox())
                        )
                    )
                }
            }
        }
        return out
    }

    override fun close() { /* no-op */ }
}
