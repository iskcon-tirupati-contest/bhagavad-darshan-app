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

data class CreatedOrder(
    val id: String,
    val amountPaise: Int,
    val currency: String
)

/**
 * Razorpay Orders for the embedded Checkout SDK. The devotee picks their UPI app
 * (or card) inside Checkout, so no QR scan is needed on the paying device.
 */
class RazorpayOrderService(
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

    fun publicKeyId(): String = keyId

    private fun authHeader(): String {
        val token = Base64.encodeToString(
            "$keyId:$keySecret".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        return "Basic $token"
    }

    suspend fun createOrder(
        amountPaise: Int,
        receipt: String,
        devoteeName: String,
        phone: String,
        planLabel: String
    ): CreatedOrder = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("amount", amountPaise)
            put("currency", "INR")
            put("receipt", receipt.take(40))
            put("payment_capture", 1)
            put("notes", JSONObject().apply {
                put("devotee", devoteeName.take(100))
                put("phone", phone.take(15))
                put("plan", planLabel.take(60))
                put("magazine", "Bhagavad Darshan")
            })
        }
        val request = Request.Builder()
            .url("$BASE/orders")
            .header("Authorization", authHeader())
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON_MEDIA))
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error(parseError(text, response.code))
            val o = JSONObject(text)
            CreatedOrder(
                id = o.getString("id"),
                amountPaise = o.optInt("amount", amountPaise),
                currency = o.optString("currency", "INR")
            )
        }
    }

    /** Server-side truth for a Checkout result: payment must be captured/authorized. */
    suspend fun isPaymentSuccessful(paymentId: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE/payments/$paymentId")
            .header("Authorization", authHeader())
            .get()
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@use false
                val status = JSONObject(text).optString("status")
                status == "captured" || status == "authorized"
            }
        }.getOrDefault(false)
    }

    private fun parseError(body: String, code: Int): String = try {
        JSONObject(body).optJSONObject("error")
            ?.optString("description")
            ?.ifBlank { null }
            ?: "Razorpay error HTTP $code"
    } catch (_: Exception) {
        "Razorpay error HTTP $code: ${body.take(200)}"
    }

    companion object {
        private const val BASE = "https://api.razorpay.com/v1"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
