package com.example.mrflashyflashapp.data.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionManager {
    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    }

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRationale(context: Context, permission: String): Boolean {

        //This would typically check if we should show explanation to user

        //for now implementing only basic logic

        return false

    }

}

