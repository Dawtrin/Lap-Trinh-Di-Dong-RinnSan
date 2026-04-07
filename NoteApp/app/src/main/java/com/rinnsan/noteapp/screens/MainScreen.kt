package com.rinnsan.noteapp.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rinnsan.noteapp.models.Product
import com.rinnsan.noteapp.ui.theme.*
import com.rinnsan.noteapp.viewmodels.*
import com.rinnsan.noteapp.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    userRole: String,
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val context        = LocalContext.current
    val isAdmin        = userRole == "admin"
    val listState      by productViewModel.listState.collectAsState()
    val isDark         = isSystemInDarkTheme()

    var searchQuery    by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isGridView     by remember { mutableStateOf(true) }

    // Scroll behavior for collapsing top bar
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Auth effects
    LaunchedEffect(Unit) {
        authViewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                AuthEffect.NavigateToMain -> {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }  // Xóa toàn bộ back stack
                    }
                }
            }
        }
    }

    // Derive products list
    val products = when (val s = listState) {
        is ProductListState.Success -> s.products
        else -> emptyList()
    }

    // Extract categories dynamically
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filtered = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchCat   = selectedCategory == "All" || p.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.category.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    // ── Delete confirm dialog ────────────────────────────────
    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    Modifier
                        .size(52.dp)
                        .background(AppleRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DeleteOutline, null, tint = AppleRed, modifier = Modifier.size(24.dp))
                }
            },
            title = { Text("Delete Product?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "\"${product.name}\" will be permanently removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.deleteProduct(product.id, product.imageUrl)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = AppleRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ── Logout confirm dialog ────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Sign out?", fontWeight = FontWeight.Bold) },
            text = { Text("You will be returned to the login screen.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                }) {
                    Text("Sign out", color = AppleRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Products",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                        )
                        AnimatedVisibility(visible = scrollBehavior.state.collapsedFraction < 0.5f) {
                            Text(
                                text = if (isAdmin) "Admin · ${products.size} items" else "${products.size} items",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Grid/List toggle
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = "Toggle layout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Logout
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Outlined.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor      = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = if (isDark)
                        Color(0xFF1C1C1E).copy(alpha = 0.95f)
                    else
                        MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.addEdit()) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Product", fontWeight = FontWeight.SemiBold) },
                    containerColor = AppleBlue,
                    contentColor   = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    expanded = true
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        // ── Empty / loading / content ────────────────────────
        when (listState) {
            is ProductListState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = AppleBlue, strokeWidth = 2.5.dp, trackColor = AppleBlue.copy(0.15f))
                        Text("Loading products…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            is ProductListState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = AppleRed, modifier = Modifier.size(40.dp))
                        Text((listState as ProductListState.Error).message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + 4.dp,
                        bottom = padding.calculateBottomPadding() + 100.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Search bar
                    item {
                        AppleSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        )
                    }

                    // Category chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(categories) { cat ->
                                AppleCategoryChip(
                                    label = cat,
                                    selected = cat == selectedCategory,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                    }

                    // Results count
                    item {
                        if (searchQuery.isNotBlank() || selectedCategory != "All") {
                            Text(
                                "${filtered.size} result${if (filtered.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            EmptyState(
                                query = searchQuery,
                                isAdmin = isAdmin,
                                onAdd = { navController.navigate(Screen.addEdit()) }
                            )
                        }
                    } else if (isGridView) {
                        // Grid layout — 2 columns
                        items(filtered.chunked(2)) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                row.forEach { product ->
                                    ProductGridCard(
                                        product = product,
                                        isAdmin = isAdmin,
                                        modifier = Modifier.weight(1f),
                                        onClick = { navController.navigate(Screen.detail(product.id, userRole)) },
                                        onEdit = { navController.navigate(Screen.addEdit(product.id)) },
                                        onDelete = { showDeleteDialog = product }
                                    )
                                }
                                // Fill last row if odd number
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    } else {
                        // List layout
                        items(filtered) { product ->
                            ProductListCard(
                                product = product,
                                isAdmin = isAdmin,
                                modifier = Modifier.padding(bottom = 10.dp),
                                onClick = { navController.navigate(Screen.detail(product.id, userRole)) },
                                onEdit = { navController.navigate(Screen.addEdit(product.id)) },
                                onDelete = { showDeleteDialog = product }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────
@Composable
private fun AppleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE8E8ED))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp
            ),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) Text("Search products…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                    inner()
                }
            }
        )
        AnimatedVisibility(visible = query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Category chip ─────────────────────────────────────────────
@Composable
private fun AppleCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg    = if (selected) AppleBlue else if (isSystemInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFFE8E8ED)
    val tint  = if (selected) Color.White else MaterialTheme.colorScheme.onBackground
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        shape = RoundedCornerShape(100.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor   = AppleBlue,
            selectedLabelColor       = Color.White,
            containerColor           = bg,
            labelColor               = tint
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor    = Color.Transparent,
            selectedBorderColor = Color.Transparent
        )
    )
}

// ── Grid card ─────────────────────────────────────────────────
@Composable
private fun ProductGridCard(
    product: Product,
    isAdmin: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(stiffness = Spring.StiffnessMediumLow), label = "scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Color(0xFF1C1C1E) else Color.White)
            .border(1.dp, if (isDark) Color.White.copy(0.07f) else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(if (isDark) Color(0xFF2C2C2E) else AppleGray100)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.ImageNotSupported, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(32.dp))
                    }
                }

                // Category pill
                if (product.category.isNotBlank()) {
                    Box(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(product.category, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatPrice(product.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppleBlue,
                    fontWeight = FontWeight.Bold
                )

                // Admin actions
                if (isAdmin) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleBlue.copy(alpha = 0.1f))
                                .clickable(onClick = onEdit)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Edit", style = MaterialTheme.typography.labelSmall, color = AppleBlue, fontWeight = FontWeight.SemiBold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppleRed.copy(alpha = 0.08f))
                                .clickable(onClick = onDelete)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Delete", style = MaterialTheme.typography.labelSmall, color = AppleRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── List card ─────────────────────────────────────────────────
@Composable
private fun ProductListCard(
    product: Product,
    isAdmin: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, spring(stiffness = Spring.StiffnessMediumLow), label = "scale")

    Row(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Color(0xFF1C1C1E) else Color.White)
            .border(1.dp, if (isDark) Color.White.copy(0.07f) else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF2C2C2E) else AppleGray100),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(product.imageUrl, product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Outlined.ImageNotSupported, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(24.dp))
            }
        }

        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (product.category.isNotBlank()) {
                Text(product.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatPrice(product.price), style = MaterialTheme.typography.labelMedium, color = AppleBlue, fontWeight = FontWeight.Bold)
        }

        // Actions
        if (isAdmin) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.Edit, null, tint = AppleBlue, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, null, tint = AppleRed, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
        }
    }
}

// ── Empty state ───────────────────────────────────────────────
@Composable
private fun EmptyState(query: String, isAdmin: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (query.isNotBlank()) Icons.Outlined.SearchOff else Icons.Outlined.Inventory2,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            if (query.isNotBlank()) "No results for \"$query\"" else "No products yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            if (isAdmin) "Tap + to add your first product." else "Check back later.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isAdmin && query.isBlank()) {
            Spacer(Modifier.height(4.dp))
            // Thay AppleButton bằng Button chuẩn để tránh lỗi
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Product", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Price formatter ───────────────────────────────────────────
private fun formatPrice(raw: String): String {
    val n = raw.toLongOrNull() ?: return raw
    return "%,d VND".format(n).replace(',', '.')
}

@Composable
private fun isSystemInDarkTheme() = androidx.compose.foundation.isSystemInDarkTheme()
