package com.pb.camerax

import androidx.camera.core.ImageAnalysis

data class BarCodeCameraConfiguration(
    var lensFacing: String = "FRONT",
    var readerMode: ImageAnalysis.ImageReaderMode = ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
)
