package com.app.ezipaycoin.ui.composables

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
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

/**
 * QR scanner that asks for the camera when it opens. If the user declines it shows why the
 * camera is needed, and once the permission is blocked it points to the system settings.
 */
@Composable
fun QrCodeScanner(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
    onResult: (String) -> Unit,
    onDispose: () -> Unit = {},
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var denied by remember { mutableStateOf(false) }
    var blocked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        if (!ok) {
            denied = true
            val activity = context.findActivity()
            // After a refusal, no rationale means the system will not ask again.
            blocked = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }

    if (granted) {
        QrCodeScannerCamera(modifier, isScanning, onResult, onDispose)
    } else if (denied) {
        Column(
            modifier = modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Camera access is needed to scan a QR code.",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = {
                if (blocked) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } else {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text(if (blocked) "Open settings" else "Allow camera")
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun QrCodeScannerCamera(
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