package com.pb.camerax

enum class BarCodeStatus {
    Success,
    NotFoundException,
    ChecksumException,
    FormatException
}

enum class ImageCodeStatus {
    CaptureSuccess,
    CaptureFailure,
    PermissionCameraFailure,
    PermissionStorageFailure,
    CameraInitSuccess
}