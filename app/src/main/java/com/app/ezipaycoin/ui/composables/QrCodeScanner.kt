package com.app.ezipaycoin.ui.composables

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrCodeScanner(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
    onResult: (String) -> Unit,
    onDispose: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var analysis: ImageAnalysis? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(isScanning) {
        if (!isScanning) {
            cameraProvider?.unbindAll()
            analysis?.clearAnalyzer()
            visible = false
            return@LaunchedEffect
        }
        // Get the camera provider safely (suspend instead of blocking get())
        val provider = suspendCancellableCoroutine<ProcessCameraProvider> { cont ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                cont.resume(future.get()) { cause, _, _ -> null?.let { it(cause) } }
            }, ContextCompat.getMainExecutor(context))
        }

        cameraProvider = provider

        // --- Setup CameraX preview
        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }

        // --- Setup barcode scanner (QR only)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)

        val analysisInstance = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysisInstance.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return@setAnalyzer
            }

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { result ->
                            onResult(result)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeScanner", "Scan failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
        analysis = analysisInstance
        try {
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                analysis
            )
        } catch (e: Exception) {
            Log.e("QrCodeScanner", "Camera binding failed", e)
        }
        visible = true
    }

    // ✅ Immediately release camera when composable leaves screen
    DisposableEffect(Unit) {
        onDispose {
            analysis?.clearAnalyzer()
            cameraProvider?.unbindAll()
            visible = false
            onDispose()
        }
    }

    // ✅ Show camera preview
    if (visible) {
        AndroidView(
            factory = { previewView },
            modifier = modifier.fillMaxSize()
        )
    } else {
        // Invisible placeholder for smooth layout transition
        Box(modifier = modifier.fillMaxSize())
    }
}