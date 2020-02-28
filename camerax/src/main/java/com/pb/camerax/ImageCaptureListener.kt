package com.pb.camerax

import android.graphics.Bitmap

interface ImageCaptureListener {
    fun onCaptureSuccess(bitmap: Bitmap, imageCodeStatus: ImageCodeStatus)
    fun onCaptureFailure(error: String, imageCodeStatus: ImageCodeStatus)
    fun onInitSuccess(imageCodeStatus: ImageCodeStatus)
    fun onInitFailure(isAlwaysDenied: Boolean, imageCodeStatus: ImageCodeStatus)
}