package com.rinnsan.noteapp.data

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.noteapp.models.Product
import com.rinnsan.noteapp.utils.CloudinaryUploader
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    fun getProductsStream(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val products = snapshot?.documents?.mapNotNull { doc ->
                Product(
                    id       = doc.id,
                    name     = doc.getString("name")     ?: "",
                    category = doc.getString("category") ?: "",
                    price    = doc.getString("price")    ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            } ?: emptyList()
            trySend(products)
        }
        awaitClose { listener.remove() }
    }

    private suspend fun uploadImageToCloudinary(uri: Uri, context: Context): String {
        return CloudinaryUploader.upload(uri, context).getOrThrow()
    }

    private suspend fun resolveImageUrl(
        manualUrl: String,
        imageUri: Uri?,
        existingUrl: String,
        context: Context
    ): String = when {
        manualUrl.isNotBlank() -> manualUrl.trim()
        imageUri != null       -> uploadImageToCloudinary(imageUri, context)
        else                   -> existingUrl
    }

    suspend fun addProduct(
        product: Product,
        imageUri: Uri?,
        manualImageUrl: String,
        context: Context
    ): Result<Unit> = try {
        val finalUrl = resolveImageUrl(manualImageUrl, imageUri, "", context)
        productsCollection.add(mapOf(
            "name"     to product.name,
            "category" to product.category,
            "price"    to product.price,
            "imageUrl" to finalUrl
        )).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProduct(
        product: Product,
        imageUri: Uri?,
        manualImageUrl: String,
        context: Context
    ): Result<Unit> = try {
        val finalUrl = resolveImageUrl(manualImageUrl, imageUri, product.imageUrl, context)
        productsCollection.document(product.id).set(mapOf(
            "name"     to product.name,
            "category" to product.category,
            "price"    to product.price,
            "imageUrl" to finalUrl
        )).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteProduct(productId: String, imageUrl: String): Result<Unit> = try {
        productsCollection.document(productId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}