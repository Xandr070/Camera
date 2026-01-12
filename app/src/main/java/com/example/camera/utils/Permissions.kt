package com.example.camera.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object Permissions {
    val required = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
}

fun Context.hasAllPermissions(): Boolean {
    return Permissions.required.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun Map<String, Boolean>.allGranted(): Boolean {
    return Permissions.required.all { this[it] == true }
}

fun Map<String, Boolean>.deniedList(): String {
    return entries
        .filter { it.key in Permissions.required && !it.value }
        .joinToString(", ") { it.key }
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

