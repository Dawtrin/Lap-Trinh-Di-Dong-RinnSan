package com.rinnsan.lab10_inventoryapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.lab10_inventoryapp.data.Item
import com.rinnsan.lab10_inventoryapp.data.ItemsRepository
import kotlinx.coroutines.flow.*

data class HomeUiState(val itemList: List<Item> = listOf())

class HomeViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {
    val homeUiState: StateFlow<HomeUiState> =
        itemsRepository.getAllItemsStream()
            .map { HomeUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = HomeUiState()
            )
}