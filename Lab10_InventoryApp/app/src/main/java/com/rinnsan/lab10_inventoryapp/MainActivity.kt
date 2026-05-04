package com.rinnsan.lab10_inventoryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.rinnsan.lab10_inventoryapp.ui.navigation.InventoryNavHost
import com.rinnsan.lab10_inventoryapp.ui.theme.Lab10_InventoryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab10_InventoryAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    InventoryNavHost(navController = navController)
                }
            }
        }
    }
}