package com.rinnsan.noteapp.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Upload ảnh lên Cloudinary (miễn phí, không cần Firebase Storage)
 *
 * Cách tạo tài khoản Cloudinary miễn phí:
 * 1. Vào https://cloudinary.com → Sign up free
 * 2. Dashboard → Copy "Cloud name"
 * 3. Settings → Upload → Add upload preset → Mode: Unsigned → Save
 * 4. Copy tên preset vừa tạo
 * 5. Điền CLOUD_NAME và UPLOAD_PRESET bên dưới
 */
object CloudinaryUploader {

    // ── ĐỔI 2 GIÁ TRỊ NÀY sau khi tạo tài khoản Cloudinary ───
    private const val CLOUD_NAME    = "dsdhckzwo"    // vd: "dxyz1234"
    private const val UPLOAD_PRESET = "noteapp_unsigned" // vd: "noteapp_unsigned"
    // ──────────────────────────────────────────────────────────

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Upload ảnh từ Uri lên Cloudinary
     * @return URL ảnh sau khi upload thành công
     */
    suspend fun upload(uri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Đọc bytes từ ContentResolver (đúng cách với Android 10+ scoped storage)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Không thể đọc file ảnh"))

            // Detect MIME type
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png"  -> "png"
                "image/webp" -> "webp"
                "image/gif"  -> "gif"
                else         -> "jpg"
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", "upload.$extension",
                    bytes.toRequestBody(mimeType.toMediaType())
                )
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Không có phản hồi từ server"))

            if (!response.isSuccessful) {
                val errorMsg = try { JSONObject(body).optString("error", "Upload thất bại") } catch (_: Exception) { "Upload thất bại (${response.code})" }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val json = JSONObject(body)
            val secureUrl = json.getString("secure_url")
            Result.success(secureUrl)

        } catch (e: Exception) {
            Result.failure(Exception("Lỗi upload: ${e.message}"))
        }
    }
}