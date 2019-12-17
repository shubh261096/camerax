package com.pb.camerax

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.TextureView
import android.webkit.MimeTypeMap
import androidx.annotation.NonNull
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


open class ImageCaptureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var outputDirectory: File
    private lateinit var config: BarCodeCameraConfiguration
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null
    var imageCaptureListener: ImageCaptureListener? = null
    private var lensFacing: CameraX.LensFacing? = null

    private fun buildUseCases() {

        //Preview
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(width, height))
            setTargetRotation(display.rotation)
            setTargetResolution(Size(width, height))
            setLensFacing(lensFacing)
        }.build()

        preview = AutoFitPreviewBuilder.build(previewConfig, this)
        //End - Preview


        // Set up the capture use case to allow users to take photos
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
            setTargetResolution(Size(width, height))
            // We request aspect ratio but no resolution to match preview config but letting
            // CameraX optimize for whatever specific resolution best fits requested capture mode
            setTargetAspectRatio(Rational(width, height))
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            setTargetRotation(display.rotation)
        }.build()


        imageCapture = ImageCapture(imageCaptureConfig)

        /* UnComment this if you have to do anything for analyzing
            // Setup image analysis pipeline that computes average pixel luminance in real time
            val analyzerConfig = ImageAnalysisConfig.Builder().apply {
                setLensFacing(lensFacing)
                // In our analysis, we care more about the latest image than analyzing *every* image
                setImageReaderMode(config.readerMode)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                setTargetRotation(display.rotation)
            }.build()

            imageAnalyzer = ImageAnalysis(analyzerConfig).apply {
                analyzer = LuminosityAnalyzer().apply {
                    onFrameAnalyzed {

                    }
                }
            }
        */
    }

    fun initCamera(
        activity: Activity,
        fragment: Fragment?,
        lifecycleOwner: LifecycleOwner,
        conf: BarCodeCameraConfiguration = BarCodeCameraConfiguration()
    ) {
        outputDirectory = getOutputDirectory(activity)
        this.lensFacing = if (TextUtils.equals(conf.lensFacing, "FRONT"))
            CameraX.LensFacing.FRONT
        else
            CameraX.LensFacing.BACK

        this.lifecycleOwner = lifecycleOwner
        this.config = conf

        if (PermissionUtils.isCameraPermissionGranted(activity) && PermissionUtils.isStoragePermissionGranted(
                activity
            )
        ) {
            startCamera()
        } else {
            PermissionUtils.grantStorageCameraPermission(activity, fragment)
        }
    }

    private fun startCamera() {
        this.post {
            CameraX.unbindAll()
            buildUseCases()
            CameraX.bindToLifecycle(
                this.lifecycleOwner,
                preview,
                imageCapture
            )
            imageCaptureListener?.onInitSuccess(ImageCodeStatus.CameraInitSuccess)
        }
    }

    open fun onRequestPermissionsResult(
        activity: Activity, @NonNull permissions: Array<String?>?, @NonNull grantResults: IntArray,
        requestCode: Int
    ) {
        when (requestCode) {
            PermissionUtils.PERMISSIONS_REQUEST_READ_WRITE_STORAGE ->
                if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    startCamera()
                } else if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        imageCaptureListener?.onInitFailure(
                            true,
                            ImageCodeStatus.PermissionStorageFailure
                        )
                    } else {
                        imageCaptureListener?.onInitFailure(
                            false,
                            ImageCodeStatus.PermissionStorageFailure
                        )
                    }
                } else if (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.CAMERA
                        )
                    ) {
                        imageCaptureListener?.onInitFailure(
                            true,
                            ImageCodeStatus.PermissionCameraFailure
                        )
                    } else {
                        imageCaptureListener?.onInitFailure(
                            false,
                            ImageCodeStatus.PermissionCameraFailure
                        )
                    }

                }
        }
    }

    /** Define callback that will be triggered after a photo has been taken and saved to disk */
    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {

        override fun onImageSaved(photoFile: File) {
            Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")

            imageCaptureListener?.onCaptureSuccess(
                photoFile.absolutePath,
                ImageCodeStatus.CaptureSuccess
            )
            // If the folder selected is an external media directory, this is unnecessary
            // but otherwise other apps will not be able to access our images unless we
            // scan them using [MediaScannerConnection]
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null
            )
        }

        override fun onError(
            useCaseError: ImageCapture.UseCaseError,
            message: String,
            cause: Throwable?
        ) {
            Log.e(TAG, "Photo capture failed: $message")
            imageCaptureListener?.onCaptureFailure(message, ImageCodeStatus.CaptureFailure)
            cause?.printStackTrace()
        }
    }

    fun captureImage(activity: Activity) {
        if (PermissionUtils.isCameraPermissionGranted(activity) && PermissionUtils.isStoragePermissionGranted(
                activity
            )
        ) {
            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->

                // Create output file to hold the image
                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

                // Setup image capture metadata
                val metadata = ImageCapture.Metadata().apply {
                    // Mirror image when using the front camera
                    isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT
                }

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(photoFile, imageSavedListener, metadata)
            }
        } else {
            imageCaptureListener?.onInitFailure(
                false,
                ImageCodeStatus.PermissionCameraFailure
            )
        }
    }


    companion object {
        private const val TAG = "ImageCapture"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, TAG).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

}