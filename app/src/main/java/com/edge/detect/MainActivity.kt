package com.edge.detect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.edge.detect.gl.GLRenderer
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var glSurfaceView: android.opengl.GLSurfaceView
    private lateinit var toggleButton: Button
    private lateinit var fpsText: TextView

    private lateinit var cameraExecutor: ExecutorService
    private var showEdges = true
    private lateinit var renderer: GLRenderer

    external fun nativeInit()
    external fun nativeSetMode(edge: Boolean)
    external fun nativeOnFrameRGBA(data: ByteArray, width: Int, height: Int, stride: Int)

    companion object {
        init { System.loadLibrary("edgeproc") }
        private const val TAG = "EdgeDetect"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) startCamera() else Log.e(TAG, "Camera permission denied")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        glSurfaceView = findViewById(R.id.glSurfaceView)
        toggleButton = findViewById(R.id.toggleButton)
        fpsText = findViewById(R.id.fpsText)

        glSurfaceView.preserveEGLContextOnPause = true
        glSurfaceView.setEGLContextClientVersion(2)
        renderer = GLRenderer()
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY

        nativeInit()

        toggleButton.setOnClickListener {
            showEdges = !showEdges
            nativeSetMode(showEdges)
            toggleButton.text = if (showEdges) "Edges" else "Raw"
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            var lastTime = System.nanoTime()
            var frames = 0

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val plane = imageProxy.planes[0]
                val buf: ByteBuffer = plane.buffer
                val data = ByteArray(buf.remaining())
                buf.get(data)
                nativeOnFrameRGBA(data, imageProxy.width, imageProxy.height, plane.rowStride)
                // Update GL texture with processed data
                renderer.updateFrame(data, imageProxy.width, imageProxy.height, plane.rowStride)
                frames++
                val now = System.nanoTime()
                if ((now - lastTime) > 1_000_000_000) {
                    val fps = frames
                    frames = 0
                    lastTime = now
                    runOnUiThread { fpsText.text = "FPS: $fps" }
                }
                imageProxy.close()
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
