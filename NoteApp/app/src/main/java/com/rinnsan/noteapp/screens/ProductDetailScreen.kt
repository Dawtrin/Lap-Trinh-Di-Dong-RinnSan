package com.rinnsan.noteapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.rinnsan.noteapp.models.Product
import com.rinnsan.noteapp.navigation.Screen
import com.rinnsan.noteapp.ui.theme.*
import com.rinnsan.noteapp.viewmodels.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    userRole: String = "user",
    productViewModel: ProductViewModel = viewModel()
) {
    val isAdmin   = userRole == "admin"
    val isDark    = isSystemInDarkTheme()
    val listState by productViewModel.listState.collectAsState()

    var product          by remember { mutableStateOf<Product?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId, listState) {
        if (listState is ProductListState.Success) {
            product = (listState as ProductListState.Success).products.find { it.id == productId }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // ── Delete dialog — ĐẶT NGOÀI Scaffold, tránh bị padding clipping ──
    if (showDeleteDialog && product != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    Modifier.size(52.dp).background(AppleRed.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DeleteOutline, null, tint = AppleRed, modifier = Modifier.size(24.dp))
                }
            },
            title = { Text("Delete Product?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "\"${product!!.name}\" will be permanently removed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.deleteProduct(product!!.id, product!!.imageUrl)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = AppleRed, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = scrollBehavior.state.contentOffset < -200f,
                        enter = fadeIn() + slideInVertically { -it },
                        exit  = fadeOut() + slideOutVertically { -it }
                    ) {
                        Text(
                            product?.name ?: "Product",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            Modifier.size(36.dp)
                                .background(
                                    if (isDark) Color(0xFF2C2C2E) else Color(0xFFEEEEF0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBackIosNew, null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (isAdmin && product != null) {
                        IconButton(onClick = { navController.navigate(Screen.addEdit(product!!.id)) }) {
                            Box(
                                Modifier.size(36.dp).background(AppleBlue.copy(0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = AppleBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Box(
                                Modifier.size(36.dp).background(AppleRed.copy(0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.DeleteOutline, null, tint = AppleRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = if (isDark) Color(0xFF1C1C1E).copy(0.95f)
                    else MaterialTheme.colorScheme.background.copy(0.92f)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        AnimatedContent(
            targetState = product,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "detail"
        ) { p ->
            if (p == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = AppleBlue, strokeWidth = 2.5.dp,
                        trackColor = AppleBlue.copy(0.15f)
                    )
                }
            } else {
                DetailContent(p, isAdmin, padding) {
                    navController.navigate(Screen.addEdit(p.id))
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    product: Product,
    isAdmin: Boolean,
    padding: PaddingValues,
    onEdit: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
    ) {
        // ── Hero image ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(if (isDark) Color(0xFF1C1C1E) else AppleGray100)
        ) {
            if (product.imageUrl.isNotEmpty()) {
                // ── FIX: SubcomposeAsyncImage dùng đúng loading/error params
                //    KHÔNG override SubcomposeAsyncImageContent() bằng TODO()
                SubcomposeAsyncImage(
                    model           = product.imageUrl,
                    contentDescription = product.name,
                    contentScale    = ContentScale.Crop,
                    modifier        = Modifier.fillMaxSize(),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = AppleBlue, strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.BrokenImage, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    "Image unavailable",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                                )
                            }
                        }
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ImageNotSupported, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No image",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                        )
                    }
                }
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                (if (isDark) Color.Black else Color(0xFFF5F5F7)).copy(0.55f)
                            )
                        )
                    )
            )

            // Category badge
            if (product.category.isNotBlank()) {
                Box(
                    Modifier.align(Alignment.BottomStart).padding(16.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Brush.horizontalGradient(listOf(AppleBlue, ApplePurple)))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        product.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Name + price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 3, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    formatP(product.price),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleBlue
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            DetailRow(Icons.Outlined.Category, "Category", product.category.ifBlank { "—" })
            DetailRow(Icons.Outlined.Tag,      "Product ID",
                if (product.id.length > 8) product.id.take(8) + "…" else product.id)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (isAdmin) {Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Edit Product", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1C1C1E) else Color.White)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp)
                .background(AppleBlue.copy(0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AppleBlue, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatP(raw: String): String {
    val n = raw.toLongOrNull() ?: return raw
    return "%,d VND".format(n).replace(',', '.')
}

@Composable
private fun isSystemInDarkTheme() = androidx.compose.foundation.isSystemInDarkTheme()
