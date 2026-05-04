package com.rinnsan.lab10_inventoryapp.ui.item

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.rinnsan.lab10_inventoryapp.data.Item
import com.rinnsan.lab10_inventoryapp.data.ItemsRepository

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

data class ItemDetails(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val quantity: String = ""
)

fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0
)

fun Item.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = ItemDetails(id, name, price.toString(), quantity.toString()),
    isEntryValid = isEntryValid
)

fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id, name = name, price = price.toString(), quantity = quantity.toString()
)

class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            isEntryValid = validateInput(itemDetails)
        )
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }
}