package com.rinnsan.noteapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Đăng nhập, trả về role của user (admin/user)
     */
    suspend fun login(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Login failed: user null")
        val doc = db.collection("users").document(uid).get().await()
        val role = doc.getString("role") ?: "user"
        Result.success(role)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Đăng ký tài khoản mới, tự động gán role:
     * - Nếu email có chứa "admin" (không phân biệt hoa thường) -> role = "admin"
     * - Ngược lại -> role = "user"
     */
    suspend fun register(email: String, password: String): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Register failed")
        val role = if (email.contains("admin", ignoreCase = true)) "admin" else "user"
        val userMap = mapOf("role" to role, "email" to email)
        db.collection("users").document(uid).set(userMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUserRole(): Result<String> = try {
        val uid = getCurrentUserId() ?: throw Exception("Not logged in")
        val doc = db.collection("users").document(uid).get().await()
        val role = doc.getString("role") ?: "user"
        Result.success(role)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Gửi email đặt lại mật khẩu
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}