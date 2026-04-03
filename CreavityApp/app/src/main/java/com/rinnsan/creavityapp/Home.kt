package com.rinnsan.creavityapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

private val VoidBlack    = Color(0xFF050505)
private val CyberAcid    = Color(0xFFCCFF00)
private val TeslaWhite   = Color(0xFFFFFFFF)
private val TechSilver   = Color(0xFFAAAAAA)
private val DimBorder    = Color(0xFF2A2A2A)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = CyberAcid, strokeWidth = 2.dp)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "SYSTEM OVERVIEW",
                    fontFamily = AppFonts.oswald,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AUTHORIZATION LEVEL: CLEARED",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = CyberAcid,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                        .background(Color(0xFF0D0D0D))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "IDENTITY CARD", fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TechSilver, letterSpacing = 2.sp)
                            Box(modifier = Modifier.size(8.dp).background(CyberAcid)) // Chấm xanh ngầu ngầu
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        InfoRow(label = "ROLE / CLASS", value = userProfile.role)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow(label = "AGENT EMAIL", value = userProfile.email)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow(label = "SYSTEM UID", value = userProfile.uid.take(12) + "...")
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Signin.rout) {
                            popUpTo(Screen.Home.rout) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, CyberAcid)
                ) {
                    Text(
                        text = "TERMINATE SESSION",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAcid,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = CyberAcid.copy(alpha = 0.7f), letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontFamily = AppFonts.spaceMono, fontSize = 16.sp, color = TeslaWhite)
    }
}