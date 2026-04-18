package com.example.bluromatic.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bluromatic.R
import com.example.bluromatic.data.BlurAmount
import com.example.bluromatic.ui.theme.BluromaticTheme

@Composable
fun BluromaticScreen(blurViewModel: BlurViewModel = viewModel(factory = BlurViewModel.Factory)) {
    val uiState by blurViewModel.blurUiState.collectAsStateWithLifecycle()
    val layoutDirection = LocalLayoutDirection.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateStartPadding(layoutDirection),
                end = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateEndPadding(layoutDirection)
            ),
        color = MaterialTheme.colorScheme.background
    ) {
        BluromaticScreenContent(
            blurUiState = uiState,
            blurAmountOptions = blurViewModel.blurAmount,
            applyBlur = blurViewModel::applyBlur,
            cancelWork = blurViewModel::cancelWork,
            setImageUri = blurViewModel::setImageUri,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        )
    }
}

@Composable
fun BluromaticScreenContent(
    blurUiState: BlurUiState,
    blurAmountOptions: List<BlurAmount>,
    applyBlur: (Int) -> Unit,
    cancelWork: () -> Unit,
    setImageUri: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedValue by rememberSaveable { mutableStateOf(1) }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            setImageUri(it)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BlurOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Blur-O-Matic",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Chọn ảnh, điều chỉnh mức độ và bắt đầu làm mờ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ── Two image panels ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left panel – source image
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Ảnh gốc",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Image
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.android_cupcake),
                                contentDescription = stringResource(R.string.description_image),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Pick-image overlay bar at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                    ),
                                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                )
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedImageUri == null) "Chọn ảnh" else "Đổi ảnh",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Right panel – result image
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kết quả",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = blurUiState,
                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                            },
                            label = "blur_result"
                        ) { state ->
                            when (state) {
                                is BlurUiState.Default -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Ảnh mờ\nsẽ hiện ở đây",
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                is BlurUiState.Loading -> {
                                    val infiniteTransition =
                                        rememberInfiniteTransition(label = "spin")
                                    val rotation by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1200, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "rotation"
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .rotate(rotation),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Đang xử lý...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                is BlurUiState.Complete -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = Uri.parse(state.outputUri),
                                            contentDescription = "Blurred result",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Success badge
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    RoundedCornerShape(50)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = "Xong",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp
                                            )
                                        }
                                        // Open overlay bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.BottomCenter)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.65f)
                                                        )
                                                    ),
                                                    RoundedCornerShape(
                                                        bottomStart = 12.dp,
                                                        bottomEnd = 12.dp
                                                    )
                                                )
                                                .clickable {
                                                    showBlurredImage(context, state.outputUri)
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Mở ảnh",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Blur amount selector ─────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.blur_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Column(modifier = Modifier.selectableGroup()) {
                    blurAmountOptions.forEach { amount ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    role = Role.RadioButton,
                                    selected = selectedValue == amount.blurAmount,
                                    onClick = { selectedValue = amount.blurAmount }
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedValue == amount.blurAmount,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = stringResource(amount.blurAmountRes),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                                color = if (selectedValue == amount.blurAmount)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ── Action buttons ───────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = blurUiState is BlurUiState.Loading) {
                OutlinedButton(
                    onClick = cancelWork,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.cancel_work))
                }
            }

            Button(
                onClick = { applyBlur(selectedValue) },
                modifier = Modifier.fillMaxWidth(),
                enabled = blurUiState !is BlurUiState.Loading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BlurOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = if (blurUiState is BlurUiState.Loading) "Đang xử lý..."
                    else stringResource(R.string.start),
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(visible = blurUiState is BlurUiState.Complete) {
                val completeState = blurUiState as? BlurUiState.Complete
                FilledTonalButton(
                    onClick = {
                        completeState?.let { showBlurredImage(context, it.outputUri) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.see_file))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun showBlurredImage(context: Context, currentUri: String) {
    val uri = if (currentUri.isNotEmpty()) Uri.parse(currentUri) else null
    val actionView = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(actionView)
}

@Preview(showBackground = true)
@Composable
fun BluromaticScreenContentPreview() {
    BluromaticTheme {
        BluromaticScreenContent(
            blurUiState = BlurUiState.Default,
            blurAmountOptions = listOf(
                BlurAmount(R.string.blur_lv_1, 1),
                BlurAmount(R.string.blur_lv_2, 2),
                BlurAmount(R.string.blur_lv_3, 3),
            ),
            applyBlur = {},
            cancelWork = {},
            setImageUri = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
