package com.pb.pbil.sample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.pb.camerax.BarCodeCameraConfiguration
import com.pb.camerax.BarCodeReaderListener
import com.pb.camerax.BarCodeStatus
import kotlinx.android.synthetic.main.activity_main.*


class BarCodeScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(TAG, "onCreate Called")
        barCodeView.barCodeReaderListener = barCodeReaderListener
        barCodeView.initCamera(
            this,
            null,
            this@BarCodeScannerActivity,
            BarCodeCameraConfiguration("FRONT")
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, @NonNull permissions: Array<String?>,
        grantResults: IntArray
    ) {
        barCodeView.onRequestPermissionsResult(this, permissions, grantResults, requestCode)
    }

    private var barCodeReaderListener: BarCodeReaderListener = object : BarCodeReaderListener {

        override fun onSuccess(qrCode: String, status: BarCodeStatus) {
            Toast.makeText(
                this@BarCodeScannerActivity,
                "Code : $qrCode",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onFailure(status: BarCodeStatus) {
            Log.i(TAG, status.name)
        }

        override fun onInitFailure(isAlwaysDenied: Boolean) {
            Toast.makeText(
                this@BarCodeScannerActivity,
                "Permission denied always : $isAlwaysDenied",
                Toast.LENGTH_SHORT
            ).show()
            Log.i(TAG, "$isAlwaysDenied")
        }
    }

    companion object {
        const val TAG = "BarCodeScannerActivity"
    }
}
