package com.rinnsan.lab10_inventoryapp

import android.app.Application
import com.rinnsan.lab10_inventoryapp.data.AppContainer
import com.rinnsan.lab10_inventoryapp.data.AppDataContainer

class InventoryApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}