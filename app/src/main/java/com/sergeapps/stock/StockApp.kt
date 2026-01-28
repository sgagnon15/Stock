package com.sergeapps.stock

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sergeapps.stock.ui.HomeScreen
import com.sergeapps.stock.ui.ItemDetailScreen
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
                onBack = { navController.popBackStack() },
                onOpenItem = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ItemDetailRoute,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("id") ?: 0
            ItemDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

object Routes {
    const val Home = "home"
    const val ItemsList = "items_list"
    const val Settings = "settings"

    const val ItemDetail = "item_detail"
    const val ItemDetailRoute = "item_detail/{id}"

    fun itemDetail(id: Int): String = "$ItemDetail/$id"
}
