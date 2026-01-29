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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StockApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable(Routes.Inventory) {
            PlaceholderScreen(title = "Inventaire", onBack = { navController.popBackStack() })
        }
        composable(Routes.Locations) {
            PlaceholderScreen(title = "Emplacements", onBack = { navController.popBackStack() })
        }
        composable(Routes.Classifications) {
            PlaceholderScreen(title = "Classifications", onBack = { navController.popBackStack() })
        }
        composable(Routes.Specifications) {
            PlaceholderScreen(title = "Spécifications", onBack = { navController.popBackStack() })
        }

        composable(Routes.Home) {
            HomeScreen(
                onOpenItems = { navController.navigate(Routes.ItemsList) },
                onOpenInventory = { navController.navigate(Routes.Inventory) },
                onOpenLocations = { navController.navigate(Routes.Locations) },
                onOpenClassifications = { navController.navigate(Routes.Classifications) },
                onOpenSpecifications = { navController.navigate(Routes.Specifications) },
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

    const val Inventory = "inventory"
    const val Locations = "locations"
    const val Classifications = "classifications"
    const val Specifications = "specifications"

    fun itemDetail(id: Int): String = "$ItemDetail/$id"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Bientôt…", style = MaterialTheme.typography.titleMedium)
        }
    }
}
