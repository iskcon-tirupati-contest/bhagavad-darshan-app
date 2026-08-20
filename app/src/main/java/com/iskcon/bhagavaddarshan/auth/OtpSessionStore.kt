package com.iskcon.bhagavaddarshan.auth

import kotlin.random.Random

/**
 * In-memory OTP for registered-account WhatsApp login (device-local; not a server).
 */
object OtpSessionStore {
    private data class Entry(
        val code: String,
        val expiresAtMs: Long,
        var attempts: Int = 0
    )

    private val lock = Any()
    private val byPhone = mutableMapOf<String, Entry>()

    private const val TTL_MS = 5 * 60 * 1000L
    private const val MAX_ATTEMPTS = 5

    fun issue(phone10: String): String {
        val code = Random.nextInt(100000, 999999).toString()
        synchronized(lock) {
            byPhone[norm(phone10)] = Entry(code, System.currentTimeMillis() + TTL_MS)
        }
        return code
    }

    fun verify(phone10: String, code: String): Boolean {
        synchronized(lock) {
            val key = norm(phone10)
            val entry = byPhone[key] ?: return false
            if (System.currentTimeMillis() > entry.expiresAtMs) {
                byPhone.remove(key)
                return false
            }
            if (entry.attempts >= MAX_ATTEMPTS) {
                byPhone.remove(key)
                return false
            }
            entry.attempts++
            if (entry.code != code.trim()) return false
            byPhone.remove(key)
            return true
        }
    }

    private fun norm(phone: String) = phone.filter(Char::isDigit).takeLast(10)
}
