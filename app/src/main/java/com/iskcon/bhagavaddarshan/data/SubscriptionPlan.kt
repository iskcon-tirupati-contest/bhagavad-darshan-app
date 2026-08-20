package com.iskcon.bhagavaddarshan.data

/**
 * Official flyer pricing for Bhagavad Darshan magazine.
 * Receipt-book legacy prices are not used.
 */
enum class SubscriptionPlan(
    val years: Int,
    val magazinePaise: Int,
    val postagePaise: Int,
    val giftBooks: Int,
    /** English display label. */
    val labelEn: String
) {
    ONE_YEAR(1, 450_00, 84_00, 1, "1 year"),
    TWO_YEARS(2, 900_00, 168_00, 2, "2 years"),
    THREE_YEARS(3, 1450_00, 252_00, 4, "3 years"),
    /** Rare legacy PDF entries; flyer has no 4-year tier — pro-rated from 5-year. */
    FOUR_YEARS(4, 1920_00, 336_00, 5, "4 years"),
    FIVE_YEARS(5, 2400_00, 420_00, 6, "5 years");

    val totalPaise: Int get() = magazinePaise + postagePaise

    val magazineRupees: Int get() = magazinePaise / 100
    val postageRupees: Int get() = postagePaise / 100
    val totalRupees: Int get() = totalPaise / 100

    val title: String get() = "$years year plan"
    val priceAmount: Int get() = totalRupees
    val description: String
        get() = "Magazine ₹$magazineRupees + postage ₹$postageRupees · $giftBooks gift book(s)"
    val isPopular: Boolean get() = years == 3

    /** @deprecated Use [labelEn]; kept for call-site compatibility. */
    val labelTe: String get() = labelEn

    companion object {
        fun fromYears(years: Int): SubscriptionPlan =
            entries.firstOrNull { it.years == years } ?: ONE_YEAR
    }
}
