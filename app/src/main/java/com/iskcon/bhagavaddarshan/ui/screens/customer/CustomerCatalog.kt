package com.iskcon.bhagavaddarshan.ui.screens.customer

import com.iskcon.bhagavaddarshan.BuildConfig

data class SacredBook(
    val title: String,
    val subtitle: String,
    val priceRupees: Int,
    val imageModel: Any
)

data class MagazineIssue(
    val year: Int,
    val monthLabel: String,
    val title: String,
    val imageUrl: String
)

/** Large images hosted on Lightsail — keeps the APK small. */
object BdMedia {
    private val root: String =
        BuildConfig.API_BASE_URL.trimEnd('/') + "/media"

    fun url(fileName: String): String = "$root/$fileName"
}

object CustomerCatalog {
    const val LATEST_ISSUE_LABEL = "August 2026 Issue"
    const val LATEST_ISSUE_TITLE = "Jagannatha Ratha Yatra Mahotsavam"
    const val LATEST_ISSUE_BLURB =
        "A special festival edition on Lord Jagannatha's grand chariot procession and its devotional mood."
    val LATEST_COVER: Any = BdMedia.url("magazine_august_2026.png")
    val LOGIN_HERO: Any = BdMedia.url("login_hero.png")
    /** Fresh filename so Coil/CDN cannot keep serving the replaced circular image. */
    val LOGIN_TOP_SECTION: Any = BdMedia.url("login_top_section_original.png?v=old1")
    val HOME_EMBLEM: Any = BdMedia.url("tilak_mark.png")
    val LORD_KRISHNA: Any = BdMedia.url("lord_krishna_standing.png")

    val books = listOf(
        SacredBook(
            title = "Bhagavad Gita",
            subtitle = "As It Is",
            priceRupees = 400,
            imageModel = BdMedia.url("book_bhagavad_gita.png")
        ),
        SacredBook(
            title = "Srimad Bhagavatam",
            subtitle = "Full Set",
            priceRupees = 8500,
            imageModel = BdMedia.url("book_bhagavatam.png")
        ),
        SacredBook(
            title = "Krishna Book",
            subtitle = "The Supreme Personality of Godhead",
            priceRupees = 400,
            imageModel = BdMedia.url("book_krishna.png")
        ),
        SacredBook(
            title = "Sri Chaitanya Charitamrta",
            subtitle = "Full set",
            priceRupees = 3000,
            imageModel = BdMedia.url("book_chaitanya_charitamrta.png")
        ),
        SacredBook(
            title = "Ramayana",
            subtitle = "Pastimes of Lord Rama",
            priceRupees = 400,
            imageModel = BdMedia.url("book_ramayana.webp")
        )
    )

    const val GITA_DAAN_RUPEES = 250

    val magazineYears = listOf(2024, 2023, 2022, 2021)

    val magazines = listOf(
        MagazineIssue(2024, "May 2024", "Glories of Lord Jagannath",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_55f2c10c0f61574a.png"),
        MagazineIssue(2024, "April 2024", "Sri Rama Navami Special",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_dc4a9c87dbb5855a.png"),
        MagazineIssue(2024, "March 2024", "Festival of Gaura Purnima",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_f0a8292c719d7da5.png"),
        MagazineIssue(2024, "February 2024", "The nectar of Holy Name",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_4672ce80d323e54a.png"),
        MagazineIssue(2023, "December 2023", "Gita Jayanti Reflections",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_3c94ba5912_e64ff424c64b9d09.png"),
        MagazineIssue(2023, "August 2023", "Sri Krishna Janmashtami",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_55f2c10c0f61574a.png"),
        MagazineIssue(2022, "November 2022", "Govardhan Puja Special",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_f0a8292c719d7da5.png"),
        MagazineIssue(2021, "January 2021", "New Year in Krishna Consciousness",
            "https://storage.googleapis.com/uxpilot-auth.appspot.com/gen_d3feb95a82_4672ce80d323e54a.png")
    )
}
