package com.pb.camerax

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Rational
import android.view.TextureView
import androidx.annotation.NonNull
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner


open class BarCodeReaderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var config: BarCodeCameraConfiguration
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    var barCodeReaderListener: BarCodeReaderListener? = null
    private var lensFacing: CameraX.LensFacing? = null
    private var isSuccess = false

    private fun buildUseCases() {

        val screenAspectRatio = Rational(width, height)
        val screenTargetRotation = display.rotation

        //Preview
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(screenTargetRotation)
            setLensFacing(lensFacing)
        }.build()

        preview = AutoFitPreviewBuilder.build(previewConfig, this)
        //End - Preview

        //ImageAnalyze
        val analysisConfig = ImageAnalysisConfig.Builder().apply {
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(screenTargetRotation)
            setLensFacing(lensFacing)
            // Use a worker thread for image analysis to prevent preview glitches
            val analyzerThread = HandlerThread("BarCodeAnalyzer").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than analyzing *every* image
            setImageReaderMode(config.readerMode)
        }.build()

        imageAnalyzer = ImageAnalysis(analysisConfig)

        imageAnalyzer?.apply {
            analyzer = BarCodeAnalyzer().apply {
                onFrameAnalyzed { qrCode, status, _, _, _, _ ->
                    if (qrCode != null && !isSuccess) {
                        barCodeReaderListener?.onSuccess(qrCode, status)
                        isSuccess = true // This is done so that onSuccess invokes only onetime
                    } else {
                        barCodeReaderListener?.onFailure(status)
                    }
                }
            }
        }
    }

    fun initCamera(
        activity: Activity,
        fragment: Fragment?,
        lifecycleOwner: LifecycleOwner,
        conf: BarCodeCameraConfiguration = BarCodeCameraConfiguration()
    ) {
        this.lensFacing = if (TextUtils.equals(conf.lensFacing, "FRONT"))
            CameraX.LensFacing.FRONT
        else
            CameraX.LensFacing.BACK
        this.lifecycleOwner = lifecycleOwner
        this.config = conf
        if (PermissionUtils.isCameraPermissionGranted(activity)) {
            startCamera()
        } else {
            PermissionUtils.grantCameraPermission(activity, fragment)
        }
    }

    private fun startCamera() {
        this.post {
            CameraX.unbindAll()
            buildUseCases()
            CameraX.bindToLifecycle(
                this.lifecycleOwner,
                preview,
                imageAnalyzer
            )
        }
    }

    fun enableTorch(enableTorch: Boolean) {
        preview?.enableTorch(enableTorch)
    }

    fun isTorchOn(): Boolean {
        return preview?.isTorchOn ?: false
    }


    open fun onRequestPermissionsResult(
        activity: Activity, @NonNull permissions: Array<String?>?, @NonNull grantResults: IntArray,
        requestCode: Int
    ) {
        when (requestCode) {
            PermissionUtils.PERMISSIONS_REQUEST_CAMERA ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.CAMERA
                        )
                    ) {
                        barCodeReaderListener?.onInitFailure(true)
                    } else {
                        barCodeReaderListener?.onInitFailure(false)
                    }
                }
        }
    }

}