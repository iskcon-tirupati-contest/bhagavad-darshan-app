package com.iskcon.bhagavaddarshan.ui.navigation

enum class AdminTab(val label: String) {
    HOME("Home"),
    CUSTOMERS("Customers"),
    AGENTS("Agents"),
    COMPLAINTS("Complaints"),
    PLANS("Plans"),
    RECONCILE("Reconcile")
}

enum class AgentTab(val label: String) {
    HOME("Home"),
    DEVOTEES("Devotees"),
    PROFILE("Profile")
}

enum class CustomerTab(val label: String) {
    HOME("Home"),
    PLANS("Plans"),
    MAGAZINES("Magazines"),
    HELP("Help"),
    PROFILE("Profile")
}
