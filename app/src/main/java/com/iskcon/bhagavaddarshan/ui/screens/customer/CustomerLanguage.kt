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
    "magazines" -> tr(language, "Past", "గతం")
    "help" -> tr(language, "Help", "సహాయం")
    "profile" -> tr(language, "Profile", "ప్రొఫైల్")
    else -> key
}

fun customerPageTitle(language: CustomerLanguage, tab: String): String = when (tab.lowercase()) {
    "home" -> tr(language, "Dashboard", "డాష్‌బోర్డ్")
    "plans" -> tr(language, "Plans", "ప్లాన్స్")
    "magazines" -> tr(language, "Past Magazines", "గత మ్యాగజైన్స్")
    "help" -> tr(language, "Help & Contact", "సహాయం & సంప్రదింపు")
    "profile" -> tr(language, "Profile", "ప్రొఫైల్")
    else -> tab
}
