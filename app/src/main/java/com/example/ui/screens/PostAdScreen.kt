package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAdScreen(
    viewModel: MarketplaceViewModel,
    onNavigateHome: () -> Unit
) {
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val postingCount by viewModel.userPostingsCount.collectAsState()

    val initialDescription by viewModel.aiGeneratedDescriptionText.collectAsState()
    val isGeneratingDescription by viewModel.isGeneratingDescription.collectAsState()
    val isRemovingBg by viewModel.isRemovingBackground.collectAsState()
    val bgState by viewModel.imageProcessingState.collectAsState()

    // AI Auto-Enhancement state values collected from View Model
    val brightness by viewModel.imageBrightness.collectAsState()
    val contrast by viewModel.imageLuminosity.collectAsState()
    val sharpness by viewModel.imageSharpness.collectAsState()
    val isEnhancingImage by viewModel.isEnhancingImage.collectAsState()
    val enhancementMessage by viewModel.enhancementMessage.collectAsState()
    val ipEnhanceState by viewModel.ipEnhanceState.collectAsState()

    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fashion & Clothing") }
    var locationName by remember { mutableStateOf("New York") }
    var zipCode by remember { mutableStateOf("10001") }
    var description by remember { mutableStateOf("") }

    // Custom Fields
    var brand by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("New") }
    var sizeChart by remember { mutableStateOf("") } // Used for Fashion or Shoes
    var techSpecs by remember { mutableStateOf("") } // Tech
    var warranty by remember { mutableStateOf("") } // Tech
    var deviceAge by remember { mutableStateOf("") } // Tech
    var material by remember { mutableStateOf("") } // Lifestyle or Fashion or Shoes

    // Sync generated AI description
    LaunchedEffect(initialDescription) {
        if (initialDescription.isNotEmpty()) {
            description = initialDescription
        }
    }

    // List of static mock presets we can randomize for image upload
    val mockImagePresets = listOf(
        "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500", // Jacket
        "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500", // Phone
        "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500", // Jordan
        "https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=500"  // Vase
    )
    var selectedImgPresetIndex by remember { mutableStateOf(0) }
    var selectedGalleryUris by remember { mutableStateOf(listOf(mockImagePresets[0])) }

    val categories = listOf("Fashion & Clothing", "Electronics & Gadgets", "Shoes & Footwear", "Beautiful Products")
    val conditions = listOf("New", "Like New", "Good", "Fair")

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publish Listing", color = WhitePure, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
                actions = {
                    TextButton(onClick = { viewModel.togglePremiumStatus() }) {
                        Text(
                            text = if (isPremium) "Tier: Premium" else "Tier: Free (3 Max)",
                            color = if (isPremium) GoldCrown else CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Posting usage telemetry
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isPremium) GoldCrown.copy(alpha = 0.5f) else NeonMint.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPremium) "👑 Primary Premium Business Account" else "Standard Level Tier Account",
                            color = if (isPremium) GoldCrown else WhitePure,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Free monthly quota: $postingCount / 3 listings posted.",
                            color = SlateGrey,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (!isPremium) {
                        Button(
                            onClick = { viewModel.togglePremiumStatus() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldCrown, contentColor = BlackPure),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Upgrade", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Image Picker Mock Container
            Text("Product Demonstration Asset", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Image Frame Showcase representation
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    val matrix = ColorMatrix(floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness * 255f,
                        0f, contrast, 0f, 0f, brightness * 255f,
                        0f, 0f, contrast, 0f, brightness * 255f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    AsyncImage(
                        model = mockImagePresets[selectedImgPresetIndex],
                        contentDescription = "Active product image",
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(matrix),
                        modifier = Modifier.fillMaxSize().blurIfProcessing(bgState)
                    )
                    
                    if (bgState == "Removed") {
                        // High-contrast clean overlay outline to show bg removal
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.dp, NeonMint, RoundedCornerShape(16.dp))
                                .background(Brush.radialGradient(listOf(NeonMint.copy(alpha = 0.1f), Color.Transparent)))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(NeonMint)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("BG ISOLATED", color = BlackPure, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    } else if (ipEnhanceState == "Optimized") {
                        // Cyan accent outline indicating AI quality enhancement is active
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.dp, CyberCyan, RoundedCornerShape(16.dp))
                                .background(Brush.radialGradient(listOf(CyberCyan.copy(alpha = 0.1f), Color.Transparent)))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(CyberCyan)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("AI OPTIMIZED", color = BlackPure, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // AI Processing action buttons
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select Stock Image Type:", color = SlateGrey, fontSize = 10.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        mockImagePresets.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedImgPresetIndex == idx) NeonMint else DarkSurface)
                                    .border(1.dp, SlateGrey, CircleShape)
                                    .clickable { 
                                        selectedImgPresetIndex = idx 
                                        selectedGalleryUris = listOf(mockImagePresets[idx])
                                        viewModel.resetAiBackgroundRemover()
                                        viewModel.resetAiImageEnhancer()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text((idx + 1).toString(), color = if (selectedImgPresetIndex == idx) BlackPure else WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Simulated AI Background remover
                    Button(
                        onClick = { viewModel.runAiBackgroundRemover() },
                        enabled = !isRemovingBg,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BlackPure),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("ai_bg_remover_btn")
                    ) {
                        if (isRemovingBg) {
                            CircularProgressIndicator(color = BlackPure, modifier = Modifier.size(12.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flip, contentDescription = "Cutout", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BG Cutout", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // AI Image Enhancer button
                    Button(
                        onClick = { viewModel.runAiImageEnhancer() },
                        enabled = !isEnhancingImage,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("ai_image_enhancer_btn")
                    ) {
                        if (isEnhancingImage) {
                            CircularProgressIndicator(color = BlackPure, modifier = Modifier.size(12.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Enhance", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Auto-Enhance", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // AI status updates
            if (enhancementMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isEnhancingImage) CyberCyan.copy(alpha = 0.08f) else NeonMint.copy(alpha = 0.08f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEnhancingImage) {
                            CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Icon(Icons.Default.Done, contentDescription = "Done", tint = NeonMint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = enhancementMessage,
                            color = if (isEnhancingImage) CyberCyan else NeonMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Sliders block 
            Text("Adjust Quality Parameters", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Slider 1: Brightness
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brightness Boost", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = "${(brightness * 100).toInt()}%", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = brightness,
                        onValueChange = { viewModel.imageBrightness.value = it },
                        valueRange = -0.3f..0.3f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonMint,
                            activeTrackColor = NeonMint,
                            inactiveTrackColor = SlateGrey.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(24.dp).testTag("brightness_slider")
                    )
                }

                // Slider 2: Contrast Depth
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Contrast Depth", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = "${(contrast * 100).toInt()}%", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = contrast,
                        onValueChange = { viewModel.imageLuminosity.value = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = SlateGrey.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(24.dp).testTag("contrast_slider")
                    )
                }

                // Slider 3: Edge Sharpness Map
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sharpness Detail Level", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = "${(sharpness * 100).toInt()}%", color = AlertCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = sharpness,
                        onValueChange = { viewModel.imageSharpness.value = it },
                        valueRange = 1.0f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AlertCoral,
                            activeTrackColor = AlertCoral,
                            inactiveTrackColor = SlateGrey.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(24.dp).testTag("sharpness_slider")
                    )
                }
            }

            // Dynamic Interactive Multiple-Photos Product Gallery Upload System
            Text("Product Gallery Photos (Multiple Preset / Web URL Uploads)", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Horizontal list of added gallery photos
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(selectedGalleryUris.size) { index ->
                        val url = selectedGalleryUris[index]
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (index == 0) NeonMint else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Gallery photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (index == 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(NeonMint.copy(alpha = 0.8f))
                                        .padding(vertical = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("COVER", color = BlackPure, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            // Delete photo button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .clickable {
                                        if (selectedGalleryUris.size > 1) {
                                            selectedGalleryUris = selectedGalleryUris - url
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = WhitePure, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                    
                    // Add Photo trigger card
                    item {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, SlateGrey, RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .clickable {
                                    // Add a random preset photo as a simulated dynamic upload
                                    val randomPreset = mockImagePresets.random()
                                    selectedGalleryUris = selectedGalleryUris + randomPreset
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Add Stock", color = SlateGrey, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Custom URL Input simulation to upload ANY dynamic photo by system link
                var customPhotoUrl by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextField(
                        value = customPhotoUrl,
                        onValueChange = { customPhotoUrl = it },
                        placeholder = { Text("Or paste image web URL to upload...", fontSize = 10.sp, color = SlateGrey) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                        modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(6.dp)),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (customPhotoUrl.isNotEmpty()) {
                                selectedGalleryUris = selectedGalleryUris + customPhotoUrl
                                customPhotoUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BlackPure),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Add URL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // General Fields
            Text("General Listing Fields", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Heading Title", color = SlateGrey) },
                colors = TextFieldDefaults.colors(focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).testTag("post_title_field")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price ($)", color = SlateGrey) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).testTag("post_price_field")
                )
                
                TextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = { Text("Pincode / ZIP", color = SlateGrey) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                )
            }

            // Category selector dropdown simulated
            Column {
                Text("Category Placement", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = category == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyberCyan else DarkSurface)
                                .clickable { category = cat }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.replace(" & ", "\n").replace(" ", "\n"),
                                color = if (isSel) BlackPure else SlateGrey,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Category Custom Fields block
            Text("Dynamic specifications for Category: $category", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Condition
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Select Condition Grade: ", color = SlateGrey, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        conditions.forEach { cond ->
                            val active = condition == cond
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (active) NeonMint else DarkBackground)
                                    .clickable { condition = cond }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(cond, color = if (active) BlackPure else SlateGrey, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Dynamic inputs depending on classification Type
                when (category) {
                    "Fashion & Clothing" -> {
                        TextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand (e.g. Ralph Lauren, Levi's)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = sizeChart,
                            onValueChange = { sizeChart = it },
                            label = { Text("Size & Silhouette (e.g. US Medium, Slim-Fit)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Materials (e.g. Organic Wool Linen, Silk)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "Shoes & Footwear" -> {
                        TextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand name (e.g. Air Jordan, Adidas)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = sizeChart,
                            onValueChange = { sizeChart = it },
                            label = { Text("UK/US Shoe Sizes (e.g. US Men 10 / UK 9)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Build structure (e.g. Primeknit, Carbon sole)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "Electronics & Gadgets" -> {
                        TextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Technical specs brand (e.g. Google, Sony)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = techSpecs,
                            onValueChange = { techSpecs = it },
                            label = { Text("Technical specs (e.g. Tensor G3, 12GB RAM, 256G)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = warranty,
                                onValueChange = { warranty = it },
                                label = { Text("Warranty remaining") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                                modifier = Modifier.weight(1f)
                            )
                            TextField(
                                value = deviceAge,
                                onValueChange = { deviceAge = it },
                                label = { Text("Device usage age") },
                                colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    "Beautiful Products" -> {
                        TextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Artistic designer / Line label") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Materials composition (e.g. Sculptured clay, soy wax)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Description Write block with AI Trigger overlay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Item Listing Details Description", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                // AI Generate descriptions trigger (Uses direct REST or simulate)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.linearGradient(listOf(NeonMint, CyberCyan)))
                        .clickable {
                            val contextSpecs = when (category) {
                                "Electronics & Gadgets" -> techSpecs
                                "Fashion & Clothing" -> sizeChart
                                "Shoes & Footwear" -> sizeChart
                                else -> material
                            }
                            viewModel.runAiDescriptionGenerator(title, category, brand, condition, contextSpecs)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("ai_desc_generate_btn")
                ) {
                    if (isGeneratingDescription) {
                        CircularProgressIndicator(color = BlackPure, modifier = Modifier.size(12.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BlackPure, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Write Pitch", color = BlackPure, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("State measurements, minor details, and local pick-up guidelines clear in text...", color = SlateGrey) },
                colors = TextFieldDefaults.colors(focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)).testTag("post_description_field")
            )

            // Submit ad POST
            Button(
                onClick = {
                    val finalPriceText = price.toDoubleOrNull() ?: 0.0
                    viewModel.handlePostListing(
                        title = title,
                        description = description,
                        price = finalPriceText,
                        category = category,
                        imageUrl = selectedGalleryUris.firstOrNull() ?: mockImagePresets[selectedImgPresetIndex],
                        locationName = locationName,
                        zipCode = zipCode,
                        brand = brand,
                        condition = condition,
                        sizeChart = sizeChart,
                        techSpecs = techSpecs,
                        warranty = warranty,
                        deviceAge = deviceAge,
                        material = material,
                        galleryUrls = selectedGalleryUris.joinToString(","),
                        onSuccess = { showSuccessDialog = true },
                        onLimitReached = { showLimitDialog = true }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("publish_listing_btn")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = "Upload")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish and Boost Listing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Success Popover
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onNavigateHome()
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure)
                ) {
                    Text("OK")
                }
            },
            title = { Text("Listing Published! 🌟", color = WhitePure, fontWeight = FontWeight.Bold) },
            text = { Text("Your listing has been successfully saved to Room SQLite, and synced with Nexkart's smart geolocation search. Local buyers will now observe it on their home dashboard!", color = SlateGrey) },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Listing Limit alert dialog
    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.togglePremiumStatus()
                        showLimitDialog = false
                        showSuccessDialog = true // Trigger successful post now!
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldCrown, contentColor = BlackPure)
                ) {
                    Text("Upgrade to Unlimited 👑", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            },
            title = { Text("Listing Limit Reached!", color = AlertCoral, fontWeight = FontWeight.Bold) },
            text = { Text("Free Standard Accounts are restricted to 3 active listings per month. Upgrade to Nexkart Unlimited to list infinite product feeds, activate premium gold badges, and secure top placement results!", color = SlateGrey) },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Ext helper for blur simulation on background deletion
@Composable
fun Modifier.blurIfProcessing(bgState: String): Modifier {
    return if (bgState == "Removing...") {
        this.background(Color.DarkGray.copy(alpha = 0.5f))
    } else {
        this
    }
}
