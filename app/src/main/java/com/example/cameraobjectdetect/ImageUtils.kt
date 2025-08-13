
package com.example.cameraobjectdetect

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

object ImageUtils {
    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        require(image.format == ImageFormat.FLEX_RGBA_8888 || image.format == ImageFormat.RGBA_8888) {
            "Use ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888"
        }
        val buffer = image.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var bmp = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(bytes))

        val rotation = image.imageInfo.rotationDegrees
        if (rotation != 0) {
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        }
        return bmp
    }
}
