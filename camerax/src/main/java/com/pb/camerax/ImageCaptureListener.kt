package com.pb.camerax

interface ImageCaptureListener {
    fun onSuccess(path: String)
    fun onFailure(isAlwaysDenied: Boolean, imageCodeStatus: ImageCodeStatus)
    fun onInitSuccess(imageCodeStatus: ImageCodeStatus)
}