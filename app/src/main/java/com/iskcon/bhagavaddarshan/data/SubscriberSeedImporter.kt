package com.iskcon.bhagavaddarshan.data

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SubscriberSeedImporter(
    private val context: Context,
    private val dao: SubscriptionDao
) {
    suspend fun importIfNeeded(): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DONE, false)) return 0
        if (dao.count() > 0) {
            prefs.edit().putBoolean(KEY_DONE, true).apply()
            return 0
        }
        val imported = importAll()
        prefs.edit().putBoolean(KEY_DONE, true).apply()
        return imported
    }

    private suspend fun importAll(): Int {
        val json = context.assets.open(ASSET).bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        if (arr.length() == 0) return 0

        var receipt = dao.maxReceiptNo()
        val startMonth = YearMonth.of(2025, 7)
        val items = ArrayList<Subscription>(arr.length())

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val years = o.optInt("planYears", 1)
            val plan = SubscriptionPlan.fromYears(years)
            val endDate = startMonth.plusYears(plan.years.toLong()).minusMonths(1).atEndOfMonth()
            receipt += 1
            // Spread historical import across recent months so Home analytics aren't all zeros.
            val createdDay = LocalDate.of(2026, 1, 1).plusDays((i % 200).toLong())
            val address = o.optString("address")
            items.add(
                Subscription(
                    receiptNo = receipt,
                    name = o.optString("name").ifBlank { "Unknown" },
                    houseNo = o.optString("houseNo"),
                    street = o.optString("street"),
                    villageTown = o.optString("villageTown").ifBlank { address.take(80) },
                    mandal = o.optString("mandal"),
                    district = o.optString("district"),
                    pincode = o.optString("pincode"),
                    state = o.optString("state").ifBlank { "Andhra Pradesh" },
                    phone = o.optString("phone").ifBlank { "0000000000" },
                    planYears = plan.years,
                    planMonths = plan.years * 12,
                    magazineAmount = plan.magazineRupees,
                    postageAmount = plan.postageRupees,
                    totalAmount = plan.totalRupees,
                    giftBooks = plan.giftBooks,
                    startMonth = startMonth.toString(),
                    endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    status = Subscription.Status.ACTIVE,
                    paymentRef = "IMPORTED_PDF",
                    paymentMethod = "imported",
                    collectorName = "",
                    agentId = 0,
                    registeredBy = "self",
                    source = Subscription.Source.SELF,
                    notes = "Imported from subscribers new.pdf (page ${o.optInt("page")})",
                    createdAt = createdDay.atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                )
            )
        }
        dao.insertAll(items)
        return items.size
    }

    companion object {
        private const val PREFS = "seed"
        private const val KEY_DONE = "subscribers_pdf_imported_v3"
        private const val ASSET = "subscribers_seed.json"
    }
}
