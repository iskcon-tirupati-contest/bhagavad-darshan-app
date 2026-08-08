package com.iskcon.bhagavaddarshan.payment

import android.util.Base64
import com.iskcon.bhagavaddarshan.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RazorpayPaymentInfo(
    val id: String,
    val status: String,
    val amountPaise: Int,
    val captured: Boolean
)

class RazorpayPaymentApi(
    private val keyId: String = BuildConfig.RAZORPAY_KEY_ID,
    private val keySecret: String = BuildConfig.RAZORPAY_KEY_SECRET
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean =
        (keyId.startsWith("rzp_test_") || keyId.startsWith("rzp_live_")) && keySecret.isNotBlank()

    private fun auth(): String =
        "Basic " + Base64.encodeToString(
            "$keyId:$keySecret".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )

    suspend fun fetchPayment(paymentId: String): Result<RazorpayPaymentInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder()
                    .url("https://api.razorpay.com/v1/payments/$paymentId")
                    .header("Authorization", auth())
                    .get()
                    .build()
                client.newCall(req).execute().use { res ->
                    val body = res.body?.string().orEmpty()
                    if (!res.isSuccessful) error("Razorpay ${res.code}: ${body.take(200)}")
                    val o = JSONObject(body)
                    RazorpayPaymentInfo(
                        id = o.getString("id"),
                        status = o.optString("status"),
                        amountPaise = o.optInt("amount"),
                        captured = o.optBoolean("captured") || o.optString("status") == "captured"
                    )
                }
            }
        }

    suspend fun fetchQrPayments(qrId: String): Result<List<RazorpayPaymentInfo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder()
                    .url("https://api.razorpay.com/v1/payments/qr_codes/$qrId/payments")
                    .header("Authorization", auth())
                    .get()
                    .build()
                client.newCall(req).execute().use { res ->
                    val body = res.body?.string().orEmpty()
                    if (!res.isSuccessful) error("Razorpay ${res.code}: ${body.take(200)}")
                    val items = JSONObject(body).optJSONArray("items") ?: return@use emptyList()
                    buildList {
                        for (i in 0 until items.length()) {
                            val o = items.getJSONObject(i)
                            add(
                                RazorpayPaymentInfo(
                                    id = o.getString("id"),
                                    status = o.optString("status"),
                                    amountPaise = o.optInt("amount"),
                                    captured = o.optBoolean("captured") ||
                                        o.optString("status") == "captured"
                                )
                            )
                        }
                    }
                }
            }
        }
}
