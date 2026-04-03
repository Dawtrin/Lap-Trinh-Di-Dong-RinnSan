package com.rinnsan.creavityapp

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VIEWMODEL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Khởi tạo "Cổng an ninh" (Auth) và "Nhà kho" (Firestore)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ── LOGIN ─────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("EMAIL VÀ ACCESS CODE KHÔNG ĐƯỢC ĐỂ TRỐNG.")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase() ?: "LỖI ĐĂNG NHẬP KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── REGISTER ──────────────────────────────────────────────────
    fun register(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("VUI LÒNG ĐIỀN ĐẦY ĐỦ THÔNG TIN.")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("ACCESS CODE KHÔNG KHỚP. THỬ LẠI.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("ACCESS CODE TỐI THIỂU 6 KÝ TỰ.")
            return
        }

        _authState.value = AuthState.Loading

        // Bước 1: Tạo tài khoản trên hệ thống Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    // Bước 2: Đóng gói thông tin hồ sơ
                    val userProfile = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis(),
                        "role" to "agent",
                        "identityProfile" to null
                    )
                    // Bước 3: Lưu hồ sơ vào Firestore
                    db.collection("users").document(userId)
                        .set(userProfile)
                        .addOnSuccessListener { _authState.value = AuthState.Success }
                        .addOnFailureListener { e ->
                            val errorMsg = e.localizedMessage?.uppercase() ?: "LỖI TẠO HỒ SƠ DATABASE."
                            _authState.value = AuthState.Error("// $errorMsg")
                        }
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase() ?: "LỖI KHỞI TẠO IDENTITY KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("NHẬP EMAIL ĐỂ TIẾP TỤC.")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase() ?: "LỖI GỬI SIGNAL KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    fun resetState() { _authState.value = AuthState.Idle }
}