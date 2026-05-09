package com.tracelux.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Sky : Screen("sky", "SKY")
    object Field : Screen("field", "FIELD")
    object Inventory : Screen("inventory", "INVENTORY")
    object Options : Screen("options", "OPTIONS")
}

val bottomNavItems = listOf(
    Screen.Sky,
    Screen.Field,
    Screen.Inventory,
    Screen.Options
)
