package com.pb.camerax

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionUtils {

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA = 101
        const val PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 102

        fun isCameraPermissionGranted(context: Context?): Boolean {
            return ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun isStoragePermissionGranted(context: Context?): Boolean {
            return ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun grantStorageCameraPermission(
            activity: Activity?,
            fragment: Fragment?
        ) {
            if (fragment == null) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ),
                    PERMISSIONS_REQUEST_READ_WRITE_STORAGE
                )
            } else {
                fragment.requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ),
                    PERMISSIONS_REQUEST_READ_WRITE_STORAGE
                )
            }
        }

        fun grantCameraPermission(activity: Activity, fragment: Fragment?) {
            if (fragment == null) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSIONS_REQUEST_CAMERA
                )
            } else {
                fragment.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSIONS_REQUEST_CAMERA
                )
            }
        }
    }
}