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
    val labelTe: String
) {
    ONE_YEAR(1, 450_00, 84_00, 1, "1 సంవత్సరం"),
    TWO_YEARS(2, 900_00, 168_00, 2, "2 సంవత్సరాలు"),
    THREE_YEARS(3, 1450_00, 252_00, 4, "3 సంవత్సరాలు"),
    FIVE_YEARS(5, 2400_00, 420_00, 6, "5 సంవత్సరాలు");

    val totalPaise: Int get() = magazinePaise + postagePaise

    val magazineRupees: Int get() = magazinePaise / 100
    val postageRupees: Int get() = postagePaise / 100
    val totalRupees: Int get() = totalPaise / 100

    companion object {
        fun fromYears(years: Int): SubscriptionPlan =
            entries.firstOrNull { it.years == years }
                ?: error("Unknown plan years: $years")
    }
}
