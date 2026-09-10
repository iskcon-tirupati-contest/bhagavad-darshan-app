package com.iskcon.bhagavaddarshan.data

import com.iskcon.bhagavaddarshan.network.BdApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Cloud-backed repository (Lightsail Postgres via API). Room is no longer the source of truth.
 */
class SubscriptionRepository(
    private val api: BdApi,
    private val session: SessionManager
) {
    private val all = MutableStateFlow<List<Subscription>>(emptyList())
    private val pending = MutableStateFlow<List<Subscription>>(emptyList())
    private val expiring = MutableStateFlow<List<Subscription>>(emptyList())

    private fun token(): String = session.authToken

    suspend fun refreshAll(agentId: Long? = null, query: String = "") {
        if (token().isBlank()) return
        api.listSubscriptions(
            token = token(),
            q = query,
            agentId = agentId
        ).onSuccess { json ->
            all.value = json.toSubscriptionList()
        }
    }

    suspend fun refreshPending(agentId: Long? = null) {
        if (token().isBlank()) return
        api.listSubscriptions(
            token = token(),
            status = Subscription.Status.PENDING_PAYMENT,
            agentId = agentId
        ).onSuccess { pending.value = it.toSubscriptionList() }
    }

    suspend fun refreshExpiring(withinDays: Long = 30) {
        if (token().isBlank()) return
        api.listSubscriptions(
            token = token(),
            expiringDays = withinDays.toInt()
        ).onSuccess { expiring.value = it.toSubscriptionList() }
    }

    fun observeAll(): Flow<List<Subscription>> = all

    fun observeByAgent(agentId: Long): Flow<List<Subscription>> =
        all.map { list -> list.filter { it.agentId == agentId } }

    fun observePending(): Flow<List<Subscription>> = pending

    fun observePendingForAgent(agentId: Long): Flow<List<Subscription>> =
        pending.map { list -> list.filter { it.agentId == agentId } }

    fun search(query: String): Flow<List<Subscription>> = all

    fun searchAll(query: String): Flow<List<Subscription>> = all

    fun searchForAgent(agentId: Long, query: String): Flow<List<Subscription>> =
        all.map { list -> list.filter { it.agentId == agentId } }

    fun observeExpiringSoon(withinDays: Long = 30): Flow<List<Subscription>> = expiring

    suspend fun count(): Long = all.value.size.toLong()

    suspend fun countByAgent(agentId: Long): Long =
        all.value.count { it.agentId == agentId }.toLong()

    suspend fun getById(id: Long): Subscription? {
        if (token().isBlank()) return all.value.find { it.id == id }
        return api.getSubscription(token(), id).getOrNull()?.optJSONObject("subscription")
            ?.toSubscription()
            ?: all.value.find { it.id == id }
    }

    fun observeById(id: Long): Flow<Subscription?> =
        all.map { list -> list.find { it.id == id } }

    suspend fun findLatestByPhone(phone: String): Subscription? {
        val p = phone.filter(Char::isDigit).takeLast(10)
        refreshAll(query = p)
        return all.value.filter { it.phone == p }.maxByOrNull { it.id }
    }

    suspend fun listNeedingExpiryReminder(withinDays: Long = 30): List<Subscription> {
        refreshExpiring(withinDays)
        return expiring.value.filter { it.expiryReminderSentAt == 0L }
    }

    suspend fun markExpiryReminderSent(id: Long) {
        // Server-side reminder worker preferred; no-op locally for now.
    }

    suspend fun extendPlan(
        id: Long,
        plan: SubscriptionPlan,
        paymentRef: String,
        paymentMethod: String,
        razorpayQrId: String = ""
    ) {
        val current = getById(id) ?: return
        val months = plan.months
        val currentEnd = runCatching { LocalDate.parse(current.endDate.take(10)) }.getOrNull()
        val base = when {
            currentEnd != null && currentEnd.isAfter(LocalDate.now()) -> currentEnd
            else -> LocalDate.now()
        }
        val startYm = YearMonth.from(base.plusDays(1))
        val endDate = startYm.plusMonths(months.toLong()).minusMonths(1).atEndOfMonth()
        update(
            current.copy(
                planYears = plan.years,
                planMonths = months,
                magazineAmount = plan.magazineRupees,
                postageAmount = plan.postageRupees,
                totalAmount = plan.totalRupees,
                giftBooks = plan.giftBooks,
                endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                status = Subscription.Status.ACTIVE,
                paymentRef = paymentRef,
                paymentMethod = paymentMethod,
                razorpayQrId = razorpayQrId.ifBlank { current.razorpayQrId },
                expiryReminderSentAt = 0L
            )
        )
    }

    suspend fun createSelfServicePending(
        name: String,
        phone: String,
        plan: SubscriptionPlan,
        houseNo: String = "",
        street: String = "",
        villageTown: String = "",
        mandal: String = "",
        district: String = "",
        pincode: String = "",
        state: String = "Andhra Pradesh",
        razorpayQrId: String = ""
    ): Long {
        return register(
            name = name.ifBlank { "Devotee" },
            houseNo = houseNo,
            street = street,
            villageTown = villageTown,
            mandal = mandal,
            district = district,
            pincode = pincode,
            state = state,
            phone = phone,
            plan = plan,
            paymentRef = "",
            paymentMethod = "upi_intent",
            status = Subscription.Status.PENDING_PAYMENT,
            razorpayQrId = razorpayQrId,
            source = Subscription.Source.SELF,
            registeredBy = "self"
        )
    }

    suspend fun register(
        name: String,
        houseNo: String = "",
        street: String = "",
        villageTown: String = "",
        mandal: String = "",
        district: String = "",
        pincode: String = "",
        state: String = "Andhra Pradesh",
        phone: String,
        plan: SubscriptionPlan? = null,
        planMonths: Int = plan?.months ?: 12,
        bookSaleAmount: Int = 0,
        paymentRef: String = "",
        paymentMethod: String = "",
        paymentProofPath: String = "",
        collectorName: String = "",
        agentId: Long = 0,
        registeredBy: String = collectorName.ifBlank { "self" },
        source: String = Subscription.Source.COLLECTOR,
        status: String = if (paymentRef.isBlank()) Subscription.Status.PENDING_PAYMENT else Subscription.Status.ACTIVE,
        razorpayQrId: String = "",
        notes: String = ""
    ): Long {
        val years = plan?.years ?: 0
        val start = LocalDate.now()
        val startMonth = YearMonth.from(start).toString()
        val end = if (planMonths > 0) {
            YearMonth.from(start).plusMonths(planMonths.toLong()).minusMonths(1).atEndOfMonth()
        } else start.plusYears(1)
        val body = JSONObject()
            .put("name", name)
            .put("houseNo", houseNo)
            .put("street", street)
            .put("villageTown", villageTown)
            .put("district", district)
            .put("pincode", pincode)
            .put("state", state)
            .put("phone", phone.filter(Char::isDigit).takeLast(10))
            .put("planYears", years)
            .put("planMonths", planMonths)
            .put("magazineAmount", plan?.magazineRupees ?: 0)
            .put("postageAmount", plan?.postageRupees ?: 0)
            .put("totalAmount", plan?.totalRupees ?: bookSaleAmount)
            .put("giftBooks", plan?.giftBooks ?: 0)
            .put("bookSaleAmount", bookSaleAmount)
            .put("startMonth", startMonth)
            .put("endDate", end.toString())
            .put("status", status)
            .put("paymentRef", paymentRef)
            .put("paymentMethod", paymentMethod)
            .put("paymentProofPath", paymentProofPath)
            .put("collectorName", collectorName)
            .put("agentId", agentId)
            .put("registeredBy", registeredBy)
            .put("razorpayQrId", razorpayQrId)
            .put("source", source)
            .put("notes", notes)
        val json = api.createSubscription(token(), body).getOrThrow()
        val sub = json.optJSONObject("subscription")?.toSubscription()
            ?: error("Create subscription failed")
        refreshAll()
        refreshPending()
        return sub.id
    }

    suspend fun update(subscription: Subscription) {
        val body = subscription.toJsonBody()
        api.updateSubscription(token(), subscription.id, body).getOrThrow()
        refreshAll()
        refreshPending()
        refreshExpiring()
    }

    suspend fun markPaid(
        id: Long,
        paymentRef: String,
        paymentMethod: String = "",
        proofPath: String = ""
    ) {
        api.markSubscriptionPaid(
            token = token(),
            id = id,
            paymentRef = paymentRef,
            paymentMethod = paymentMethod,
            razorpayQrId = ""
        ).getOrThrow()
        refreshAll()
        refreshPending()
    }

    suspend fun delete(id: Long) {
        api.deleteSubscription(token(), id).getOrThrow()
        refreshAll()
        refreshPending()
        refreshExpiring()
    }

    suspend fun countExpiringNextMonth(): Long {
        refreshExpiring(45)
        return expiring.value.size.toLong()
    }

    suspend fun countBetween(fromMs: Long, toMs: Long): Long {
        refreshAll()
        return all.value.size.toLong()
    }

    suspend fun countByAgentBetween(agentId: Long, fromMs: Long, toMs: Long): Long {
        refreshAll(agentId = agentId)
        return all.value.count { it.agentId == agentId }.toLong()
    }

    suspend fun countExpiringBetween(fromDate: String, toDate: String): Long {
        refreshExpiring(60)
        return expiring.value.count {
            it.endDate >= fromDate && it.endDate <= toDate
        }.toLong()
    }

    suspend fun countExpiringByAgentBetween(
        agentId: Long,
        fromDate: String,
        toDate: String
    ): Long {
        refreshExpiring(60)
        return expiring.value.count {
            it.agentId == agentId && it.endDate >= fromDate && it.endDate <= toDate
        }.toLong()
    }

    suspend fun listByAgentBetween(agentId: Long, fromMs: Long, toMs: Long): List<Subscription> {
        refreshAll(agentId = agentId)
        return all.value.filter { it.agentId == agentId }
    }

    suspend fun listBetween(fromMs: Long, toMs: Long): List<Subscription> {
        refreshAll()
        return all.value
    }
}

fun JSONObject.toSubscriptionList(): List<Subscription> {
    val arr = optJSONArray("subscriptions") ?: return emptyList()
    return buildList {
        for (i in 0 until arr.length()) {
            add(arr.getJSONObject(i).toSubscription())
        }
    }
}

fun JSONObject.toSubscription(): Subscription {
    fun s(vararg keys: String): String {
        keys.forEach { k ->
            val v = optString(k)
            if (v.isNotBlank() && v != "null") return v
        }
        return ""
    }
    fun i(vararg keys: String): Int {
        keys.forEach { k -> if (has(k) && !isNull(k)) return optInt(k) }
        return 0
    }
    fun l(vararg keys: String): Long {
        keys.forEach { k -> if (has(k) && !isNull(k)) return optLong(k) }
        return 0L
    }
    return Subscription(
        id = l("id"),
        receiptNo = l("receipt_no", "receiptNo"),
        name = s("name"),
        houseNo = s("house_no", "houseNo"),
        street = s("street"),
        villageTown = s("village_town", "villageTown"),
        mandal = "",
        district = s("district"),
        pincode = s("pincode"),
        state = s("state").ifBlank { "Andhra Pradesh" },
        phone = s("phone"),
        planYears = i("plan_years", "planYears"),
        planMonths = i("plan_months", "planMonths"),
        magazineAmount = i("magazine_amount", "magazineAmount"),
        postageAmount = i("postage_amount", "postageAmount"),
        totalAmount = i("total_amount", "totalAmount"),
        giftBooks = i("gift_books", "giftBooks"),
        bookSaleAmount = i("book_sale_amount", "bookSaleAmount"),
        startMonth = s("start_month", "startMonth"),
        endDate = s("end_date", "endDate"),
        status = s("status").ifBlank { Subscription.Status.ACTIVE },
        paymentRef = s("payment_ref", "paymentRef"),
        paymentMethod = s("payment_method", "paymentMethod"),
        paymentProofPath = s("payment_proof_path", "paymentProofPath"),
        collectorName = s("collector_name", "collectorName"),
        agentId = l("agent_id", "agentId"),
        registeredBy = s("registered_by", "registeredBy").ifBlank { "self" },
        razorpayQrId = s("razorpay_qr_id", "razorpayQrId"),
        source = s("source").ifBlank { Subscription.Source.COLLECTOR },
        notes = s("notes"),
        expiryReminderSentAt = 0L,
        createdAt = System.currentTimeMillis()
    )
}

fun Subscription.toJsonBody(): JSONObject = JSONObject()
    .put("name", name)
    .put("houseNo", houseNo)
    .put("street", street)
    .put("villageTown", villageTown)
    .put("district", district)
    .put("pincode", pincode)
    .put("state", state)
    .put("phone", phone)
    .put("planYears", planYears)
    .put("planMonths", planMonths)
    .put("magazineAmount", magazineAmount)
    .put("postageAmount", postageAmount)
    .put("totalAmount", totalAmount)
    .put("giftBooks", giftBooks)
    .put("bookSaleAmount", bookSaleAmount)
    .put("startMonth", startMonth)
    .put("endDate", endDate)
    .put("status", status)
    .put("paymentRef", paymentRef)
    .put("paymentMethod", paymentMethod)
    .put("paymentProofPath", paymentProofPath)
    .put("collectorName", collectorName)
    .put("agentId", agentId)
    .put("registeredBy", registeredBy)
    .put("razorpayQrId", razorpayQrId)
    .put("source", source)
    .put("notes", notes)
