package com.iskcon.bhagavaddarshan.whatsapp

import com.iskcon.bhagavaddarshan.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * MNV WhatsApp campaign API — same provider as iskconcontest.org.
 * Credentials come from BuildConfig (local.properties); contest repo is never modified.
 */
class WhatsAppClient(
    private val apiKey: String = BuildConfig.MNV_API_KEY,
    private val apiUrl: String = BuildConfig.MNV_API_URL,
    private val username: String = BuildConfig.MNV_USERNAME,
    private val defaultCampaign: String = BuildConfig.MNV_CAMPAIGN_NAME,
    private val registrationCampaign: String = BuildConfig.MNV_REGISTRATION_CAMPAIGN
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean = apiKey.isNotBlank() && apiUrl.isNotBlank()

    private fun normPhone(phone: String): String =
        phone.filter(Char::isDigit).takeLast(10)

    suspend fun send(
        phone: String,
        templateParams: List<String>,
        campaignName: String = defaultCampaign,
        source: String = "bhagavad-darshan-app",
        buttons: JSONArray = JSONArray(),
        paramsFallbackValue: JSONObject? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!isConfigured()) error("WhatsApp not configured")
            val dest = "91${normPhone(phone)}"
            if (dest.length != 12) error("Invalid phone")
            val payload = JSONObject().apply {
                put("apiKey", apiKey)
                put("campaignName", campaignName.ifBlank { defaultCampaign })
                put("destination", dest)
                put("userName", username.ifBlank { "IskconContest" })
                put("templateParams", JSONArray(templateParams))
                put("source", source)
                put("media", JSONObject())
                put("buttons", buttons)
                put("carouselCards", JSONArray())
                put("location", JSONObject())
                put("attributes", JSONObject())
                paramsFallbackValue?.let { put("paramsFallbackValue", it) }
            }
            val body = payload.toString().toRequestBody(JSON)
            val req = Request.Builder().url(apiUrl).post(body).build()
            client.newCall(req).execute().use { res ->
                val text = res.body?.string().orEmpty()
                if (res.code !in 200..299) error("WA ${res.code}: ${text.take(200)}")
                text
            }
        }
    }

    /** MNV OTP template requires both the body value and URL-button value. */
    suspend fun sendOtp(phone: String, code: String): Result<String> {
        val buttons = JSONArray().put(
            JSONObject().apply {
                put("type", "button")
                put("sub_type", "url")
                put("index", 0)
                put(
                    "parameters",
                    JSONArray().put(
                        JSONObject().apply {
                            put("type", "text")
                            put("text", code)
                        }
                    )
                )
            }
        )
        return send(
            phone = phone,
            campaignName = defaultCampaign.ifBlank { "OTP" },
            templateParams = listOf(code),
            source = "new-landing-page form",
            buttons = buttons,
            paramsFallbackValue = JSONObject().put("FirstName", code)
        )
    }

    suspend fun sendRegistration(
        phone: String,
        name: String,
        receipt: String,
        plan: String,
        amount: String = "",
        duration: String = "",
        startMonth: String = "",
        endDate: String = "",
        address: String = ""
    ) =
        send(
            phone = phone,
            campaignName = registrationCampaign.ifBlank { defaultCampaign },
            // Matches WABA template bd_subscription_success:
            // {{1}} name {{2}} receipt {{3}} plan {{4}} amount
            // {{5}} duration {{6}} startMonth {{7}} endDate {{8}} address
            templateParams = listOf(
                name,
                receipt,
                plan,
                amount.ifBlank { "—" },
                duration.ifBlank { plan },
                startMonth.ifBlank { "—" },
                endDate.ifBlank { "—" },
                address.ifBlank { "—" }
            ),
            source = "bd-registration"
        )

    suspend fun sendExpiryReminder(phone: String, name: String, endDate: String) =
        send(
            phone = phone,
            templateParams = listOf(name, endDate, "Your Bhagavad Darshan subscription is ending soon. Please renew."),
            source = "bd-expiry"
        )

    suspend fun sendPaymentFailed(phone: String, name: String, amount: String) =
        send(
            phone = phone,
            templateParams = listOf(name, amount, "Payment not completed for Bhagavad Darshan. Please retry or contact agent."),
            source = "bd-payment-failed"
        )

    suspend fun sendAddressChange(phone: String, name: String) =
        send(
            phone = phone,
            templateParams = listOf(name, "Your postal address was updated for Bhagavad Darshan."),
            source = "bd-address-change"
        )

    suspend fun sendDispatch(phone: String, name: String, monthLabel: String) =
        send(
            phone = phone,
            campaignName = BuildConfig.MNV_DISPATCH_CAMPAIGN.ifBlank { defaultCampaign },
            templateParams = listOf(name, monthLabel, "Bhagavad Darshan magazine dispatched"),
            source = "bd-dispatch"
        )

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
