package com.rinnsan.creavityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rinnsan.creavityapp.ui.theme.CreavityAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ HILT HOẠT ĐỘNG
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreavityAppTheme {
                // Gọi hàm chứa toàn bộ logic chuyển trang của bạn ra đây
                Mynavigation()
            }
        }
    }
}