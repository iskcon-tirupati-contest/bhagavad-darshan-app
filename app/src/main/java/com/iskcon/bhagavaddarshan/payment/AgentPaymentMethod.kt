package com.iskcon.bhagavaddarshan.payment

enum class AgentPaymentMethod(val routeKey: String, val label: String) {
    DYNAMIC_QR("dynamic", "Razorpay UPI QR"),
    CASH("cash", "Cash by hand"),
    BOOK_REDEEM("books", "Redeem by book sales");

    companion object {
        fun fromRoute(key: String): AgentPaymentMethod =
            entries.firstOrNull { it.routeKey == key } ?: DYNAMIC_QR
    }
}

/** ₹1000 books = 1 month magazine; multiples of 1000. */
object BookRedeemCalculator {
    fun monthsForSale(amountRupees: Int): Int = (amountRupees / 1000).coerceAtLeast(0)

    fun isEligible(amountRupees: Int): Boolean = amountRupees >= 1000
}
