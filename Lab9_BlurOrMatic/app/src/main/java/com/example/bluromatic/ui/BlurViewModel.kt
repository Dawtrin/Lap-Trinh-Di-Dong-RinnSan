package com.example.bluromatic.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import com.example.bluromatic.BluromaticApplication
import com.example.bluromatic.data.BlurAmountData
import com.example.bluromatic.data.BluromaticRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BlurViewModel(private val bluromaticRepository: BluromaticRepository) : ViewModel() {

    internal val blurAmount = BlurAmountData.blurAmount

    // FIX: Map WorkInfo from the repository to BlurUiState properly
    val blurUiState: StateFlow<BlurUiState> = bluromaticRepository.outputWorkInfo
        .map { workInfo ->
            when {
                workInfo == null -> BlurUiState.Default
                workInfo.state == WorkInfo.State.SUCCEEDED -> {
                    val outputUri = workInfo.outputData.getString(
                        com.example.bluromatic.KEY_IMAGE_URI
                    ) ?: ""
                    BlurUiState.Complete(outputUri)
                }
                workInfo.state == WorkInfo.State.CANCELLED -> BlurUiState.Default
                workInfo.state == WorkInfo.State.FAILED -> BlurUiState.Default
                else -> BlurUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BlurUiState.Default
        )

    fun applyBlur(blurLevel: Int) {
        bluromaticRepository.applyBlur(blurLevel)
    }

    fun cancelWork() {
        bluromaticRepository.cancelWork()
    }

    fun setImageUri(uri: Uri) {
        bluromaticRepository.setImageUri(uri)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val bluromaticRepository =
                    (this[APPLICATION_KEY] as BluromaticApplication).container.bluromaticRepository
                BlurViewModel(bluromaticRepository = bluromaticRepository)
            }
        }
    }
}

sealed interface BlurUiState {
    object Default : BlurUiState
    object Loading : BlurUiState
    data class Complete(val outputUri: String) : BlurUiState
}
