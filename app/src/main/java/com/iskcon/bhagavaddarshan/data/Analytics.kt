package com.iskcon.bhagavaddarshan.data

import com.iskcon.bhagavaddarshan.network.BdApi

data class PlanBreakdown(
    val months: Int,
    val label: String,
    val count: Int
)

data class DashboardStats(
    val totalAll: Long,
    val last12Months: Long = 0L,
    val todayCount: Long,
    val weekCount: Long = 0L,
    val monthCount: Long,
    val yearCount: Long,
    val expiringNextMonth: Long = 0L,
    val selectedYear: Int = 0,
    val availableYears: List<Int> = emptyList(),
    val byPlanYears: Map<Int, Int> = emptyMap(),
    val byPlan: List<PlanBreakdown> = emptyList(),
    val byPlanMonthsBook: Int = 0,
    val monthlyCounts: List<Pair<String, Int>> = emptyList(),
    val dropLastMonth: Double = 0.0,
    val dropLast6Months: Double = 0.0,
    val dropLastYear: Double = 0.0,
    val bestAgentId: Long? = null,
    val bestAgentName: String? = null,
    val bestAgentCount: Long = 0L
)

object Analytics {
    suspend fun buildDashboard(
        api: BdApi,
        token: String,
        agentId: Long? = null,
        period: String? = null,
        year: Int? = null
    ): DashboardStats {
        val json = api.dashboard(token, agentId, period, year).getOrThrow()
        val byPlanArr = json.optJSONArray("byPlan")
        val byPlanMap = mutableMapOf<Int, Int>()
        val byPlanList = mutableListOf<PlanBreakdown>()
        if (byPlanArr != null) {
            for (i in 0 until byPlanArr.length()) {
                val o = byPlanArr.getJSONObject(i)
                val months = o.optInt("months").takeIf { it > 0 } ?: o.optInt("years")
                val count = o.optInt("count")
                val label = o.optString("label").ifBlank {
                    when (months) {
                        6 -> "6 Mo"
                        12 -> "1 Yr"
                        24 -> "2 Yr"
                        30 -> "2 Yr+6 Free"
                        36 -> "3 Yr"
                        48 -> "4 Yr"
                        60 -> "5 Yr"
                        else -> "$months Mo"
                    }
                }
                byPlanMap[months] = count
                byPlanList += PlanBreakdown(months, label, count)
            }
        }
        val trendArr = json.optJSONArray("salesTrend")
        val monthly = mutableListOf<Pair<String, Int>>()
        if (trendArr != null) {
            for (i in 0 until trendArr.length()) {
                val o = trendArr.getJSONObject(i)
                val label = o.optString("label").ifBlank { o.optString("month") }
                monthly += label to o.optInt("count")
            }
        }
        val yearsArr = json.optJSONArray("availableYears")
        val years = mutableListOf<Int>()
        if (yearsArr != null) {
            for (i in 0 until yearsArr.length()) {
                years += yearsArr.optInt(i)
            }
        }
        return DashboardStats(
            totalAll = json.optLong("total"),
            last12Months = json.optLong("last12Mo"),
            todayCount = json.optLong("today"),
            weekCount = json.optLong("thisWeek"),
            monthCount = json.optLong("thisMonth"),
            yearCount = json.optLong("thisYear"),
            expiringNextMonth = json.optLong("expiringNextMo"),
            selectedYear = json.optInt("year"),
            availableYears = years,
            byPlanYears = byPlanMap,
            byPlan = byPlanList,
            byPlanMonthsBook = 0,
            monthlyCounts = monthly
        )
    }

    suspend fun buildAgentDashboard(
        api: BdApi,
        token: String,
        agentId: Long,
        period: String = "week"
    ): DashboardStats = buildDashboard(api, token, agentId, period)
}
