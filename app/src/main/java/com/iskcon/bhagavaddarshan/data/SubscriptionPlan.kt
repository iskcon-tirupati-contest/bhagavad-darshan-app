package com.iskcon.bhagavaddarshan.data

/**
 * Official Bhagavad Darshan magazine plans (months-based flyer pricing).
 * [years] is the API plan key (same numeric value as duration months for these tiers).
 */
enum class SubscriptionPlan(
    val years: Int,
    val months: Int,
    val magazinePaise: Int,
    val postagePaise: Int,
    val giftBooks: Int,
    /** English display label. */
    val labelEn: String
) {
    SIX_MONTHS(6, 6, 299_00, 0, 0, "6 Months"),
    TWELVE_MONTHS(12, 12, 599_00, 0, 0, "12 Months"),
    THIRTY_MONTHS(30, 30, 1199_00, 0, 0, "30 Months");

    val totalPaise: Int get() = magazinePaise + postagePaise

    val magazineRupees: Int get() = magazinePaise / 100
    val postageRupees: Int get() = postagePaise / 100
    val totalRupees: Int get() = totalPaise / 100

    val title: String get() = "$months month plan"
    val priceAmount: Int get() = totalRupees
    val description: String
        get() = when (this) {
            THIRTY_MONTHS -> "24 Months + 6 Months FREE · ₹$totalRupees"
            else -> "₹$totalRupees for $months months"
        }
    val isPopular: Boolean get() = this == THIRTY_MONTHS

    /** @deprecated Use [labelEn]; kept for call-site compatibility. */
    val labelTe: String get() = labelEn

    companion object {
        fun fromYears(years: Int): SubscriptionPlan =
            entries.firstOrNull { it.years == years } ?: TWELVE_MONTHS

        /** Active flyer plans only. */
        val activePlans: List<SubscriptionPlan> = entries.toList()
    }
}

/** Duration in months for plan_years / plan_months (supports legacy year keys). */
fun planDurationMonths(planYears: Int, planMonths: Int = 0): Int = when {
    planYears >= 6 -> planYears
    planMonths > 0 -> planMonths
    planYears > 0 -> planYears * 12
    else -> 12
}

fun planDisplayLabel(planYears: Int, planMonths: Int = 0): String {
    val m = planDurationMonths(planYears, planMonths)
    return if (m > 0) "$m Month Plan" else "Magazine Plan"
}
