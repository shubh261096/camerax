package com.pb.camerax

interface BarCodeReaderListener {
    fun onSuccess(qrCode: String, status: BarCodeStatus)
    fun onFailure(status: BarCodeStatus)
    fun onInitFailure(isAlwaysDenied: Boolean)
}