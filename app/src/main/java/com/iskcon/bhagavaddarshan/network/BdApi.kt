package com.iskcon.bhagavaddarshan.network

import com.iskcon.bhagavaddarshan.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BdApi(private val apiClient: ApiClient = ApiClient()) {

    suspend fun sendOtp(phone: String, purpose: String): Result<JSONObject> =
        post("/v1/auth/otp/send", jsonOf("phone" to phone, "purpose" to purpose))

    suspend fun verifyLogin(phone: String, otp: String): Result<JSONObject> =
        post(
            "/v1/auth/otp/verify",
            jsonOf("phone" to phone, "otp" to otp, "purpose" to "login")
        )

    suspend fun verifyRegister(
        phone: String,
        otp: String,
        name: String,
        houseNo: String,
        street: String,
        villageTown: String,
        district: String,
        pincode: String,
        state: String
    ): Result<JSONObject> = post(
        "/v1/auth/otp/verify",
        jsonOf(
            "phone" to phone,
            "otp" to otp,
            "purpose" to "register",
            "name" to name,
            "houseNo" to houseNo,
            "street" to street,
            "villageTown" to villageTown,
            "district" to district,
            "pincode" to pincode,
            "state" to state
        )
    )

    suspend fun getPlans(): Result<JSONObject> = get("/v1/customer/plans")

    suspend fun getMe(token: String): Result<JSONObject> = get("/v1/customer/me", token)

    suspend fun updateProfile(
        token: String,
        fields: JSONObject
    ): Result<JSONObject> = request {
        apiClient.put("/v1/customer/profile", fields, token)
    }

    suspend fun createPaymentOrder(
        token: String,
        planYears: Int,
        subscriptionId: Long? = null,
        testAmount: Boolean = BuildConfig.TEST_PAYMENTS,
        purpose: String = "subscription",
        amountPaise: Int? = null,
        notes: String? = null,
        shippingPaise: Int = 0
    ): Result<JSONObject> {
        val body = jsonOf(
            "planYears" to planYears,
            "subscriptionId" to subscriptionId?.takeIf { it > 0L },
            "purpose" to purpose,
            "amountPaise" to amountPaise,
            "notes" to notes,
            "shippingPaise" to shippingPaise
        )
        if (testAmount) body.put("testAmount", "1")
        return post("/v1/payments/orders", body, token)
    }

    suspend fun createUpiIntent(token: String, orderId: String): Result<JSONObject> =
        post("/v1/payments/upi-intent", jsonOf("orderId" to orderId), token)

    suspend fun orderPaymentStatus(token: String, orderId: String): Result<JSONObject> =
        get("/v1/payments/order-status/$orderId", token)

    suspend fun paymentStatus(token: String, paymentId: String): Result<JSONObject> =
        get("/v1/payments/status/$paymentId", token)

    suspend fun confirmPayment(
        token: String,
        orderId: String,
        paymentId: String
    ): Result<JSONObject> = post(
        "/v1/payments/confirm",
        jsonOf("orderId" to orderId, "paymentId" to paymentId),
        token
    )

    suspend fun failPayment(
        token: String,
        orderId: String,
        paymentId: String? = null,
        message: String
    ): Result<JSONObject> = post(
        "/v1/payments/fail",
        jsonOf(
            "orderId" to orderId,
            "paymentId" to paymentId,
            "message" to message
        ),
        token
    )

    suspend fun paymentHistory(token: String): Result<JSONObject> =
        get("/v1/payments/history", token)

    suspend fun listComplaints(token: String): Result<JSONObject> =
        get("/v1/complaints", token)

    suspend fun createComplaint(
        token: String,
        message: String,
        category: String,
        transactionRef: String
    ): Result<JSONObject> = post(
        "/v1/complaints",
        jsonOf(
            "message" to message,
            "category" to category,
            "transactionRef" to transactionRef
        ),
        token
    )

    /** Public agent support complaint (download / install / login issues). */
    suspend fun submitAgentSupportComplaint(
        name: String,
        phone: String,
        deviceModel: String,
        issue: String,
        otherDetails: String = ""
    ): Result<JSONObject> = post(
        "/v1/support/agent-complaint",
        jsonOf(
            "name" to name,
            "phone" to phone,
            "deviceModel" to deviceModel,
            "issue" to issue,
            "otherDetails" to otherDetails.takeIf { it.isNotBlank() }
        )
    )

    suspend fun listAdminComplaints(
        token: String,
        q: String = "",
        status: String? = null,
        source: String? = null
    ): Result<JSONObject> {
        val qs = buildString {
            append("?")
            if (q.isNotBlank()) append("q=").append(java.net.URLEncoder.encode(q, "UTF-8")).append('&')
            if (!status.isNullOrBlank()) append("status=").append(status).append('&')
            if (!source.isNullOrBlank()) append("source=").append(source).append('&')
        }.trimEnd('&', '?')
        return get("/v1/complaints/admin$qs", token)
    }

    suspend fun updateComplaintStatus(
        token: String,
        id: Long,
        status: String
    ): Result<JSONObject> = request {
        apiClient.patch(
            "/v1/complaints/$id/status",
            jsonOf("status" to status),
            token
        )
    }

    // ── Staff / admin (cloud Postgres) ──────────────────────────────

    suspend fun listSubscriptions(
        token: String,
        q: String = "",
        status: String? = null,
        agentId: Long? = null,
        expiringDays: Int? = null
    ): Result<JSONObject> {
        val qs = buildString {
            append("?")
            if (q.isNotBlank()) append("q=").append(java.net.URLEncoder.encode(q, "UTF-8")).append('&')
            if (!status.isNullOrBlank()) append("status=").append(status).append('&')
            if (agentId != null && agentId > 0) append("agentId=").append(agentId).append('&')
            if (expiringDays != null) append("expiringDays=").append(expiringDays).append('&')
        }.trimEnd('&', '?')
        return get("/v1/subscriptions$qs", token)
    }

    suspend fun getSubscription(token: String, id: Long): Result<JSONObject> =
        get("/v1/subscriptions/$id", token)

    suspend fun createSubscription(token: String, body: JSONObject): Result<JSONObject> =
        post("/v1/subscriptions", body, token)

    suspend fun updateSubscription(token: String, id: Long, body: JSONObject): Result<JSONObject> =
        request { apiClient.put("/v1/subscriptions/$id", body, token) }

    suspend fun deleteSubscription(token: String, id: Long): Result<JSONObject> =
        request { apiClient.delete("/v1/subscriptions/$id", token) }

    suspend fun markSubscriptionPaid(
        token: String,
        id: Long,
        paymentRef: String,
        paymentMethod: String,
        razorpayQrId: String = ""
    ): Result<JSONObject> = post(
        "/v1/subscriptions/$id/mark-paid",
        jsonOf(
            "paymentRef" to paymentRef,
            "paymentMethod" to paymentMethod,
            "razorpayQrId" to razorpayQrId.ifBlank { null }
        ),
        token
    )

    suspend fun listAgents(token: String): Result<JSONObject> = get("/v1/agents", token)

    suspend fun getAgent(token: String, id: Long): Result<JSONObject> = get("/v1/agents/$id", token)

    suspend fun createAgent(token: String, body: JSONObject): Result<JSONObject> =
        post("/v1/agents", body, token)

    suspend fun updateAgent(token: String, id: Long, body: JSONObject): Result<JSONObject> =
        request { apiClient.put("/v1/agents/$id", body, token) }

    suspend fun deleteAgent(token: String, id: Long): Result<JSONObject> =
        request { apiClient.delete("/v1/agents/$id", token) }

    suspend fun listStaffPlans(token: String, activeOnly: Boolean = false): Result<JSONObject> =
        get(if (activeOnly) "/v1/plans?active=1" else "/v1/plans", token)

    suspend fun createPlan(token: String, body: JSONObject): Result<JSONObject> =
        post("/v1/plans", body, token)

    suspend fun updatePlan(token: String, id: Long, body: JSONObject): Result<JSONObject> =
        request { apiClient.put("/v1/plans/$id", body, token) }

    suspend fun deletePlan(token: String, id: Long): Result<JSONObject> =
        request { apiClient.delete("/v1/plans/$id", token) }

    suspend fun dashboard(
        token: String,
        agentId: Long? = null,
        period: String? = null,
        year: Int? = null
    ): Result<JSONObject> {
        val qs = buildString {
            append("?")
            if (agentId != null && agentId > 0) append("agentId=").append(agentId).append('&')
            if (!period.isNullOrBlank()) append("period=").append(period).append('&')
            if (year != null && year > 0) append("year=").append(year).append('&')
        }.trimEnd('&', '?')
        return get("/v1/analytics/dashboard$qs", token)
    }

    suspend fun createStaffQr(
        token: String,
        amountPaise: Int,
        description: String,
        devoteeName: String,
        phone: String
    ): Result<JSONObject> = post(
        "/v1/staff/payments/qr",
        jsonOf(
            "amountPaise" to amountPaise,
            "description" to description,
            "devoteeName" to devoteeName,
            "phone" to phone
        ),
        token
    )

    suspend fun staffQrStatus(token: String, qrId: String): Result<JSONObject> =
        get("/v1/staff/payments/qr/$qrId", token)

    suspend fun listDevotees(
        token: String,
        q: String = "",
        agentId: Long? = null
    ): Result<JSONObject> {
        val qs = buildString {
            append("?")
            if (q.isNotBlank()) append("q=").append(java.net.URLEncoder.encode(q, "UTF-8")).append('&')
            if (agentId != null && agentId > 0) append("agentId=").append(agentId).append('&')
        }.trimEnd('&', '?')
        return get("/v1/devotees$qs", token)
    }

    suspend fun createDevotee(token: String, body: JSONObject): Result<JSONObject> =
        post("/v1/devotees", body, token)

    suspend fun getDevotee(token: String, id: Long): Result<JSONObject> =
        get("/v1/devotees/$id", token)

    suspend fun updateDevotee(token: String, id: Long, body: JSONObject): Result<JSONObject> =
        request { apiClient.put("/v1/devotees/$id", body, token) }

    suspend fun draftDevotee(token: String, id: Long): Result<JSONObject> =
        request { apiClient.delete("/v1/devotees/$id", token) }

    /** Public — no auth. Client compares versionCode to BuildConfig.VERSION_CODE. */
    suspend fun getAppVersion(platform: String = "android"): Result<JSONObject> =
        get("/v1/app/version?platform=$platform")

    private suspend fun get(path: String, token: String? = null): Result<JSONObject> =
        request { apiClient.get(path, token) }

    private suspend fun post(
        path: String,
        body: JSONObject,
        token: String? = null
    ): Result<JSONObject> = request { apiClient.post(path, body, token) }

    private suspend fun request(block: () -> JSONObject): Result<JSONObject> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = block()
                if (json.has("ok") && !json.optBoolean("ok", true)) {
                    throw ApiException(400, json.optString("message", "Request failed"))
                }
                json
            }
        }

    private fun jsonOf(vararg values: Pair<String, Any?>): JSONObject =
        JSONObject().apply {
            values.forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is JSONObject, is JSONArray -> put(key, value)
                    else -> put(key, value)
                }
            }
        }
}
