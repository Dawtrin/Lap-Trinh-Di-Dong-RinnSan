package com.rinnsan.creavityapp

sealed class Screen(val rout: String) {
    object Home : Screen("home")
    object Signin : Screen("signin")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
}