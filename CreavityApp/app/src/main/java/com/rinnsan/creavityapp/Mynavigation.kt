package com.rinnsan.creavityapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Mynavigation() {
    val navController = rememberNavController()

    // Kiểm tra xem đã đăng nhập chưa. Nếu rồi thì vào thẳng Home, chưa thì ra Signin
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) Screen.Home.rout else Screen.Signin.rout

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Signin.rout) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Signup.rout) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.rout) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.Home.rout) {
            HomeScreen(navController = navController) // Đảm bảo hàm trong file Home.kt của bạn tên là HomeScreen nhé
        }
    }
}