package com.iskcon.bhagavaddarshan.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Main : AppRoute("main")
    data object Register : AppRoute("register")
    data object Detail : AppRoute("detail/{id}") {
        fun create(id: Long) = "detail/$id"
    }
    data object CollectPayment : AppRoute("collect/{method}") {
        fun create(method: String) = "collect/$method"
    }
    data object CollectPaymentExisting : AppRoute("collect/{method}/{id}") {
        fun create(method: String, id: Long) = "collect/$method/$id"
    }
    data object AdminAgentNew : AppRoute("admin/agents/new")
    data object AdminAgentEdit : AppRoute("admin/agents/{id}") {
        fun create(id: Long) = "admin/agents/$id"
    }
}
