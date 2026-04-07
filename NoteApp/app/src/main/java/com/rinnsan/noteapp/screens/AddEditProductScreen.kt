package com.rinnsan.noteapp.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.rinnsan.noteapp.ui.theme.*
import com.rinnsan.noteapp.models.Product
import com.rinnsan.noteapp.viewmodels.*

private enum class ImageMode { NONE, UPLOAD, URL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    navController: NavController,
    productId: String?,
    productViewModel: ProductViewModel = viewModel()
) {
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark       = isSystemInDarkTheme()
    val isEditMode   = productId != null && productId != "null"

    var name            by remember { mutableStateOf("") }
    var category        by remember { mutableStateOf("") }
    var price           by remember { mutableStateOf("") }
    var existingProduct by remember { mutableStateOf<Product?>(null) }

    var imageMode by remember { mutableStateOf(ImageMode.NONE) }
    var imageUri  by remember { mutableStateOf<Uri?>(null) }
    var imageUrl  by remember { mutableStateOf("") }

    val listState      by productViewModel.listState.collectAsState()
    val operationState by productViewModel.operationState.collectAsState()
    val isLoading = operationState is ProductOperationState.Loading

    // Load existing product (edit mode)
    LaunchedEffect(productId, listState) {
        if (isEditMode && listState is ProductListState.Success) {
            existingProduct = (listState as ProductListState.Success).products.find { it.id == productId }
            existingProduct?.let { p ->
                if (name.isEmpty())     name     = p.name
                if (category.isEmpty()) category = p.category
                if (price.isEmpty())    price    = p.price
                if (imageMode == ImageMode.NONE && p.imageUrl.isNotEmpty()) {
                    imageUrl  = p.imageUrl
                    imageMode = ImageMode.URL
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.effect.collect { effect ->
            when (effect) {
                is ProductEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                ProductEffect.NavigateBack -> navController.popBackStack()
            }
        }
    }

    // Image picker — trả về Uri, KHÔNG convert sang File
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri  = uri
            imageMode = ImageMode.UPLOAD
            imageUrl  = ""
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) imagePicker.launch("image/*")
        else Toast.makeText(context, "Cần cấp quyền truy cập ảnh", Toast.LENGTH_SHORT).show()
    }

    fun launchPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        else
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val alpha = remember { Animatable(0f) }
    val slide = remember { Animatable(24f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(400))
        slide.animateTo(0f, tween(400, easing = EaseOutCubic))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Product" else "Add Product",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            Modifier.size(36.dp)
                                .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFEEEEF0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            submitProduct(
                                isEditMode, existingProduct,
                                name, category, price,
                                imageMode, imageUri, imageUrl,
                                context, productViewModel
                            )
                        },
                        enabled = !isLoading && name.isNotBlank() && price.isNotBlank()
                    ) {
                        Text(
                            "Save",
                            color = if (!isLoading && name.isNotBlank() && price.isNotBlank())
                                AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
                .alpha(alpha.value)
                .offset(y = slide.value.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            ImageSection(
                isDark       = isDark,
                imageMode    = imageMode,
                imageUri     = imageUri,
                imageUrl     = imageUrl,
                isLoading    = isLoading,
                onModeSelect = { mode ->
                    imageMode = mode
                    if (mode == ImageMode.UPLOAD) imageUrl = ""
                    if (mode == ImageMode.URL)    imageUri = null
                },
                onPickImage  = { launchPicker() },
                onClearImage = { imageUri = null; imageUrl = ""; imageMode = ImageMode.NONE },
                onUrlChange  = { imageUrl = it }
            )

            FormSection(title = "Product Information") {
                FormField(
                    value = name, onValueChange = { name = it },
                    label = "Product name", placeholder = "e.g. Áo thun trắng",
                    icon = Icons.Outlined.ShoppingBag, required = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    enabled = !isLoading
                )
                FormField(
                    value = category, onValueChange = { category = it },
                    label = "Category", placeholder = "e.g. Thời trang nữ",
                    icon = Icons.Outlined.Category,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                    enabled = !isLoading
                )
                FormField(
                    value = price, onValueChange = { v -> if (v.all { it.isDigit() }) price = v },
                    label = "Price (VND)", placeholder = "e.g. 300000",
                    icon = Icons.Outlined.AttachMoney, required = true,
                    suffix = { Text("VND", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    enabled = !isLoading
                )
            }

            AnimatedVisibility(visible = operationState is ProductOperationState.Error) {
                if (operationState is ProductOperationState.Error) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(0.7f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text((operationState as ProductOperationState.Error).message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            AppleButton(
                onClick = {
                    focusManager.clearFocus()
                    submitProduct(isEditMode, existingProduct, name, category, price, imageMode, imageUri, imageUrl, context, productViewModel)
                },
                enabled = !isLoading && name.isNotBlank() && price.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                    label = "btn"
                ) { loading ->
                    if (loading) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp, trackColor = Color.White.copy(0.3f))
                            Text(if (isEditMode) "Đang cập nhật…" else "Đang thêm…", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isEditMode) Icons.Default.Check else Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Text(if (isEditMode) "Update Product" else "Add Product", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun AppleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val bgColor = if (isDanger) AppleRed else AppleBlue
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = bgColor,
            contentColor           = Color.White,
            disabledContainerColor = bgColor.copy(alpha = 0.4f),
            disabledContentColor   = Color.White.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 2.dp
        ),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────
// Image Section
// ─────────────────────────────────────────────────────────────
@Composable
private fun ImageSection(
    isDark: Boolean,
    imageMode: ImageMode,
    imageUri: Uri?,
    imageUrl: String,
    isLoading: Boolean,
    onModeSelect: (ImageMode) -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onUrlChange: (String) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF0F0F5))
                .border(1.5.dp, if (isDark) Color.White.copy(0.08f) else AppleGray200, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            val hasPreview = imageUri != null || imageUrl.isNotBlank()
            if (hasPreview) {
                // BUG FIX 2: Dùng ImageRequest.Builder với crossfade và memoryCachePolicy
                // để Coil load đúng cả Uri local và URL internet
                // ✅ FIX 2: SubcomposeAsyncImage + User-Agent header
                // Nhiều CDN (Facebook, Google CDN...) chặn request không có User-Agent
                // dẫn đến ảnh trắng hoặc không load được
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (imageUri != null) imageUri else imageUrl)
                        .crossfade(true)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .addHeader("User-Agent", "Mozilla/5.0 (Android) Chrome/120")
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = AppleBlue, strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp),
                                trackColor = AppleBlue.copy(0.15f)
                            )
                        }
                    },
                    error = {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.BrokenImage, null,
                                tint = AppleRed.copy(0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Không tải được ảnh\nKiểm tra lại URL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
                // Clear button
                Box(
                    Modifier.align(Alignment.TopEnd).padding(10.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(0.55f), CircleShape)
                        .clickable { onClearImage() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(36.dp))
                    Text(
                        "Chọn cách thêm ảnh bên dưới",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            "CHỌN 1 TRONG 2 CÁCH THÊM ẢNH",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ImageModeCard(
                modifier  = Modifier.weight(1f),
                icon      = Icons.Outlined.UploadFile,
                title     = "Tải ảnh lên",
                subtitle  = "Chọn từ thư viện",
                selected  = imageMode == ImageMode.UPLOAD,
                isDark    = isDark,
                onClick   = {
                    onModeSelect(ImageMode.UPLOAD)
                    onPickImage()
                }
            )
            ImageModeCard(
                modifier = Modifier.weight(1f),
                icon     = Icons.Outlined.Link,
                title    = "URL Internet",
                subtitle = "Dán link ảnh",
                selected = imageMode == ImageMode.URL,
                isDark   = isDark,
                onClick  = { onModeSelect(ImageMode.URL) }
            )
        }

        AnimatedVisibility(
            visible = imageMode == ImageMode.URL,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                label = { Text("URL ảnh") },
                placeholder = { Text("https://example.com/image.jpg", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Link, null,
                        tint = if (imageUrl.isNotEmpty()) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (imageUrl.isNotEmpty()) {
                        IconButton(onClick = { onUrlChange("") }) {
                            Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AppleBlue,
                    unfocusedBorderColor = if (isDark) Color.White.copy(0.12f) else AppleGray200,
                    focusedLabelColor    = AppleBlue
                )
            )
        }

        AnimatedVisibility(
            visible = imageMode == ImageMode.UPLOAD && imageUri == null,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            OutlinedButton(
                onClick = onPickImage,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, AppleBlue.copy(0.4f))
            ) {
                Icon(Icons.Outlined.UploadFile, null, tint = AppleBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chọn ảnh từ thư viện", color = AppleBlue, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ImageModeCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) AppleBlue else (if (isDark) Color.White.copy(0.10f) else AppleGray200)
    val bgColor     = if (selected) AppleBlue.copy(0.08f) else (if (isDark) Color(0xFF1C1C1E) else Color.White)
    val iconTint    = if (selected) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier.size(40.dp)
                    .background(
                        if (selected) AppleBlue.copy(0.12f)
                        else if (isDark) Color.White.copy(0.06f) else AppleGray100,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)) }

            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = if (selected) AppleBlue else MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            if (selected) Box(Modifier.size(8.dp).background(AppleBlue, CircleShape))
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (isDark) Color(0xFF1C1C1E) else Color.White)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    value: String, onValueChange: (String) -> Unit,
    label: String, placeholder: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    required: Boolean = false,
    suffix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    TextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(), enabled = enabled,
        label = { Row { Text(label); if (required) Text(" *", color = AppleRed) } },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)) },
        leadingIcon = { Icon(icon, null, tint = if (value.isNotEmpty()) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
        trailingIcon = suffix?.let { { Box(Modifier.padding(end = 8.dp)) { it() } } },
        singleLine = true, keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = if (isDark) Color(0xFF1C1C1E) else Color.White,
            unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color.White,
            disabledContainerColor  = if (isDark) Color(0xFF1C1C1E).copy(0.5f) else Color(0xFFF5F5F7),
            focusedIndicatorColor   = AppleBlue.copy(0.5f),
            unfocusedIndicatorColor = if (isDark) Color.White.copy(0.08f) else AppleGray200,
            disabledIndicatorColor  = Color.Transparent,
            focusedLabelColor       = AppleBlue,
            unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor        = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor      = MaterialTheme.colorScheme.onBackground,
        )
    )
}

// ── Submit ────────────────────────────────────────────────────
private fun submitProduct(
    isEditMode: Boolean,
    existingProduct: Product?,
    name: String, category: String, price: String,
    imageMode: ImageMode,
    imageUri: Uri?,
    imageUrl: String,
    context: android.content.Context,
    productViewModel: ProductViewModel
) {
    if (name.isBlank())  { Toast.makeText(context, "Tên sản phẩm không được trống", Toast.LENGTH_SHORT).show(); return }
    if (price.isBlank()) { Toast.makeText(context, "Giá không được trống", Toast.LENGTH_SHORT).show(); return }

    // BUG FIX: truyền Uri trực tiếp — KHÔNG convert sang File
    val finalUri = if (imageMode == ImageMode.UPLOAD) imageUri else null
    val finalUrl = if (imageMode == ImageMode.URL) imageUrl.trim() else ""

    val product = Product(
        id       = existingProduct?.id ?: "",
        name     = name.trim(),
        category = category.trim(),
        price    = price.trim(),
        imageUrl = existingProduct?.imageUrl ?: ""
    )

    if (isEditMode) productViewModel.updateProduct(product, finalUri, finalUrl, context)
    else            productViewModel.addProduct(product, finalUri, finalUrl, context)
}

private val EaseOutCubic = Easing { t -> 1 - (1 - t) * (1 - t) * (1 - t) }

@Composable
private fun isSystemInDarkTheme() = androidx.compose.foundation.isSystemInDarkTheme()