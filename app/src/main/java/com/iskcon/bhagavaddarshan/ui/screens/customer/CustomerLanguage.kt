package com.iskcon.bhagavaddarshan.ui.screens.customer

enum class CustomerLanguage {
    ENGLISH,
    TELUGU;

    companion object {
        fun fromStored(value: String): CustomerLanguage =
            if (value.equals("te", true)) TELUGU else ENGLISH
    }

    fun storedValue(): String = if (this == TELUGU) "te" else "en"
}

fun tr(language: CustomerLanguage, english: String, telugu: String): String =
    if (language == CustomerLanguage.TELUGU) telugu else english

fun customerTabLabel(language: CustomerLanguage, key: String): String = when (key) {
    "home" -> tr(language, "Home", "హోమ్")
    "plans" -> tr(language, "Plans", "ప్లాన్స్")
    "magazines" -> tr(language, "Magazines", "మ్యాగజైన్స్")
    "help" -> tr(language, "Help", "సహాయం")
    "profile" -> tr(language, "Profile", "ప్రొఫైల్")
    else -> key
}
