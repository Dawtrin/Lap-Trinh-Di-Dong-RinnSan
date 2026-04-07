package com.rinnsan.noteapp.models

data class Product(
    val id: String = "",          // ID document trên Firestore
    val name: String = "",        // Tên sản phẩm
    val category: String = "",    // Loại sản phẩm (Thời trang nữ, Áo phong nam...)
    val price: String = "",       // Giá sản phẩm (dạng chuỗi, ví dụ "300000")
    val imageUrl: String = ""     // URL ảnh trên Firebase Storage
)