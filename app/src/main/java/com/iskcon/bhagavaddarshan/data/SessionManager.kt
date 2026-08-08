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

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER, value).apply()

    val isLoggedIn: Boolean get() = agentId > 0L
    val isAdmin: Boolean get() = role == Agent.Role.ADMIN

    fun login(agent: Agent, remember: Boolean) {
        agentId = agent.id
        agentName = agent.name
        agentPhone = agent.phone
        role = agent.role
        rememberMe = remember
    }

    fun logout() {
        val keepRemember = rememberMe
        val phone = if (keepRemember) agentPhone else ""
        prefs.edit().clear().apply()
        if (keepRemember && phone.isNotBlank()) {
            rememberMe = true
            agentPhone = phone
        }
    }

    fun updateProfile(name: String, phone: String) {
        agentName = name
        agentPhone = phone
    }

    companion object {
        private const val PREFS = "session"
        private const val KEY_ID = "agent_id"
        private const val KEY_NAME = "agent_name"
        private const val KEY_PHONE = "agent_phone"
        private const val KEY_ROLE = "role"
        private const val KEY_REMEMBER = "remember"
    }
}
