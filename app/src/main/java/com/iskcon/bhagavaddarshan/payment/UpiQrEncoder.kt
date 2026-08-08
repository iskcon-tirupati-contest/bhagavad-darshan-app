package com.iskcon.bhagavaddarshan.payment

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder

object UpiQrEncoder {

    fun buildUpiPayUri(
        vpa: String,
        payeeName: String,
        amountRupees: Int,
        note: String
    ): String {
        val am = "%.2f".format(amountRupees.toDouble())
        val pn = URLEncoder.encode(payeeName, "UTF-8")
        val tn = URLEncoder.encode(note.take(50), "UTF-8")
        return "upi://pay?pa=$vpa&pn=$pn&am=$am&cu=INR&tn=$tn"
    }

    fun encodeBitmap(content: String, sizePx: Int = 720): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
