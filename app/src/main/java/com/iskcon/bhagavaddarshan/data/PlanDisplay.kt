package com.iskcon.bhagavaddarshan.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Legacy flyer totals (magazine + postage) used for imported / old subscriptions. */
object LegacyPlanPrices {
    const val ONE_YEAR = 534
    const val TWO_YEAR = 1068
    const val THREE_YEAR = 1702
    const val FIVE_YEAR = 2820
}

private val MonthYearFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH)

fun formatMonthYear(ym: YearMonth): String = ym.format(MonthYearFmt)

fun formatMonthYearFromStart(startMonth: String): String {
    val ym = runCatching { YearMonth.parse(startMonth.take(7)) }.getOrNull() ?: return startMonth
    return formatMonthYear(ym)
}

fun formatMonthYearFromEnd(endDate: String): String {
    val d = runCatching { LocalDate.parse(endDate.take(10)) }.getOrNull()
    if (d != null) return formatMonthYear(YearMonth.from(d))
    val ym = runCatching { YearMonth.parse(endDate.take(7)) }.getOrNull()
    return ym?.let { formatMonthYear(it) } ?: endDate.take(10)
}

fun Subscription.planDurationMonthsResolved(): Int = planDurationMonths(planYears, planMonths)

/** Human plan name for list/detail (6 Months / 1 Year / 2 Years / …). */
fun Subscription.planDisplayName(): String = when (val m = planDurationMonthsResolved()) {
    6 -> "6 Months"
    12 -> "1 Year"
    24, 30 -> "2 Years"
    36 -> "3 Years"
    48 -> "4 Years"
    60 -> "5 Years"
    else -> if (m > 0) "$m Months" else "Plan"
}

fun Subscription.isImportedRecord(): Boolean {
    val method = paymentMethod.lowercase()
    val ref = paymentRef.lowercase()
    val by = registeredBy.lowercase()
    return method.contains("import") ||
        ref.contains("import") ||
        by.contains("import")
}

/**
 * Display price: stored amount when set; otherwise legacy flyer rates for old durations,
 * or current flyer rates for 6 / 12 / 30 month plans.
 */
fun Subscription.displayPriceRupees(): Int {
    if (totalAmount > 0) return totalAmount
    val m = planDurationMonthsResolved()
    val imported = isImportedRecord()
    return when (m) {
        6 -> SubscriptionPlan.SIX_MONTHS.totalRupees
        12 -> if (imported) LegacyPlanPrices.ONE_YEAR else SubscriptionPlan.TWELVE_MONTHS.totalRupees
        24 -> LegacyPlanPrices.TWO_YEAR
        30 -> SubscriptionPlan.THIRTY_MONTHS.totalRupees
        36 -> LegacyPlanPrices.THREE_YEAR
        60 -> LegacyPlanPrices.FIVE_YEAR
        else -> when {
            planYears == 1 -> LegacyPlanPrices.ONE_YEAR
            planYears == 2 -> LegacyPlanPrices.TWO_YEAR
            planYears == 3 -> LegacyPlanPrices.THREE_YEAR
            planYears == 5 -> LegacyPlanPrices.FIVE_YEAR
            else -> 0
        }
    }
}

/** Offer line for detail. New 30-month (2Y+6 free) only; legacy 24-month = none. */
fun Subscription.offerEligibleLabel(): String = when {
    planDurationMonthsResolved() == 30 -> "Extra 6 Months FREE"
    else -> "None"
}

fun Subscription.isExpiredStatus(): Boolean =
    status.contains("expired", true) || status.contains("inactive", true)

fun Subscription.statusLabelShort(): String = when {
    status.contains("active", true) -> "Active"
    status.contains("expired", true) || status.contains("inactive", true) -> "Expired"
    status.contains("pending", true) -> "Pending"
    status.contains("fail", true) -> "Failed"
    else -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

fun Subscription.listDateRangeLabel(): String {
    val start = formatMonthYearFromStart(startMonth)
    val end = formatMonthYearFromEnd(endDate)
    return "$start to $end · ${planDisplayName()}"
}
