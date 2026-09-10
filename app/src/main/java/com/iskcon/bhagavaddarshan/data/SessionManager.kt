package com.iskcon.bhagavaddarshan.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var agentId: Long
        get() = prefs.getLong(KEY_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ID, value).apply()

    var agentName: String
        get() = prefs.getString(KEY_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var agentPhone: String
        get() = prefs.getString(KEY_PHONE, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_PHONE, value).apply()

    var role: String
        get() = prefs.getString(KEY_ROLE, Agent.Role.AGENT).orEmpty()
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    var subscriptionId: Long
        get() = prefs.getLong(KEY_SUB_ID, 0L)
        set(value) = prefs.edit().putLong(KEY_SUB_ID, value).apply()

    var authToken: String
        get() = prefs.getString(KEY_AUTH_TOKEN, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var customerId: Long
        get() = prefs.getLong(KEY_CUSTOMER_ID, 0L)
        set(value) = prefs.edit().putLong(KEY_CUSTOMER_ID, value).apply()

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER, value).apply()

    var customerLanguage: String
        get() = prefs.getString(KEY_CUSTOMER_LANGUAGE, "en").orEmpty().ifBlank { "en" }
        set(value) = prefs.edit().putString(KEY_CUSTOMER_LANGUAGE, value).apply()

    val isAdmin: Boolean get() = role == Agent.Role.ADMIN
    val isCustomer: Boolean get() = role == Agent.Role.CUSTOMER
    /** Session is valid only with an auth token. Remember-me only prefills phone after logout. */
    val isLoggedIn: Boolean
        get() = authToken.isNotBlank()

    fun clearRememberMe() {
        rememberMe = false
        // Keep current session; only stop auto-prefilling phone after future logout.
    }

    fun login(agent: Agent, remember: Boolean) {
        agentId = agent.id
        agentName = agent.name
        agentPhone = agent.phone
        role = agent.role
        subscriptionId = 0L
        authToken = ""
        customerId = 0L
        rememberMe = remember
    }

    fun loginCustomer(
        name: String,
        phone: String,
        subscriptionId: Long,
        remember: Boolean
    ) {
        agentId = 0L
        agentName = name
        agentPhone = phone.filter(Char::isDigit).takeLast(10)
        role = Agent.Role.CUSTOMER
        this.subscriptionId = subscriptionId
        authToken = ""
        customerId = 0L
        rememberMe = remember
    }

    fun loginFromApi(
        role: String,
        token: String,
        name: String,
        phone: String,
        customerId: Long,
        subscriptionId: Long,
        agentId: Long,
        remember: Boolean
    ) {
        prefs.edit()
            .putString(KEY_ROLE, role)
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_NAME, name)
            .putString(KEY_PHONE, phone.filter(Char::isDigit).takeLast(10))
            .putLong(KEY_CUSTOMER_ID, customerId)
            .putLong(KEY_SUB_ID, subscriptionId)
            .putLong(KEY_ID, agentId)
            .putBoolean(KEY_REMEMBER, remember)
            .apply()
    }

    fun logout() {
        val keepPhone = if (rememberMe) agentPhone else ""
        prefs.edit().clear().apply()
        if (keepPhone.isNotBlank()) {
            rememberMe = true
            agentPhone = keepPhone
            // Do not restore role/token — user must verify OTP again.
        }
    }

    fun updateProfile(name: String, phone: String) {
        agentName = name
        agentPhone = phone
    }

    fun bindSubscription(id: Long, name: String? = null) {
        subscriptionId = id
        if (!name.isNullOrBlank()) agentName = name
    }

    companion object {
        private const val PREFS = "session"
        private const val KEY_ID = "agent_id"
        private const val KEY_NAME = "agent_name"
        private const val KEY_PHONE = "agent_phone"
        private const val KEY_ROLE = "role"
        private const val KEY_SUB_ID = "subscription_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_CUSTOMER_ID = "customer_id"
        private const val KEY_REMEMBER = "remember"
        private const val KEY_CUSTOMER_LANGUAGE = "customer_language"
    }
}
