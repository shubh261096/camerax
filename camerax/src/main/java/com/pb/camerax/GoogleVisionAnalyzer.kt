package com.pb.camerax

import android.content.Context
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector


class GoogleVisionAnalyzer(
    private val context: Context,
    private val onQrCodesDetected: (qrCodes: String) -> Unit
) :
    ImageAnalysis.Analyzer {
    /*
        https://developer.android.com/training/camerax/configuration

        Default resolution: The default target resolution setting is 640x480.

        Adjusting both target resolution and corresponding aspect ratio will result
        in a best-supported resolution under 1080p (max analysis resolution).
    */
    override fun analyze(imageProxy: ImageProxy, rotationDegrees: Int) {
        // okay - manage rotation, not needed for QRCode decoding [-;
        // okay - manage it for barcode scanning instead!!!
        try {
            imageProxy.image?.let {
                // ImageProxy uses an ImageReader under the hood:
                // https://developer.android.com/reference/androidx/camera/core/ImageProxy.html
                // That has a default format of YUV_420_888 if not changed.
                // https://developer.android.com/reference/android/graphics/ImageFormat.html#YUV_420_888
                // https://developer.android.com/reference/android/media/ImageReader.html
                if ((it.format == ImageFormat.YUV_420_888
                            || it.format == ImageFormat.YUV_422_888
                            || it.format == ImageFormat.YUV_444_888)
                    && it.planes.size == 3
                ) {
                    val buffer = it.planes[0].buffer // We get the luminance plane only, since we

                    val barcodeDetector =
                        BarcodeDetector.Builder(context)
                            .setBarcodeFormats(Barcode.ALL_FORMATS).build()
                    if (!barcodeDetector.isOperational) {
                        Log.i("TAG", "Could not set up the detector!")
                        return
                    }
                    val mFrame: Frame = Frame.Builder()
                        .setImageData(buffer, imageProxy.width, imageProxy.height, ImageFormat.NV21)
                        .build()
                    val barcodes = barcodeDetector.detect(mFrame)
                    for (x in 0 until barcodes.size()) {
                        val barcode = barcodes.valueAt(x)
                        onQrCodesDetected(barcode.displayValue)
                        Log.e("AB_READ", barcode.displayValue)
                    }

                } else {
                    Log.e("GoogleVisionAnalyzer", "something went wrong")
                }
            }
        } catch (ise: IllegalStateException) {
            ise.printStackTrace()
        }
    }


}
