package com.pb.camerax

enum class BarCodeStatus {
    Success,
    NotFoundException,
    ChecksumException,
    FormatException
}

enum class ImageCodeStatus {
    Success,
    PermissionInitFailure,
    CameraInitSuccess
}