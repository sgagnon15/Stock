package com.sergeapps.stock

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sergeapps.stock.ui.HomeScreen
import com.sergeapps.stock.ui.ItemsListScreen
import com.sergeapps.stock.ui.SettingsScreen

@Composable
fun StockApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(Routes.Home) {
            HomeScreen(
                onOpenItems = { navController.navigate(Routes.ItemsList) },
                onOpenSettings = { navController.navigate(Routes.Settings) }
            )
        }
        composable(Routes.ItemsList) {
            ItemsListScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

object Routes {
    const val Home = "home"
    const val ItemsList = "items_list"
    const val Settings = "settings"
}
