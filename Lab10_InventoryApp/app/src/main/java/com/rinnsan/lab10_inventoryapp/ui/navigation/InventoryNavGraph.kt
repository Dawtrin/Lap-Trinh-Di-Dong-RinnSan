package com.rinnsan.lab10_inventoryapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rinnsan.lab10_inventoryapp.ui.home.HomeScreen
import com.rinnsan.lab10_inventoryapp.ui.item.ItemDetailsScreen
import com.rinnsan.lab10_inventoryapp.ui.item.ItemEditScreen
import com.rinnsan.lab10_inventoryapp.ui.item.ItemEntryScreen

object HomeDestination {
    const val route = "home"
}

object ItemEntryDestination {
    const val route = "item_entry"
}

object ItemDetailsDestination {
    const val route = "item_details"
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

object ItemEditDestination {
    const val route = "item_edit"
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@Composable
fun InventoryNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = HomeDestination.route) {
        composable(HomeDestination.route) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(ItemEntryDestination.route) },
                navigateToItemUpdate = {
                    navController.navigate("${ItemDetailsDestination.route}/$it")
                }
            )
        }
        composable(ItemEntryDestination.route) {
            ItemEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = ItemDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemDetailsDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            ItemDetailsScreen(
                navigateToEditItem = {
                    navController.navigate("${ItemEditDestination.route}/$it")
                },
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = ItemEditDestination.routeWithArgs,
            arguments = listOf(navArgument(ItemEditDestination.itemIdArg) {
                type = NavType.IntType
            })
        ) {
            ItemEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}