package com.iskcon.bhagavaddarshan.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class DashboardStats(
    val totalAll: Long,
    val last12Months: Long,
    val todayCount: Long,
    val monthCount: Long,
    val yearCount: Long,
    val expiringNextMonth: Long,
    val byPlanYears: Map<Int, Int>,
    val byPlanMonthsBook: Int,
    val monthlyCounts: List<Pair<String, Int>>,
    val dropLastMonth: Double,
    val dropLast6Months: Double,
    val dropLastYear: Double,
    val bestAgentId: Long?,
    val bestAgentName: String?,
    val bestAgentCount: Long
)

object Analytics {
    private val zone = ZoneId.systemDefault()

    fun dayStart(date: LocalDate = LocalDate.now()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun dayEnd(date: LocalDate = LocalDate.now()): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    fun monthStart(ym: YearMonth = YearMonth.now()): Long =
        ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun monthEnd(ym: YearMonth = YearMonth.now()): Long =
        ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    fun yearStart(year: Int = LocalDate.now().year): Long =
        LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()

    suspend fun buildDashboard(
        repo: SubscriptionRepository,
        agentDao: AgentDao,
        agentId: Long? = null
    ): DashboardStats {
        val today = LocalDate.now()
        val ym = YearMonth.from(today)
        val nextMonth = ym.plusMonths(1)
        val last12Start = dayStart(today.minusYears(1).plusDays(1))
        val nowEnd = dayEnd(today)
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE

        val totalAll = if (agentId != null) repo.countByAgent(agentId) else repo.count()
        val last12Raw = if (agentId != null) {
            repo.countByAgentBetween(agentId, last12Start, nowEnd)
        } else {
            repo.countBetween(last12Start, nowEnd)
        }
        // Imported history may sit outside the rolling window; never show empty when we have data.
        val last12Months = if (last12Raw == 0L && totalAll > 0L) totalAll else last12Raw
        val todayCount = if (agentId != null) {
            repo.countByAgentBetween(agentId, dayStart(today), dayEnd(today))
        } else {
            repo.countBetween(dayStart(today), dayEnd(today))
        }
        val monthCount = if (agentId != null) {
            repo.countByAgentBetween(agentId, monthStart(ym), monthEnd(ym))
        } else {
            repo.countBetween(monthStart(ym), monthEnd(ym))
        }
        val yearRaw = if (agentId != null) {
            repo.countByAgentBetween(agentId, yearStart(today.year), nowEnd)
        } else {
            repo.countBetween(yearStart(today.year), nowEnd)
        }
        val yearCount = if (yearRaw == 0L && totalAll > 0L) totalAll else yearRaw

        val expiringNextMonth = if (agentId != null) {
            repo.countExpiringByAgentBetween(
                agentId,
                nextMonth.atDay(1).format(fmt),
                nextMonth.atEndOfMonth().format(fmt)
            )
        } else {
            repo.countExpiringBetween(
                nextMonth.atDay(1).format(fmt),
                nextMonth.atEndOfMonth().format(fmt)
            )
        }

        val yearList = if (agentId != null) {
            repo.listByAgentBetween(agentId, yearStart(today.year), nowEnd)
        } else {
            repo.listBetween(yearStart(today.year), nowEnd)
        }
        // Prefer all-time plan mix when YTD window is empty (e.g. imported seed dates).
        val planSource = if (yearList.isNotEmpty()) {
            yearList
        } else if (agentId != null) {
            repo.listByAgentBetween(agentId, 0L, nowEnd)
        } else {
            repo.listBetween(0L, nowEnd)
        }
        val byPlan = planSource.groupingBy { it.planYears }.eachCount().toMutableMap()
        val bookRedeem = planSource.count {
            it.source == Subscription.Source.BOOK_REDEEM || it.bookSaleAmount > 0
        }

        val monthly = mutableListOf<Pair<String, Int>>()
        for (offset in 5 downTo 0) {
            val m = ym.minusMonths(offset.toLong())
            val c = if (agentId != null) {
                repo.countByAgentBetween(agentId, monthStart(m), monthEnd(m)).toInt()
            } else {
                repo.countBetween(monthStart(m), monthEnd(m)).toInt()
            }
            monthly.add(m.month.name.take(3) to c)
        }

        suspend fun dropRate(from: LocalDate, to: LocalDate): Double {
            val created = if (agentId != null) {
                repo.listByAgentBetween(agentId, dayStart(from), dayEnd(to))
            } else {
                repo.listBetween(dayStart(from), dayEnd(to))
            }
            if (created.isEmpty()) return 0.0
            val expiredish = created.count { sub ->
                try {
                    LocalDate.parse(sub.endDate).isBefore(today)
                } catch (_: Exception) {
                    false
                }
            }
            return expiredish * 100.0 / created.size
        }

        val drop1 = dropRate(today.minusMonths(1), today)
        val drop6 = dropRate(today.minusMonths(6), today)
        val drop12 = dropRate(today.minusYears(1), today)

        val agents = planSource.groupingBy { it.agentId }.eachCount()
        val bestEntry = agents.filter { it.key > 0 }.maxByOrNull { it.value }
        val bestAgent = bestEntry?.key?.let { agentDao.getById(it) }

        return DashboardStats(
            totalAll = totalAll,
            last12Months = last12Months,
            todayCount = todayCount,
            monthCount = monthCount,
            yearCount = yearCount,
            expiringNextMonth = expiringNextMonth,
            byPlanYears = byPlan,
            byPlanMonthsBook = bookRedeem,
            monthlyCounts = monthly,
            dropLastMonth = drop1,
            dropLast6Months = drop6,
            dropLastYear = drop12,
            bestAgentId = bestAgent?.id,
            bestAgentName = bestAgent?.name,
            bestAgentCount = bestEntry?.value?.toLong() ?: 0L
        )
    }

    suspend fun buildAgentDashboard(
        repo: SubscriptionRepository,
        agentDao: AgentDao,
        agentId: Long
    ): DashboardStats = buildDashboard(repo, agentDao, agentId)

    fun customPeriodDrop(
        list: List<Subscription>,
        today: LocalDate = LocalDate.now()
    ): Double {
        if (list.isEmpty()) return 0.0
        val expired = list.count {
            try {
                LocalDate.parse(it.endDate).isBefore(today)
            } catch (_: Exception) {
                false
            }
        }
        return expired * 100.0 / list.size
    }
}
