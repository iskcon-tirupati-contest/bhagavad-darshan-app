package com.iskcon.bhagavaddarshan.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SubscriptionRepository(private val dao: SubscriptionDao) {

    fun observeAll(): Flow<List<Subscription>> = dao.observeAll()

    fun observeByAgent(agentId: Long): Flow<List<Subscription>> = dao.observeByAgent(agentId)

    fun observePending(): Flow<List<Subscription>> = dao.observePending()

    fun observePendingForAgent(agentId: Long): Flow<List<Subscription>> =
        dao.observePendingForAgent(agentId)

    fun search(query: String): Flow<List<Subscription>> =
        if (query.isBlank()) dao.observeAll() else dao.search(query.trim())

    fun searchAll(query: String): Flow<List<Subscription>> =
        if (query.isBlank()) dao.observeAll() else dao.searchAll(query.trim())

    fun searchForAgent(agentId: Long, query: String): Flow<List<Subscription>> =
        if (query.isBlank()) dao.observeByAgent(agentId)
        else dao.searchForAgent(agentId, query.trim())

    fun observeExpiringSoon(withinDays: Long = 30): Flow<List<Subscription>> {
        val cutoff = LocalDate.now().plusDays(withinDays)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dao.observeExpiringBefore(cutoff)
    }

    suspend fun count(): Long = dao.count()

    suspend fun countByAgent(agentId: Long): Long = dao.countByAgent(agentId)

    suspend fun getById(id: Long): Subscription? = dao.getById(id)

    suspend fun countBetween(from: Long, to: Long): Long = dao.countBetween(from, to)

    suspend fun countByAgentBetween(agentId: Long, from: Long, to: Long): Long =
        dao.countByAgentBetween(agentId, from, to)

    suspend fun listBetween(from: Long, to: Long): List<Subscription> = dao.listBetween(from, to)

    suspend fun listByAgentBetween(agentId: Long, from: Long, to: Long): List<Subscription> =
        dao.listByAgentBetween(agentId, from, to)

    suspend fun countExpiringBetween(fromDate: String, toDate: String): Long =
        dao.countExpiringBetween(fromDate, toDate)

    suspend fun countExpiringByAgentBetween(
        agentId: Long,
        fromDate: String,
        toDate: String
    ): Long = dao.countExpiringByAgentBetween(agentId, fromDate, toDate)

    suspend fun markPaid(
        id: Long,
        paymentId: String,
        paymentMethod: String = "",
        proofPath: String = ""
    ) {
        val current = dao.getById(id) ?: return
        dao.update(
            current.copy(
                status = Subscription.Status.ACTIVE,
                paymentRef = paymentId,
                paymentMethod = paymentMethod.ifBlank { current.paymentMethod },
                paymentProofPath = proofPath.ifBlank { current.paymentProofPath }
            )
        )
    }

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
        plan: SubscriptionPlan? = null,
        planMonths: Int? = null,
        bookSaleAmount: Int = 0,
        paymentRef: String,
        paymentMethod: String,
        paymentProofPath: String = "",
        collectorName: String,
        agentId: Long,
        registeredBy: String = "self",
        notes: String = "",
        paymentDate: LocalDate = LocalDate.now(),
        source: String = Subscription.Source.COLLECTOR
    ): Long {
        val months = when {
            planMonths != null && planMonths > 0 -> planMonths
            plan != null -> plan.years * 12
            else -> 12
        }
        val years = plan?.years ?: (months / 12)
        val magazine = plan?.magazineRupees ?: 0
        val postage = plan?.postageRupees ?: 0
        val total = when {
            bookSaleAmount > 0 -> bookSaleAmount
            plan != null -> plan.totalRupees
            else -> 0
        }
        val gifts = plan?.giftBooks ?: 0

        val receiptNo = dao.maxReceiptNo() + 1
        val startMonth = YearMonth.from(paymentDate).plusMonths(1)
        val endDate = startMonth.plusMonths(months.toLong()).minusMonths(1).atEndOfMonth()

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
            planYears = years,
            planMonths = months,
            magazineAmount = magazine,
            postageAmount = postage,
            totalAmount = total,
            giftBooks = gifts,
            bookSaleAmount = bookSaleAmount,
            startMonth = startMonth.toString(),
            endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            status = if (paymentRef.isBlank()) Subscription.Status.PENDING_PAYMENT
            else Subscription.Status.ACTIVE,
            paymentRef = paymentRef.trim(),
            paymentMethod = paymentMethod,
            paymentProofPath = paymentProofPath,
            collectorName = collectorName.trim(),
            agentId = agentId,
            registeredBy = registeredBy.trim().ifBlank { "self" },
            source = source,
            notes = notes.trim()
        )
        return dao.insert(subscription)
    }

    suspend fun update(subscription: Subscription) = dao.update(subscription)

    suspend fun delete(id: Long) = dao.deleteById(id)
}
