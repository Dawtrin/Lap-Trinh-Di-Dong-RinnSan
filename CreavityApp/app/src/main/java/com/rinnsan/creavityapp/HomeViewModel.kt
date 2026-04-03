package com.rinnsan.creavityapp

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// Gói chứa thông tin người dùng
data class UserProfile(
    val email: String = "",
    val role: String = "",
    val uid: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchUserData()
    }

    // Hàm chui vào Firestore để lấy thông tin hồ sơ
    private fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val email = document.getString("email") ?: currentUser.email ?: "UNKNOWN"
                        val role = document.getString("role")?.uppercase() ?: "AGENT"

                        _userProfile.value = UserProfile(email = email, role = role, uid = currentUser.uid)
                    }
                }
                .addOnCompleteListener {
                    _isLoading.value = false // Tắt hiệu ứng loading khi lấy xong
                }
        } else {
            _isLoading.value = false
        }
    }
}