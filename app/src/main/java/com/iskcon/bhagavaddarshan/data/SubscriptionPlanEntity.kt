package com.iskcon.bhagavaddarshan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class SubscriptionPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val years: Int,
    /** Magazine amount in rupees. */
    val magazineAmount: Int,
    /** Postage amount in rupees. */
    val postageAmount: Int,
    val giftBooks: Int = 0,
    val labelTe: String = "",
    val active: Boolean = true,
    val sortOrder: Int = 0
) {
    val totalAmount: Int get() = magazineAmount + postageAmount

    /** Map DB row to flyer enum (fallback by years when amounts differ). */
    fun toSubscriptionPlan(): SubscriptionPlan =
        SubscriptionPlan.entries.firstOrNull { it.years == years }
            ?: SubscriptionPlan.TWELVE_MONTHS

    companion object {
        fun fromEnum(plan: SubscriptionPlan, sortOrder: Int = plan.years): SubscriptionPlanEntity =
            SubscriptionPlanEntity(
                years = plan.years,
                magazineAmount = plan.magazineRupees,
                postageAmount = plan.postageRupees,
                giftBooks = plan.giftBooks,
                labelTe = plan.labelEn,
                active = true,
                sortOrder = sortOrder
            )
    }
}
