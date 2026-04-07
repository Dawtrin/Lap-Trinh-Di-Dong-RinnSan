package com.rinnsan.noteapp.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.noteapp.data.ProductRepository
import com.rinnsan.noteapp.models.Product
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProductListState {
    object Loading : ProductListState()
    data class Success(val products: List<Product>) : ProductListState()
    data class Error(val message: String) : ProductListState()
}

sealed class ProductOperationState {
    object Idle    : ProductOperationState()
    object Loading : ProductOperationState()
    object Success : ProductOperationState()
    data class Error(val message: String) : ProductOperationState()
}

sealed class ProductEffect {
    data class ShowToast(val message: String) : ProductEffect()
    object NavigateBack : ProductEffect()
}

class ProductViewModel(
    private val repo: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _listState = MutableStateFlow<ProductListState>(ProductListState.Loading)
    val listState: StateFlow<ProductListState> = _listState.asStateFlow()

    private val _operationState = MutableStateFlow<ProductOperationState>(ProductOperationState.Idle)
    val operationState: StateFlow<ProductOperationState> = _operationState.asStateFlow()

    private val _effect = MutableSharedFlow<ProductEffect>()
    val effect: SharedFlow<ProductEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            repo.getProductsStream()
                .catch { e -> _listState.value = ProductListState.Error(e.message ?: "Lỗi không xác định") }
                .collect { products -> _listState.value = ProductListState.Success(products) }
        }
    }

    /**
     * BUG FIX: Nhận Uri trực tiếp thay vì File
     * Repo sẽ đọc bytes từ contentResolver — đúng cách với scoped storage Android 10+
     *
     * @param imageUri   Uri ảnh từ gallery (null nếu dùng URL hoặc không có ảnh)
     * @param manualImageUrl URL nhập tay (rỗng nếu dùng upload)
     */
    fun addProduct(
        product: Product,
        imageUri: Uri?,
        manualImageUrl: String,
        context: Context
    ) {
        viewModelScope.launch {
            _operationState.value = ProductOperationState.Loading
            repo.addProduct(product, imageUri, manualImageUrl, context).fold(
                onSuccess = {
                    _operationState.value = ProductOperationState.Idle
                    _effect.emit(ProductEffect.ShowToast("Thêm sản phẩm thành công"))
                    _effect.emit(ProductEffect.NavigateBack)
                },
                onFailure = { e ->
                    _operationState.value = ProductOperationState.Error(e.message ?: "Thêm thất bại")
                    _effect.emit(ProductEffect.ShowToast("Lỗi: ${e.message}"))
                }
            )
        }
    }

    fun updateProduct(
        product: Product,
        imageUri: Uri?,
        manualImageUrl: String,
        context: Context
    ) {
        viewModelScope.launch {
            _operationState.value = ProductOperationState.Loading
            repo.updateProduct(product, imageUri, manualImageUrl, context).fold(
                onSuccess = {
                    _operationState.value = ProductOperationState.Idle
                    _effect.emit(ProductEffect.ShowToast("Cập nhật thành công"))
                    _effect.emit(ProductEffect.NavigateBack)
                },
                onFailure = { e ->
                    _operationState.value = ProductOperationState.Error(e.message ?: "Cập nhật thất bại")
                    _effect.emit(ProductEffect.ShowToast("Lỗi: ${e.message}"))
                }
            )
        }
    }

    fun deleteProduct(productId: String, imageUrl: String) {
        viewModelScope.launch {
            _operationState.value = ProductOperationState.Loading
            repo.deleteProduct(productId, imageUrl).fold(
                onSuccess = {
                    _operationState.value = ProductOperationState.Idle
                    _effect.emit(ProductEffect.ShowToast("Xóa thành công"))
                },
                onFailure = { e ->
                    _operationState.value = ProductOperationState.Idle
                    _effect.emit(ProductEffect.ShowToast("Xóa thất bại: ${e.message}"))
                }
            )
        }
    }

    fun resetOperationState() { _operationState.value = ProductOperationState.Idle }
}
