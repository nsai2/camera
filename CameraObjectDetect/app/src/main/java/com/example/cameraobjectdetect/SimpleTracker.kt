
package com.example.cameraobjectdetect

import android.graphics.RectF
import kotlin.math.hypot

class SimpleTracker {
    data class Tracked(
        val trackingId: Int,
        val category: String,
        val score: Float,
        val boundingBox: RectF,
        val isMoving: Boolean
    )

    private data class TrackState(
        val id: Int,
        var box: RectF,
        var lastCx: Float,
        var lastCy: Float
    )

    private val tracks = mutableListOf<TrackState>()
    private var nextId = 1

    private val iouThreshold = 0.3f
    private val moveThresholdPx = 6f

    fun update(dets: List<ObjectDetectorHelper.SimpleDet>): List<Tracked> {
        val assigned = BooleanArray(dets.size)
        val newTracks = mutableListOf<TrackState>()
        val results = mutableListOf<Tracked>()

        for (track in tracks) {
            var bestIdx = -1
            var bestIou = 0f
            for ((i, det) in dets.withIndex()) {
                if (assigned[i]) continue
                val iou = iou(track.box, det.boundingBox)
                if (iou > bestIou) {
                    bestIou = iou; bestIdx = i
                }
            }
            if (bestIdx >= 0 && bestIou >= iouThreshold) {
                val det = dets[bestIdx]
                assigned[bestIdx] = true
                val cx = det.boundingBox.centerX()
                val cy = det.boundingBox.centerY()
                val dist = hypot(cx - track.lastCx, cy - track.lastCy)
                val moving = dist >= moveThresholdPx
                track.box = RectF(det.boundingBox)
                track.lastCx = cx; track.lastCy = cy
                newTracks.add(track)
                results.add(
                    Tracked(track.id, det.category, det.score, det.boundingBox, moving)
                )
            }
        }

        for ((i, det) in dets.withIndex()) {
            if (assigned[i]) continue
            val cx = det.boundingBox.centerX()
            val cy = det.boundingBox.centerY()
            val st = TrackState(nextId++, RectF(det.boundingBox), cx, cy)
            newTracks.add(st)
            results.add(Tracked(st.id, det.category, det.score, det.boundingBox, false))
        }

        tracks.clear(); tracks.addAll(newTracks)
        return results
    }

    private fun iou(a: RectF, b: RectF): Float {
        val interLeft = maxOf(a.left, b.left)
        val interTop = maxOf(a.top, b.top)
        val interRight = minOf(a.right, b.right)
        val interBottom = minOf(a.bottom, b.bottom)
        val interW = maxOf(0f, interRight - interLeft)
        val interH = maxOf(0f, interBottom - interTop)
        val interArea = interW * interH
        val union = a.width() * a.height() + b.width() * b.height() - interArea
        return if (union <= 0f) 0f else interArea / union
    }
}
