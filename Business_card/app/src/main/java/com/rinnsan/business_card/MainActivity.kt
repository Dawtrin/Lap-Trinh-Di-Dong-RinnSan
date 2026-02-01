package com.rinnsan.business_card

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.HorizontalDivider

// --- BẢNG MÀU HIGH-FASHION ---
val DeepBlack = Color(0xFF050505)
val SilverWhite = Color(0xFFF5F5F7)
val TitanGray = Color(0xFF8E8E93)
val AccentGold = Color(0xFFD4AF37) // Một chút nhấn vàng kim loại cho sự sang trọng

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusinessCardApp()
        }
    }
}

@Composable
fun BusinessCardApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Spacer phía trên để đẩy nội dung chính vào trung tâm vàng
            Spacer(modifier = Modifier.height(60.dp))

            // PHẦN 1: IDENTITY (NHẬN DIỆN)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo trừu tượng tự vẽ bằng Box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, SilverWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = SilverWhite,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraLight
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "TRAN VIET DAT",
                    color = SilverWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 8.sp // Tạo độ thoáng High-fashion
                )

                Text(
                    text = "EXECUTIVE DIRECTOR",
                    color = TitanGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Một đường kẻ mảnh tinh tế
                HorizontalDivider(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .width(40.dp),
                    thickness = 0.5.dp,
                    color = TitanGray
                )
            }

            // PHẦN 2: CONTACT (LIÊN HỆ)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.Start // Căn lề trái cho chuyên nghiệp
            ) {
                ContactRow(icon = Icons.Rounded.Phone, text = "0396 704 484")
                ContactRow(icon = Icons.Rounded.Email, text = "dattv.24it@vku.udn.vn")
                ContactRow(icon = Icons.Rounded.Language, text = "linkedin.com/in/vietdat.exec")

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ARCHITECTING THE FUTURE",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun ContactRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SilverWhite,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = SilverWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp
        )
    }
}