package com.iskcon.bhagavaddarshan.util

object FormValidators {
    fun phone(value: String): String? {
        val p = value.filter(Char::isDigit)
        return when {
            p.length != 10 -> "Enter a valid 10-digit mobile number"
            p[0] !in '6'..'9' -> "Mobile number must start with 6–9"
            else -> null
        }
    }

    fun required(value: String, label: String): String? =
        if (value.isBlank()) "$label is required" else null

    fun pincode(value: String): String? {
        if (value.isBlank()) return null
        return if (value.length != 6 || value.any { !it.isDigit() }) "Pincode must be 6 digits" else null
    }

    fun password(value: String): String? =
        when {
            value.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }

    fun name(value: String): String? =
        when {
            value.isBlank() -> "Name is required"
            value.trim().length < 2 -> "Enter a valid name"
            else -> null
        }
}
