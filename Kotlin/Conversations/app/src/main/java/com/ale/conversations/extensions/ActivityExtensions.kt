package com.ale.conversations.extensions

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.core.app.ActivityCompat

// Extension method to check if permissions are granted
fun Activity.hasPermission(vararg permissions: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        permissions.all { permission ->
            applicationContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    } else true
}

// Extension method to ask for permissions
fun Activity.askPermission(vararg permissions: String, @IntRange(from = 0) requestCode: Int) =
    ActivityCompat.requestPermissions(this, permissions, requestCode)

// Extension method to display a toast in UI thread
fun Activity.toast(message: String) = runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }