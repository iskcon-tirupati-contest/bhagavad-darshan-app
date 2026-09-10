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
    data object CustomerShipping : AppRoute("customer/shipping/{planYears}") {
        fun create(planYears: Int) = "customer/shipping/$planYears"
    }
    data object CustomerCheckout : AppRoute("customer/checkout/{planYears}/{shippingPaise}") {
        fun create(planYears: Int, shippingPaise: Int = 0) = "customer/checkout/$planYears/$shippingPaise"
    }
    data object CustomerSeva : AppRoute("customer/seva/{kind}/{amountPaise}") {
        fun create(kind: String, amountPaise: Int, title: String): String {
            val encoded = java.net.URLEncoder.encode(title, "UTF-8")
            return "customer/seva/$kind/$amountPaise?title=$encoded"
        }
    }
    data object CustomerRegister : AppRoute("customer/register")
    data object AddDevotee : AppRoute("agent/add-devotee")
    data object EditDevotee : AppRoute("agent/edit-devotee/{id}") {
        fun create(id: Long) = "agent/edit-devotee/$id"
    }
    data object CustomerCompleteProfile : AppRoute("customer/complete-profile")
    data object PaymentSuccess : AppRoute("customer/success") {
        // detail passed via savedStateHandle / callback; route is simple
    }
}
