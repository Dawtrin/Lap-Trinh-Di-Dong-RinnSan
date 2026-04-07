package com.rinnsan.noteapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rinnsan.noteapp.screens.*

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier
    ) {

        composable(route = Screen.Splash) {
            SplashScreen(navController = navController)
        }

        composable(route = Screen.Login) {
            LoginScreen(navController = navController)
        }

        // main/{role}
        composable(
            route = Screen.Main,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "user"
            MainScreen(navController = navController, userRole = role)
        }

        // detail/{productId}/{role}
        composable(
            route = Screen.ProductDetail,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("role")      { type = NavType.StringType; defaultValue = "user" }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val role      = backStackEntry.arguments?.getString("role")      ?: "user"
            ProductDetailScreen(
                navController = navController,
                productId     = productId,
                userRole      = role
            )
        }

        composable(
            route = Screen.AddEditProduct,
            arguments = listOf(
                navArgument("productId") {
                    type         = NavType.StringType
                    nullable     = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AddEditProductScreen(navController = navController, productId = productId)
        }
    }
}

object Screen {
    const val Splash         = "splash"
    const val Login          = "login"
    const val Main           = "main/{role}"
    const val ProductDetail  = "detail/{productId}/{role}"

    const val AddEditProduct = "add_edit?productId={productId}"

    fun main(role: String)                      = "main/$role"
    fun detail(productId: String, role: String) = "detail/$productId/$role"

    // Add mới → "add_edit" (không có query, defaultValue = null tự xử lý)
    // Edit    → "add_edit?productId=abc123"
    fun addEdit(productId: String? = null): String =
        if (productId != null) "add_edit?productId=$productId" else "add_edit"
}