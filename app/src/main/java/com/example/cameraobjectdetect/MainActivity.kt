
package com.example.cameraobjectdetect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.cameraobjectdetect.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private lateinit var detector: ObjectDetectorHelper
    private val tracker = SimpleTracker()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) startCamera() else finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        detector = ObjectDetectorHelper(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnToggleCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        binding.btnZoomIn.setOnClickListener { adjustZoom(1.15f) }
        binding.btnZoomOut.setOnClickListener { adjustZoom(1f / 1.15f) }
    }

    private fun adjustZoom(multiplier: Float) {
        val cam = camera ?: return
        val current = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
        val minZ = cam.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
        val maxZ = cam.cameraInfo.zoomState.value?.maxZoomRatio ?: 5f
        val newRatio = (current * multiplier).coerceIn(minZ, maxZ)
        cam.cameraControl.setZoomRatio(newRatio)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetResolution(Size(1280, 720))
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                analyzeFrame(imageProxy)
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, analysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeFrame(image: ImageProxy) {
        try {
            val bitmap = ImageUtils.imageProxyToBitmap(image)
            val results = detector.detect(bitmap)
            val movingDetections = tracker.update(results)

            binding.overlay.post {
                binding.overlay.updateDetections(movingDetections, bitmap.width, bitmap.height)
                binding.txtDetections.text = buildString {
                    append("frame: ${bitmap.width}x${bitmap.height}\\n")
                    movingDetections.forEach { det ->
                        val box = det.boundingBox
                        append("#${det.trackingId} ${det.category} ${"%.2f".format(det.score)} moving=${det.isMoving} -> ")
                        append("[x=${box.left.toInt()}, y=${box.top.toInt()}, w=${box.width().toInt()}, h=${box.height().toInt()}]\\n")
                    }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            image.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector.close()
    }
}
