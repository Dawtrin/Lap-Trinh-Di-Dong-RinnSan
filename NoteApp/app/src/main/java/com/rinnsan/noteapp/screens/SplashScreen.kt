package com.rinnsan.noteapp.screens

import androidx.compose.animation.core.*
import kotlin.math.pow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rinnsan.noteapp.R
import com.rinnsan.noteapp.navigation.Screen
import com.rinnsan.noteapp.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * SPLASH SCREEN — Fashion × Apple iOS 18
 *
 * Design Philosophy:
 * - Editorial fashion layout: title top-left aligned, large & dominant
 * - Italic accent word for high-fashion feel
 * - Frosted glass pill tag for slogan
 * - iOS 18-style liquid glass button pinned to bottom
 * - Multi-stop gradient vignette for cinematic depth
 */
@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val isDark = isSystemInDarkTheme()

    // ── Animation states ──────────────────────────────────────────
    val containerAlpha = remember { Animatable(0f) }
    val contentSlide   = remember { Animatable(60f) }
    val textAlpha      = remember { Animatable(0f) }
    val tagAlpha       = remember { Animatable(0f) }
    val buttonAlpha    = remember { Animatable(0f) }

    // ── Auth state (unchanged) ────────────────────────────────────
    var authRole by remember { mutableStateOf<String?>("__loading__") }
    LaunchedEffect(Unit) {
        val userId = authViewModel.getCurrentUserId()
        val role: String? = if (userId == null) null else {
            withTimeoutOrNull(4000L) { authViewModel.currentUserRole.first { it != null } }
                ?: authViewModel.currentUserRole.value
        }
        authRole = role
    }

    // ── Entrance animations ───────────────────────────────────────
    LaunchedEffect(Unit) {
        containerAlpha.animateTo(1f, tween(900, easing = EaseOutQuart))
        delay(100)
        contentSlide.animateTo(0f, tween(1000, easing = EaseOutExpo))
        textAlpha.animateTo(1f, tween(900, easing = EaseOutQuart))
        delay(200)
        tagAlpha.animateTo(1f, tween(700, easing = EaseOutQuart))
        delay(200)
        buttonAlpha.animateTo(1f, tween(700, easing = EaseOutQuart))
    }

    // ── Subtle floating animation for slogan ─────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val sloganFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 5f,
        animationSpec = infiniteRepeatable(
            tween(3800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "sloganFloat"
    )

    // ── Navigation (unchanged) ────────────────────────────────────
    fun navigate() {
        if (authRole == "__loading__") return
        if (authRole != null) {
            navController.navigate(Screen.main(authRole!!)) {
                popUpTo(Screen.Splash) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login) {
                popUpTo(Screen.Splash) { inclusive = true }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // ROOT
    // ═════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(containerAlpha.value)
    ) {

        // ── Background image ──────────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.a2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(8.dp),
            contentScale = ContentScale.Crop
        )

        // ── Multi-stop cinematic vignette ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = if (isDark) arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.55f),
                            0.30f to Color.Black.copy(alpha = 0.10f),
                            0.60f to Color.Black.copy(alpha = 0.40f),
                            1.00f to Color.Black.copy(alpha = 0.88f)
                        ) else arrayOf(
                            0.00f to Color(0xFFF8FAFC).copy(alpha = 0.72f),
                            0.30f to Color.White.copy(alpha = 0.18f),
                            0.60f to Color(0xFFF1F5F9).copy(alpha = 0.60f),
                            1.00f to Color(0xFFE2E8F0).copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // ═════════════════════════════════════════════════════════
        // CONTENT — SpaceBetween: top block + bottom block
        // ═════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── TOP BLOCK ─────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(top = 52.dp)
                    .alpha(textAlpha.value)
                    .offset(y = contentSlide.value.dp)
            ) {
                // Eyebrow — magazine-style category label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(1.5.dp)
                            .background(Color(0xFF3B82F6))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "PRODUCT MANAGEMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 3.5.sp,
                            fontSize = 9.5.sp
                        ),
                        color = if (isDark) Color.White.copy(0.55f) else Color(0xFF0F172A).copy(0.48f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // "Product" — ultra-heavy, editorial
                Text(
                    text = "Product",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-3.5).sp,
                        fontSize = 76.sp,
                        lineHeight = 70.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF0A0A0F)
                )

                // "Hub." — italic, blue accent, offset for visual rhythm
                Text(
                    text = "Hub.",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-3.5).sp,
                        fontSize = 76.sp,
                        lineHeight = 70.sp
                    ),
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.offset(x = 6.dp)
                )

                Spacer(Modifier.height(32.dp))

                // Frosted glass pill — slogan
                Box(
                    modifier = Modifier
                        .alpha(tagAlpha.value)
                        .offset(y = sloganFloat.dp)
                        .background(
                            color = if (isDark)
                                Color.White.copy(alpha = 0.11f)
                            else
                                Color(0xFF0F172A).copy(alpha = 0.06f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Where innovation meets organization.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.1.sp,
                            fontSize = 12.5.sp
                        ),
                        color = if (isDark)
                            Color.White.copy(alpha = 0.82f)
                        else
                            Color(0xFF1E293B).copy(alpha = 0.78f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Body descriptor
                Text(
                    text = "Transform your inventory into a\nmasterpiece of efficiency.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.1.sp,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    color = if (isDark)
                        Color.White.copy(alpha = 0.40f)
                    else
                        Color(0xFF475569).copy(alpha = 0.65f),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .alpha(tagAlpha.value)
                        .offset(y = sloganFloat.dp)
                )
            }

            // ── BOTTOM BLOCK: Button + footer ─────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .alpha(buttonAlpha.value)
            ) {
                if (authRole == "__loading__") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = if (isDark) Color(0xFF60A5FA) else Color(0xFF3B82F6),
                            strokeWidth = 2.5.dp,
                            trackColor = Color(0xFF3B82F6).copy(0.18f)
                        )
                        Text(
                            "Preparing your experience…",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = if (isDark) Color.White.copy(0.50f) else Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    IosGlassButton(
                        text = if (authRole != null) "Get Started" else "Get Started",
                        onClick = { navigate() },
                        isDark = isDark
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Rinnsan © 2026",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark)
                        Color.White.copy(alpha = 0.22f)
                    else
                        Color(0xFF64748B).copy(alpha = 0.38f),
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// iOS 18 LIQUID GLASS BUTTON
// Full-width, deep blue surface with frosted arrow pill
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun IosGlassButton(
    text: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2563EB),
                        Color(0xFF1D4ED8)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 80f)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    fontSize = 16.sp
                ),
                color = Color.White
            )

            // Arrow chip — frosted white pill
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// EASING FUNCTIONS
// ═══════════════════════════════════════════════════════════════════
private val EaseOutQuart = Easing { t ->
    val t1 = t - 1f; 1f - t1 * t1 * t1 * t1
}

private val EaseOutExpo = Easing { t ->
    if (t == 1f) 1f else 1f - 2.0.pow(-10.0 * t).toFloat()
}

@Composable
private fun isSystemInDarkTheme() = androidx.compose.foundation.isSystemInDarkTheme()