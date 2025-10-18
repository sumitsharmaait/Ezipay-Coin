package com.app.ezipaycoin.fcm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestNotificationPermissionOnce() {

    val context = LocalContext.current
    val permissionsToRequest = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.addAll(
            listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        )
    } else {
        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    permissionsToRequest.add(Manifest.permission.CAMERA)


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val notificationGranted = results[Manifest.permission.POST_NOTIFICATIONS] ?: false
        val mediaGranted = results[Manifest.permission.READ_MEDIA_IMAGES]
            ?: results[Manifest.permission.READ_EXTERNAL_STORAGE]
            ?: false

        val cameraGranted =
            results[Manifest.permission.CAMERA] ?: false

        if (notificationGranted) {
            Log.d("Permissions", "✅ Notification permission granted")
        }
        if (mediaGranted) {
            Log.d("Permissions", "✅ Media permission granted")
        }
        if (cameraGranted) {
            Log.d("Permissions", "✅ Camera permission granted")
        }
        if (!notificationGranted || !mediaGranted || !cameraGranted) {
            Log.d("Permissions", "❌ Permission denied")
        }
    }

    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allGranted) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d("Permissions", "🔔 All permissions already granted")
        }
    }

}