package com.iskcon.bhagavaddarshan.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SubscriptionRepository(private val dao: SubscriptionDao) {

    fun observeAll(): Flow<List<Subscription>> = dao.observeAll()

    fun search(query: String): Flow<List<Subscription>> =
        if (query.isBlank()) dao.observeAll() else dao.search(query.trim())

    fun observeExpiringSoon(withinDays: Long = 30): Flow<List<Subscription>> {
        val cutoff = LocalDate.now().plusDays(withinDays)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dao.observeExpiringBefore(cutoff)
    }

    suspend fun getById(id: Long): Subscription? = dao.getById(id)

    suspend fun register(
        name: String,
        houseNo: String,
        street: String,
        villageTown: String,
        mandal: String,
        district: String,
        pincode: String,
        state: String,
        phone: String,
        plan: SubscriptionPlan,
        paymentRef: String,
        collectorName: String,
        notes: String = "",
        paymentDate: LocalDate = LocalDate.now()
    ): Long {
        val receiptNo = dao.maxReceiptNo() + 1
        val startMonth = YearMonth.from(paymentDate).plusMonths(1)
        val endDate = startMonth.plusYears(plan.years.toLong()).minusMonths(1)
            .atEndOfMonth()

        val subscription = Subscription(
            receiptNo = receiptNo,
            name = name.trim(),
            houseNo = houseNo.trim(),
            street = street.trim(),
            villageTown = villageTown.trim(),
            mandal = mandal.trim(),
            district = district.trim(),
            pincode = pincode.trim(),
            state = state.trim(),
            phone = phone.trim(),
            planYears = plan.years,
            magazineAmount = plan.magazineRupees,
            postageAmount = plan.postageRupees,
            totalAmount = plan.totalRupees,
            giftBooks = plan.giftBooks,
            startMonth = startMonth.toString(),
            endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            status = if (paymentRef.isBlank()) Subscription.Status.PENDING_PAYMENT
            else Subscription.Status.ACTIVE,
            paymentRef = paymentRef.trim(),
            collectorName = collectorName.trim(),
            source = Subscription.Source.COLLECTOR,
            notes = notes.trim()
        )
        return dao.insert(subscription)
    }

    suspend fun update(subscription: Subscription) = dao.update(subscription)

    suspend fun delete(id: Long) = dao.deleteById(id)
}
