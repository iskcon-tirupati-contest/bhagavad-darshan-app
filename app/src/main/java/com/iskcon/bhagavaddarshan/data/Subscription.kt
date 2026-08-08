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
    val planYears: Int,
    val magazineAmount: Int,
    val postageAmount: Int,
    val totalAmount: Int,
    val giftBooks: Int,
    /** First magazine month as yyyy-MM (month after payment). */
    val startMonth: String,
    /** Inclusive end date as yyyy-MM-dd. */
    val endDate: String,
    val status: String = Status.ACTIVE,
    val paymentRef: String = "",
    val collectorName: String = "",
    val source: String = Source.COLLECTOR,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    object Status {
        const val PENDING_PAYMENT = "pending_payment"
        const val ACTIVE = "active"
        const val EXPIRING = "expiring"
        const val EXPIRED = "expired"
    }

    object Source {
        const val COLLECTOR = "collector"
        const val SELF = "self"
        const val ADMIN = "admin"
    }
}
