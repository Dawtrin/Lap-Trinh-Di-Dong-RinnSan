package com.rinnsan.lab10_inventoryapp.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.lab10_inventoryapp.data.ItemsRepository
import com.rinnsan.lab10_inventoryapp.ui.navigation.ItemDetailsDestination
import kotlinx.coroutines.flow.*

data class ItemDetailsUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val outOfStock: Boolean = true
)

class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map {
                ItemDetailsUiState(
                    itemDetails = it.toItemDetails(),
                    outOfStock = it.quantity <= 0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ItemDetailsUiState()
            )

    suspend fun deleteItem() {
        itemsRepository.deleteItem(uiState.value.itemDetails.toItem())
    }

    suspend fun reduceQuantityByOne() {
        val currentItem = uiState.value.itemDetails.toItem()
        if (currentItem.quantity > 0) {
            itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - 1))
        }
    }
}