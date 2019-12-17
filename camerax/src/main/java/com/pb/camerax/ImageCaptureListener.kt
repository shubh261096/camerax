package com.pb.camerax

interface ImageCaptureListener {
    fun onCaptureSuccess(path: String, imageCodeStatus: ImageCodeStatus)
    fun onCaptureFailure(error: String, imageCodeStatus: ImageCodeStatus)
    fun onInitSuccess(imageCodeStatus: ImageCodeStatus)
    fun onInitFailure(isAlwaysDenied: Boolean, imageCodeStatus: ImageCodeStatus)
}