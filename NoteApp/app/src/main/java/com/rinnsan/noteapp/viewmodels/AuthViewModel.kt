package com.rinnsan.noteapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.noteapp.data.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class AuthEffect {
    data class ShowToast(val message: String) : AuthEffect()
    object NavigateToMain : AuthEffect()
}

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect: SharedFlow<AuthEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            val userId = repo.getCurrentUserId()
            if (userId != null) {
                val result = repo.getCurrentUserRole()
                result.onSuccess { role ->
                    _currentUserRole.value = role
                }.onFailure {
                    // BUG FIX: Set null rõ ràng khi fail để SplashScreen không treo
                    _currentUserRole.value = null
                    _effect.emit(AuthEffect.ShowToast("Lỗi lấy role: ${it.message}"))
                }
            }
            // userId == null -> currentUserRole luôn null -> SplashScreen -> Login
        }
    }

    // BUG FIX: Expose để SplashScreen kiểm tra có user chưa
    // mà không cần đợi timeout hay đoán mò
    fun getCurrentUserId(): String? = repo.getCurrentUserId()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.login(email, password)
            _uiState.value = AuthUiState.Idle
            result.fold(
                onSuccess = { role ->
                    _currentUserRole.value = role
                    _effect.emit(AuthEffect.NavigateToMain)
                },
                onFailure = { e ->
                    val message = when {
                        e.message?.contains("badly formatted") == true           -> "Email không hợp lệ"
                        e.message?.contains("no user record") == true            -> "Tài khoản không tồn tại"
                        e.message?.contains("password is invalid") == true       -> "Mật khẩu sai"
                        e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Email hoặc mật khẩu không đúng"
                        else -> e.message ?: "Đăng nhập thất bại"
                    }
                    _effect.emit(AuthEffect.ShowToast(message))
                }
            )
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.register(email, password)
            _uiState.value = AuthUiState.Idle
            result.fold(
                onSuccess = {
                    _effect.emit(AuthEffect.ShowToast("Đăng ký thành công! Vui lòng đăng nhập"))
                },
                onFailure = { e ->
                    val message = when {
                        e.message?.contains("badly formatted") == true        -> "Email không hợp lệ"
                        e.message?.contains("already in use") == true         -> "Email đã được sử dụng"
                        e.message?.contains("at least 6 characters") == true  -> "Mật khẩu phải có ít nhất 6 ký tự"
                        else -> e.message ?: "Đăng ký thất bại"
                    }
                    _effect.emit(AuthEffect.ShowToast(message))
                }
            )
        }
    }

    // ── Quên mật khẩu ─────────────────────────────────────────
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val result = repo.sendPasswordResetEmail(email)
            result.onSuccess {
                _effect.emit(AuthEffect.ShowToast("Email đặt lại mật khẩu đã được gửi (nếu email tồn tại)"))
            }.onFailure { e ->
                _effect.emit(AuthEffect.ShowToast("Lỗi: ${e.message}"))
            }
        }
    }

    fun logout() {
        repo.logout()
        _currentUserRole.value = null
        viewModelScope.launch {
            _effect.emit(AuthEffect.NavigateToMain)
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}