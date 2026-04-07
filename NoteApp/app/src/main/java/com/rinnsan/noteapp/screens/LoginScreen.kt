package com.rinnsan.noteapp.screens

import android.widget.Toast
import androidx.compose.animation.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rinnsan.noteapp.navigation.Screen
import com.rinnsan.noteapp.ui.theme.*
import com.rinnsan.noteapp.viewmodels.*
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // ── State (unchanged) ─────────────────────────────────────────
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegisterMode  by remember { mutableStateOf(false) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    var passwordError   by remember { mutableStateOf<String?>(null) }
    var confirmError    by remember { mutableStateOf<String?>(null) }

    var passwordStrength by remember { mutableStateOf(0f) }
    var strengthText    by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading

    // ── Effects (unchanged) ───────────────────────────────────────
    LaunchedEffect(password) {
        val (score, text) = checkPasswordStrength(password)
        passwordStrength = score
        strengthText = text
        if (passwordError != null && password.length >= 6) passwordError = null
        if (confirmError != null && confirmPassword == password) confirmError = null
    }

    LaunchedEffect(confirmPassword) {
        if (confirmError != null && confirmPassword == password) confirmError = null
    }

    // ── Entrance animations ───────────────────────────────────────
    val containerAlpha = remember { Animatable(0f) }
    val headerSlide    = remember { Animatable((-40f)) }   // slides down from top
    val glassAlpha     = remember { Animatable(0f) }
    val contentSlide   = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        containerAlpha.animateTo(1f, tween(400, easing = EaseOut))
        // Header comes in from top
        headerSlide.animateTo(0f, tween(700, easing = EaseOutExpo))
        delay(80)
        glassAlpha.animateTo(1f, tween(600, easing = EaseOut))
        contentSlide.animateTo(0f, tween(500, easing = EaseOutCubic))
    }

    // ── Auth navigation effect (unchanged) ───────────────────────
    LaunchedEffect(Unit) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                AuthEffect.NavigateToMain -> {
                    val role = authViewModel.currentUserRole.value ?: "user"
                    navController.navigate(Screen.main(role)) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
    }

    // ── Floating orb animation (unchanged) ───────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -12f, targetValue = 12f,
        animationSpec = infiniteRepeatable(
            tween(3500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // ── Subtle slogan float ───────────────────────────────────────
    val sloganFloat by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 4f,
        animationSpec = infiniteRepeatable(
            tween(4200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "sloganFloat"
    )

    // ═════════════════════════════════════════════════════════════
    // ROOT
    // ═════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(containerAlpha.value)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusManager.clearFocus() }
    ) {
        // ── Background gradient (unchanged colors) ────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) listOf(
                            Color(0xFF0A0E27),
                            Color(0xFF151B3A),
                            Color(0xFF0F172A)
                        ) else listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFF0F9FF),
                            Color(0xFFFCFDFF)
                        )
                    )
                )
        )

        // ── Floating orbs (unchanged) ─────────────────────────────
        Box(
            Modifier
                .size(320.dp)
                .offset(x = (-100).dp, y = (-80).dp + floatY.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AppleBlue.copy(alpha = if (isDark) 0.25f else 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp + (-floatY).dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            ApplePurple.copy(alpha = if (isDark) 0.2f else 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // ═════════════════════════════════════════════════════════
        // SCROLLABLE CONTENT
        // ═════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // ═════════════════════════════════════════════════════
            // HEADER BLOCK — Editorial branding (replaces old logo)
            // Đồng bộ visual language với SplashScreen
            // ═════════════════════════════════════════════════════
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(containerAlpha.value)
                    .offset(y = headerSlide.value.dp)
                    .padding(start = 4.dp, bottom = 36.dp)
            ) {
                // Eyebrow — same as Splash
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
                        color = if (isDark) Color.White.copy(0.50f) else Color(0xFF0F172A).copy(0.45f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                // "Product Hub." — same editorial weight as Splash, smaller size for form context
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Product ",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            fontSize = 42.sp,
                            lineHeight = 42.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0A0A0F)
                    )
                    Text(
                        text = "Hub.",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = (-2).sp,
                            fontSize = 42.sp,
                            lineHeight = 42.sp
                        ),
                        color = Color(0xFF3B82F6)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Animated slogan pill — frosted glass, same as Splash
                Box(
                    modifier = Modifier
                        .offset(y = sloganFloat.dp)
                        .background(
                            color = if (isDark)
                                Color.White.copy(alpha = 0.09f)
                            else
                                Color(0xFF0F172A).copy(alpha = 0.055f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Where innovation meets organization.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.1.sp,
                            fontSize = 11.5.sp
                        ),
                        color = if (isDark)
                            Color.White.copy(alpha = 0.75f)
                        else
                            Color(0xFF1E293B).copy(alpha = 0.72f)
                    )
                }
            }

            // ═════════════════════════════════════════════════════
            // GLASS CARD — form content (unchanged structure)
            // ═════════════════════════════════════════════════════
            GlassLoginCard(
                modifier = Modifier
                    .alpha(glassAlpha.value)
                    .offset(y = contentSlide.value.dp),
                isDark = isDark
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(28.dp)
                ) {
                    // ── Mode title — animated (unchanged) ─────────
                    AnimatedContent(
                        targetState = isRegisterMode,
                        transitionSpec = {
                            (slideInVertically { -it / 2 } + fadeIn()) togetherWith
                                    (slideOutVertically { it / 2 } + fadeOut())
                        },
                        label = "modeTitle"
                    ) { isReg ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isReg) "Create Account" else "Welcome Back",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-1).sp,
                                    fontSize = 24.sp
                                ),
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isReg) "Join Product Hub today" else "Sign in to continue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Email field (unchanged) ───────────────────
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, null, tint = AppleBlue)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !isLoading,
                        isDark = isDark
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Password field (unchanged) ────────────────
                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, null, tint = AppleBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.Visibility
                                    else Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = {
                                focusManager.clearFocus()
                                if (!isRegisterMode) performLogin(authViewModel, email, password)
                            }
                        ),
                        enabled = !isLoading,
                        isError = passwordError != null,
                        supportingText = passwordError?.let {
                            { Text(it, color = AppleRed, fontSize = 12.sp) }
                        },
                        isDark = isDark
                    )

                    // ── Password strength (unchanged) ─────────────
                    if (isRegisterMode && password.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        PasswordStrengthIndicator(
                            strength = passwordStrength,
                            label = strengthText,
                            isDark = isDark
                        )
                    }

                    // ── Confirm password (unchanged) ──────────────
                    AnimatedVisibility(
                        visible = isRegisterMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            GlassTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm Password",
                                leadingIcon = {
                                    Icon(Icons.Outlined.Lock, null, tint = AppleBlue)
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        performRegister(
                                            authViewModel, email, password, confirmPassword
                                        ) { field ->
                                            when (field) {
                                                "password" -> passwordError = "Password must be at least 6 characters"
                                                "confirm" -> confirmError = "Passwords do not match"
                                            }
                                        }
                                    }
                                ),
                                enabled = !isLoading,
                                isError = confirmError != null,
                                supportingText = confirmError?.let {
                                    { Text(it, color = AppleRed, fontSize = 12.sp) }
                                },
                                isDark = isDark
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Primary action button (unchanged) ─────────
                    GlassPrimaryButton(
                        text = when {
                            isLoading -> "Processing..."
                            isRegisterMode -> "Create Account"
                            else -> "Sign In"
                        },
                        onClick = {
                            focusManager.clearFocus()
                            if (isRegisterMode) {
                                performRegister(authViewModel, email, password, confirmPassword) { field ->
                                    when (field) {
                                        "password" -> passwordError = "Password must be at least 6 characters"
                                        "confirm" -> confirmError = "Passwords do not match"
                                    }
                                }
                            } else {
                                performLogin(authViewModel, email, password)
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() &&
                                (!isRegisterMode || confirmPassword.isNotBlank()),
                        isLoading = isLoading,
                        isDark = isDark
                    )

                    // ── Forgot password (unchanged) ───────────────
                    if (!isRegisterMode) {
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = { showForgotDialog = true },
                            enabled = !isLoading
                        ) {
                            Text(
                                "Forgot Password?",
                                style = MaterialTheme.typography.labelLarge,
                                color = AppleBlue,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Divider (unchanged) ───────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    if (isDark) Color.White.copy(0.1f)
                                    else Color(0xFFE2E8F0)
                                )
                        )
                        Text(
                            "or",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    if (isDark) Color.White.copy(0.1f)
                                    else Color(0xFFE2E8F0)
                                )
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Toggle mode button (unchanged) ────────────
                    GlassSecondaryButton(
                        text = if (isRegisterMode) "Already have an account? Sign In"
                        else "Don't have an account? Sign Up",
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            passwordError = null
                            confirmError = null
                        },
                        enabled = !isLoading,
                        isDark = isDark
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Footer — đồng bộ với Splash ──────────────────────
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

            Spacer(Modifier.height(24.dp))
        }

        // ── Forgot password dialog (unchanged) ────────────────────
        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White,
                title = {
                    Text(
                        "Reset Password",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        Text(
                            "Enter your email address and we'll send you a link to reset your password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color.White.copy(0.7f) else Color(0xFF64748B)
                        )
                        Spacer(Modifier.height(16.dp))
                        AppleTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = "Email",
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, null, tint = AppleBlue)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (forgotEmail.isNotBlank()) {
                                authViewModel.sendPasswordResetEmail(forgotEmail)
                                showForgotDialog = false
                                forgotEmail = ""
                            } else {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Send", color = AppleBlue, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("Cancel", color = if (isDark) Color.White.copy(0.7f) else Color(0xFF64748B))
                    }
                }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// Glass Components (unchanged)
// ══════════════════════════════════════════════════════════

@Composable
private fun GlassLoginCard(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.85f)
                        )
                    )
                }
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    ) else listOf(
                        Color.White.copy(alpha = 1f),
                        Color.White.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    isDark: Boolean
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        enabled = enabled,
        placeholder = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        leadingIcon = leadingIcon?.let {
            { Box(Modifier.padding(start = 4.dp)) { it() } }
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = if (isDark) Color(0xFF1C1C1E).copy(0.6f) else Color.White.copy(0.9f),
            unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E).copy(0.4f) else Color(0xFFF8F9FA).copy(0.8f),
            disabledContainerColor  = if (isDark) Color(0xFF1C1C1E).copy(0.3f) else Color(0xFFF0F0F5),
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor  = Color.Transparent,
            focusedLeadingIconColor   = AppleBlue,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
            focusedTextColor   = if (isDark) Color.White else Color(0xFF0F172A),
            unfocusedTextColor = if (isDark) Color.White.copy(0.9f) else Color(0xFF1E293B),
            errorContainerColor      = if (isDark) Color(0xFF4A1C1C).copy(0.5f) else Color(0xFFFFF0F0),
            errorLeadingIconColor    = AppleRed,
            errorTextColor           = if (isDark) Color.White else AppleRed,
            errorSupportingTextColor = AppleRed,
            errorIndicatorColor      = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun GlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    // iOS 18 Liquid Glass style — đồng bộ với Splash button
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = if (enabled) listOf(
                        Color(0xFF2563EB),
                        Color(0xFF1D4ED8)
                    ) else listOf(
                        Color(0xFF2563EB).copy(0.4f),
                        Color(0xFF1D4ED8).copy(0.4f)
                    )
                )
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        fontSize = 16.sp
                    ),
                    color = Color.White.copy(if (enabled) 1f else 0.7f)
                )
            }
            // Frosted arrow chip — same as Splash
            if (!isLoading) {
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
                        modifier = Modifier.size(15.dp),
                        tint = Color.White.copy(if (enabled) 1f else 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDark) Color.White.copy(alpha = 0.06f)
                else Color.Black.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.1f)
                else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = if (isDark) Color.White.copy(0.85f) else Color(0xFF334155)
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(
    strength: Float,
    label: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                strength < 0.4f -> AppleRed
                strength < 0.7f -> Color(0xFFFBBF24)
                else -> AppleGreen
            },
            trackColor = if (isDark) Color.White.copy(0.1f) else Color(0xFFE2E8F0),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = when {
                strength < 0.4f -> AppleRed
                strength < 0.7f -> Color(0xFFFBBF24)
                else -> AppleGreen
            }
        )
    }
}

// ══════════════════════════════════════════════════════════
// Helper Functions (unchanged)
// ══════════════════════════════════════════════════════════

private fun checkPasswordStrength(password: String): Pair<Float, String> {
    if (password.isEmpty()) return Pair(0f, "")
    var score = 0f
    if (password.length >= 8) score += 0.25f
    if (password.any { it.isDigit() }) score += 0.25f
    if (password.any { it.isLowerCase() } && password.any { it.isUpperCase() }) score += 0.25f
    if (password.any { !it.isLetterOrDigit() }) score += 0.25f
    val strength = when {
        score < 0.4f -> "Weak"
        score < 0.7f -> "Medium"
        else -> "Strong"
    }
    return Pair(score.coerceIn(0f, 1f), strength)
}

private fun performLogin(authViewModel: AuthViewModel, email: String, password: String) {
    if (email.isBlank() || password.isBlank()) return
    authViewModel.login(email, password)
}

private fun performRegister(
    authViewModel: AuthViewModel,
    email: String,
    password: String,
    confirmPassword: String,
    onError: (String) -> Unit
) {
    if (email.isBlank()) return
    if (password.length < 6) {
        onError("password")
        return
    }
    if (password != confirmPassword) {
        onError("confirm")
        return
    }
    authViewModel.register(email, password)
}

// ══════════════════════════════════════════════════════════
// Shared Apple Components (unchanged)
// ══════════════════════════════════════════════════════════

@Composable
fun AppleCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isDark) Color(0xFF1C1C1E).copy(alpha = 0.9f)
                else Color.White.copy(alpha = 0.85f)
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
        enabled = enabled,
        placeholder = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = leadingIcon?.let { { Box(Modifier.padding(start = 4.dp), contentAlignment = Alignment.Center) { it() } } },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = if (isDark) Color(0xFF2C2C2E) else Color.White,
            unfocusedContainerColor = if (isDark) Color(0xFF2C2C2E).copy(alpha = 0.7f) else Color(0xFFF5F5F7),
            disabledContainerColor  = if (isDark) Color(0xFF1C1C1E) else Color(0xFFEEEEF0),
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor  = Color.Transparent,
            focusedLeadingIconColor   = AppleBlue,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor   = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            errorContainerColor      = if (isDark) Color(0xFF4A1C1C) else Color(0xFFFFF0F0),
            errorLeadingIconColor    = AppleRed,
            errorTextColor           = AppleRed,
            errorSupportingTextColor = AppleRed,
            errorIndicatorColor      = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun isSystemInDarkTheme(): Boolean =
    androidx.compose.foundation.isSystemInDarkTheme()

// ── Easing functions ──────────────────────────────────────────────
private val EaseOut = Easing { t -> 1 - (1 - t) * (1 - t) }
private val EaseOutCubic = Easing { t -> 1 - (1 - t) * (1 - t) * (1 - t) }
private val EaseOutExpo = Easing { t ->
    if (t == 1f) 1f else 1f - 2.0.pow(-10.0 * t).toFloat()
}