package com.airnettie.mobile.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

object QRUtils {
    fun generateQRCode(data: String, size: Int = 512): Bitmap {
        val bitMatrix: BitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}