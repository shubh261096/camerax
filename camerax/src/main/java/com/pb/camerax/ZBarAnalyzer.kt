package com.pb.camerax

import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.yanzhenjie.zbar.Config
import com.yanzhenjie.zbar.ImageScanner
import java.io.ByteArrayOutputStream


class ZBarAnalyzer(
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
        var scanner: ImageScanner
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
                    val bitmap = imageProxy.toBitmap()
                    val W = bitmap.width
                    val H = bitmap.height
                    val photodata = IntArray(W * H)
                    bitmap.getPixels(photodata, 0, W, 0, 0, W, H)

                    val greyData = ByteArray(W * H)
                    for (i in greyData.indices) {
                        greyData[i] = (((photodata[i] and 0x00ff0000 shr 16)
                                * 19595) + ((photodata[i] and 0x0000ff00 shr 8)
                                * 38469) + (photodata[i] and 0x000000ff) * 7472 shr 16).toByte()
                    }

                    val barcode = com.yanzhenjie.zbar.Image(W, H, "GREY")
                    barcode.data = greyData
                    /* Instance barcode scanner */
                    scanner = ImageScanner()
                    scanner.setConfig(0, Config.X_DENSITY, 3)
                    scanner.setConfig(0, Config.Y_DENSITY, 3)

                    val result: Int = scanner.scanImage(barcode)
                    if (result != 0) {
                        val syms = scanner.results
                        var resultString = ""
                        for (sym in syms) {
                            resultString = "" + sym.data
                        }
                        if (resultString == "") {
                            Log.d("ZBarAnalyzer", "decode: empty")
                        } else {
                            Log.d("ZBarAnalyzer", "decode: $resultString")
                            onQrCodesDetected(resultString)
                        }
                    } else {
                        Log.i("ZBarAnalyzer", "Error")
                    }
                } else {
                    Log.e("ZBarAnalyzer", "something went wrong")
                }
            }
        } catch (ise: IllegalStateException) {
            ise.printStackTrace()
        }
    }


    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


}
