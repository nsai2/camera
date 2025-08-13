
package com.example.cameraobjectdetect

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintBox = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.GREEN
        isAntiAlias = true
    }
    private val paintText = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
    }

    private var dets: List<com.example.cameraobjectdetect.SimpleTracker.Tracked> = emptyList()
    private var frameW: Int = 0
    private var frameH: Int = 0

    fun updateDetections(detections: List<com.example.cameraobjectdetect.SimpleTracker.Tracked>, frameWidth: Int, frameHeight: Int) {
        this.frameW = frameWidth
        this.frameH = frameHeight
        this.dets = detections
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frameW == 0 || frameH == 0) return

        val scale = maxOf(width / frameW.toFloat(), height / frameH.toFloat())
        val xOffset = (width - frameW * scale) / 2f
        val yOffset = (height - frameH * scale) / 2f

        for (d in dets) {
            val r = RectF(
                xOffset + d.boundingBox.left * scale,
                yOffset + d.boundingBox.top * scale,
                xOffset + d.boundingBox.right * scale,
                yOffset + d.boundingBox.bottom * scale
            )
            paintBox.color = if (d.isMoving) Color.CYAN else Color.GREEN
            canvas.drawRect(r, paintBox)

            val label = "#${d.trackingId} ${d.category} ${(d.score * 100).toInt()}%" + if (d.isMoving) " •moving" else ""
            canvas.drawText(label, r.left + 8f, (r.top - 8f).coerceAtLeast(24f), paintText)
        }
    }
}
