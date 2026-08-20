package com.iskcon.bhagavaddarshan.data

import com.iskcon.bhagavaddarshan.network.BdApi
import org.json.JSONObject

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
        agentId: Long? = null
    ): DashboardStats {
        val json = api.dashboard(token, agentId).getOrThrow()
        val byPlanArr = json.optJSONArray("byPlan")
        val byPlan = mutableMapOf<Int, Int>()
        if (byPlanArr != null) {
            for (i in 0 until byPlanArr.length()) {
                val o = byPlanArr.getJSONObject(i)
                byPlan[o.optInt("years")] = o.optInt("count")
            }
        }
        val trendArr = json.optJSONArray("salesTrend")
        val monthly = mutableListOf<Pair<String, Int>>()
        if (trendArr != null) {
            for (i in 0 until trendArr.length()) {
                val o = trendArr.getJSONObject(i)
                monthly += o.optString("month") to o.optInt("count")
            }
        }
        return DashboardStats(
            totalAll = json.optLong("total"),
            last12Months = json.optLong("last12Mo"),
            todayCount = json.optLong("today"),
            monthCount = json.optLong("thisMonth"),
            yearCount = json.optLong("thisYear"),
            expiringNextMonth = json.optLong("expiringNextMo"),
            byPlanYears = byPlan,
            byPlanMonthsBook = 0,
            monthlyCounts = monthly
        )
    }

    suspend fun buildAgentDashboard(
        api: BdApi,
        token: String,
        agentId: Long
    ): DashboardStats = buildDashboard(api, token, agentId)
}
