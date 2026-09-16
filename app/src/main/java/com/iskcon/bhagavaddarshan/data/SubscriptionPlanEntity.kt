package com.iskcon.bhagavaddarshan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class SubscriptionPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val years: Int,
    /** Magazine amount in rupees (new / current price). */
    val magazineAmount: Int,
    /** Postage amount in rupees (new / current price). */
    val postageAmount: Int,
    val giftBooks: Int = 0,
    val labelTe: String = "",
    val active: Boolean = true,
    val sortOrder: Int = 0,
    /** Legacy flyer price in rupees (0 if none). */
    val oldMagazineAmount: Int = 0,
    val oldPostageAmount: Int = 0,
    /** e.g. "6 Months FREE" for 2-year plan. */
    val offerLabel: String = ""
) {
    val totalAmount: Int get() = magazineAmount + postageAmount
    val oldTotalAmount: Int get() = oldMagazineAmount + oldPostageAmount

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
                sortOrder = sortOrder,
                oldMagazineAmount = 0,
                oldPostageAmount = 0,
                offerLabel = if (plan == SubscriptionPlan.THIRTY_MONTHS) "6 Months FREE" else ""
            )
    }
}
