package com.iskcon.bhagavaddarshan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNo: Long,
    val name: String,
    val houseNo: String = "",
    val street: String = "",
    val villageTown: String = "",
    val mandal: String = "",
    val district: String = "",
    val pincode: String = "",
    val state: String = "Andhra Pradesh",
    val phone: String,
    /** Flyer plan years (0 when book-redeem months-only). */
    val planYears: Int = 0,
    /** Duration in months (years*12 or book redeem months). */
    val planMonths: Int = 12,
    val magazineAmount: Int = 0,
    val postageAmount: Int = 0,
    val totalAmount: Int = 0,
    val giftBooks: Int = 0,
    val bookSaleAmount: Int = 0,
    val startMonth: String,
    val endDate: String,
    val status: String = Status.ACTIVE,
    val paymentRef: String = "",
    val paymentMethod: String = "",
    val paymentProofPath: String = "",
    val collectorName: String = "",
    val agentId: Long = 0,
    /** "self" for website/PDF import; agent display name when an agent registers. */
    val registeredBy: String = "self",
    val razorpayQrId: String = "",
    val source: String = Source.COLLECTOR,
    val notes: String = "",
    /** Epoch ms when 1-month WhatsApp expiry reminder was sent (0 = never). */
    val expiryReminderSentAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    object Status {
        const val PENDING_PAYMENT = "pending_payment"
        const val ACTIVE = "active"
        const val EXPIRING = "expiring"
        const val EXPIRED = "expired"
        const val PAYMENT_FAILED = "payment_failed"
    }

    object Source {
        const val COLLECTOR = "collector"
        const val SELF = "self"
        const val ADMIN = "admin"
        const val BOOK_REDEEM = "book_redeem"
    }
}
