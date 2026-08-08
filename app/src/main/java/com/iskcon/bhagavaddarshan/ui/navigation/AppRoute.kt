package com.iskcon.bhagavaddarshan.ui.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Register : AppRoute("register")
    data object Search : AppRoute("search")
    data object Expiring : AppRoute("expiring")
    data object Detail : AppRoute("detail/{id}") {
        fun create(id: Long) = "detail/$id"
    }
}
