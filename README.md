# CameraX Library 
This Sample uses my custom camerax library where BarCode Analyzer and ImageCapture is used

This project will directly run the application.

## Usage
If someone wants to use my camerax library. Follow these steps given below:
1. Add camerax as a library in your project to use it as a library.

2. To use Barcode Scanning:

    a. Add these lines in your activity.xml file
    ```android
    <com.pb.camerax.BarCodeReaderView
          android:id="@+id/barCodeView"
          android:layout_width="500dp"
          android:layout_height="500dp"
          app:layout_constraintBottom_toBottomOf="parent"
          app:layout_constraintEnd_toEndOf="parent"
          app:layout_constraintStart_toStartOf="parent"
          app:layout_constraintTop_toTopOf="parent" />
    ```
    
    b. For using in you activity or fragment:
    
      i. Initialize the barCodeReaderListener in onCreate()
      ```android
      barCodeView.barCodeReaderListener = barCodeReaderListener
      ```
      ii. Init the camera in onCreate
      ```android
      barCodeView.initCamera(this,null,this,BarCodeCameraConfiguration("FRONT"))
      ```
      iii. Override the onRequestPermissionsResult method
      ```android
      override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String?>,grantResults: IntArray) {
            barCodeView.onRequestPermissionsResult(this, permissions, grantResults, requestCode)
      }
      ```
      iv. Implement the barCodeReaderListener
      ```android
      private var barCodeReaderListener: BarCodeReaderListener = object : BarCodeReaderListener {
            override fun onSuccess(qrCode: String, status: BarCodeStatus) {
                Toast.makeText(this@BarCodeScannerActivity,"Code : $qrCode",Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(status: BarCodeStatus) {
                Log.i(TAG, status.name)
            }

            override fun onInitFailure(isAlwaysDenied: Boolean) {
                Toast.makeText(this@BarCodeScannerActivity,"Permission denied always : $isAlwaysDenied",Toast.LENGTH_SHORT).show()
                Log.i(TAG, "$isAlwaysDenied")
            }
      }
      ```
      
3. To use Barcode Scanning:

    a. Add these lines in your activity.xml file
    ```android
    <com.pb.camerax.ImageCaptureView
          android:id="@+id/barCodeView"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          app:layout_constraintBottom_toBottomOf="parent"
          app:layout_constraintEnd_toEndOf="parent"
          app:layout_constraintStart_toStartOf="parent"
          app:layout_constraintTop_toTopOf="parent" />
    ```
    
    b. For using in you activity or fragment:
    
      i. Initialize the imageCaptureListener in onCreate()
      ```android
      imageCaptureView.imageCaptureListener = imageCaptureListener
      ```
      ii. Init the camera in onCreate
      ```android
      imageCaptureView.initCamera(this,null,this,BarCodeCameraConfiguration("FRONT")) // USE "BACK for Back Camera"
      ```
      iii. Override the onRequestPermissionsResult method
      ```android
      override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String?>,grantResults: IntArray) {
            imageCaptureView.onRequestPermissionsResult(this, permissions, grantResults, requestCode)
      }
      ```
      iv. Implement the imageCaptureListener
      ```android
      private var barCodeReaderListener: BarCodeReaderListener = object : BarCodeReaderListener {
            override fun onSuccess(qrCode: String, status: BarCodeStatus) {
                Toast.makeText(this@BarCodeScannerActivity,"Code : $qrCode",Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(status: BarCodeStatus) {
                Log.i(TAG, status.name)
            }

            override fun onInitFailure(isAlwaysDenied: Boolean) {
                Toast.makeText(this@BarCodeScannerActivity,"Permission denied always : $isAlwaysDenied",Toast.LENGTH_SHORT).show()
                Log.i(TAG, "$isAlwaysDenied")
            }
      }
      ```
