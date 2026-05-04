package com.rinnsan.lab10_inventoryapp.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rinnsan.lab10_inventoryapp.InventoryApplication
import com.rinnsan.lab10_inventoryapp.ui.home.HomeViewModel
import com.rinnsan.lab10_inventoryapp.ui.item.ItemDetailsViewModel
import com.rinnsan.lab10_inventoryapp.ui.item.ItemEditViewModel
import com.rinnsan.lab10_inventoryapp.ui.item.ItemEntryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(inventoryApplication().container.itemsRepository)
        }
        initializer {
            ItemEntryViewModel(inventoryApplication().container.itemsRepository)
        }
        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.itemsRepository
            )
        }
        initializer {
            ItemEditViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.itemsRepository
            )
        }
    }
}

fun CreationExtras.inventoryApplication(): InventoryApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InventoryApplication)