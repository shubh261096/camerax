package com.pb.pbil.sample

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.pb.camerax.BarCodeCameraConfiguration
import com.pb.camerax.ImageCaptureListener
import com.pb.camerax.ImageCodeStatus
import kotlinx.android.synthetic.main.activity_image.*


class ImageCaptureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        imageCaptureView.imageCaptureListener = imageCaptureListener
        imageCaptureView.initCamera(
            this,
            null,
            this@ImageCaptureActivity,
            BarCodeCameraConfiguration("FRONT") // USE "BACK for Back Camera"
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, @NonNull permissions: Array<String?>,
        grantResults: IntArray
    ) {
        imageCaptureView.onRequestPermissionsResult(this, permissions, grantResults, requestCode)
    }

    private var imageCaptureListener: ImageCaptureListener = object : ImageCaptureListener {

        override fun onCaptureSuccess(path: String, imageCodeStatus: ImageCodeStatus) {
            Log.i(TAG, path)
        }

        override fun onInitFailure(isAlwaysDenied: Boolean, imageCodeStatus: ImageCodeStatus) {
            Log.i(TAG, imageCodeStatus.name)
        }

        override fun onInitSuccess(imageCodeStatus: ImageCodeStatus) {
            /** This method #captureImage is called after successful initialization. You can use this method on click of a button */
            Handler().postDelayed({
                imageCaptureView.captureImage(this@ImageCaptureActivity)
            }, 5000)
        }

        override fun onCaptureFailure(error: String, imageCodeStatus: ImageCodeStatus) {
            Log.i(TAG, error)
        }
    }

    companion object {
        const val TAG = "ImageCaptureActivity"
    }
}
