package com.iskcon.bhagavaddarshan.payment

import android.util.Base64
import com.iskcon.bhagavaddarshan.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CreatedQr(
    val id: String,
    val imageUrl: String,
    val amountPaise: Int,
    val closeBy: Long
)

data class QrPaymentStatus(
    val paid: Boolean,
    val paymentId: String?,
    val amountReceivedPaise: Int,
    val qrStatus: String
)

/**
 * Agent-side Razorpay UPI QR: create fixed-amount QR, then poll until devotee pays.
 * Key secret is for this internal agent app only (from local.properties).
 */
class RazorpayQrService(
    private val keyId: String = BuildConfig.RAZORPAY_KEY_ID,
    private val keySecret: String = BuildConfig.RAZORPAY_KEY_SECRET
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean =
        (keyId.startsWith("rzp_test_") || keyId.startsWith("rzp_live_")) &&
            keySecret.isNotBlank()

    private fun authHeader(): String {
        val token = Base64.encodeToString(
            "$keyId:$keySecret".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        return "Basic $token"
    }

    suspend fun createFixedAmountQr(
        amountPaise: Int,
        description: String,
        devoteeName: String,
        phone: String
    ): CreatedQr = withContext(Dispatchers.IO) {
        val closeBy = (System.currentTimeMillis() / 1000L) + 30 * 60 // 30 min
        val bodyJson = JSONObject().apply {
            put("type", "upi_qr")
            put("name", "Bhagavad Darshan")
            put("usage", "single_use")
            put("fixed_amount", true)
            put("payment_amount", amountPaise)
            put("description", description.take(255))
            put("close_by", closeBy)
            put("notes", JSONObject().apply {
                put("devotee", devoteeName.take(100))
                put("phone", phone.take(15))
                put("magazine", "Bhagavad Darshan")
            })
        }
        val request = Request.Builder()
            .url("$BASE/payments/qr_codes")
            .header("Authorization", authHeader())
            .header("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody(JSON_MEDIA))
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error(parseError(text, response.code))
            }
            val o = JSONObject(text)
            CreatedQr(
                id = o.getString("id"),
                imageUrl = o.optString("image_url"),
                amountPaise = o.optInt("payment_amount", amountPaise),
                closeBy = o.optLong("close_by", closeBy)
            )
        }
    }

    suspend fun fetchStatus(qrId: String): QrPaymentStatus = withContext(Dispatchers.IO) {
        val qrReq = Request.Builder()
            .url("$BASE/payments/qr_codes/$qrId")
            .header("Authorization", authHeader())
            .get()
            .build()

        val qrJson = client.newCall(qrReq).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error(parseError(text, response.code))
            JSONObject(text)
        }

        val count = qrJson.optInt("payments_count_received", 0)
        val received = qrJson.optInt("payments_amount_received", 0)
        val status = qrJson.optString("status")
        val closeReason = qrJson.optString("close_reason")
        val paid = count > 0 || closeReason == "paid" || received > 0

        var paymentId: String? = null
        if (paid) {
            paymentId = fetchLatestPaymentId(qrId) ?: "qr_paid_$qrId"
        }
        QrPaymentStatus(
            paid = paid,
            paymentId = paymentId,
            amountReceivedPaise = received,
            qrStatus = status
        )
    }

    private fun fetchLatestPaymentId(qrId: String): String? {
        val req = Request.Builder()
            .url("$BASE/payments/qr_codes/$qrId/payments")
            .header("Authorization", authHeader())
            .get()
            .build()
        client.newCall(req).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) return null
            val items = JSONObject(text).optJSONArray("items") ?: return null
            if (items.length() == 0) return null
            return items.getJSONObject(0).optString("id").ifBlank { null }
        }
    }

    private fun parseError(body: String, code: Int): String {
        return try {
            val err = JSONObject(body).optJSONObject("error")
            err?.optString("description")?.ifBlank { null }
                ?: "Razorpay error HTTP $code"
        } catch (_: Exception) {
            "Razorpay error HTTP $code: ${body.take(200)}"
        }
    }

    companion object {
        private const val BASE = "https://api.razorpay.com/v1"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
