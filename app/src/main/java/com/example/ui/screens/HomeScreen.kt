package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ListingEntity
import com.example.ui.components.NexKartFullLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val listings by viewModel.listingsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val conditionFilter by viewModel.conditionFilter.collectAsState()
    val maxRadiusKm by viewModel.maxRadiusKm.collectAsState()
    val wishlistedIds by viewModel.wishlistedIds.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showGeoSheet by remember { mutableStateOf(false) }
    var tempRadius by remember { mutableStateOf(maxRadiusKm ?: 25.0) }
    var zipInput by remember { mutableStateOf("") }

    val categories = listOf("All", "Fashion & Clothing", "Electronics & Gadgets", "Shoes & Footwear", "Beautiful Products")
    val conditions = listOf("All", "New", "Like New", "Good", "Fair")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(DarkBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Brand Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NexKartFullLogo()

                    Row {
                        IconButton(
                            onClick = { viewModel.refreshMarketplace() },
                            modifier = Modifier.testTag("refresh_catalog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Catalog List",
                                tint = NeonMint
                            )
                        }
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Dashboard", tint = WhitePure)
                        }
                        IconButton(onClick = { viewModel.togglePremiumStatus() }) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = "Premium Tier Status",
                                tint = if (isPremium) GoldCrown else SlateGrey
                            )
                        }
                    }
                }

                // Search Bar with Integrated Geo Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search shoes, gadget models...", color = SlateGrey, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonMint) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = SlateGrey)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("search_bar_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Location Filter Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (maxRadiusKm != null || zipInput.isNotEmpty()) NeonMint.copy(alpha = 0.2f) else DarkSurface)
                            .clickable { showGeoSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Location Filtering",
                            tint = if (maxRadiusKm != null || zipInput.isNotEmpty()) NeonMint else WhitePure
                        )
                    }
                }

                // Category Tabs
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) NeonMint else DarkSurface)
                                .clickable { viewModel.selectedCategory.value = cat }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) BlackPure else WhitePure,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPost,
                containerColor = NeonMint,
                contentColor = BlackPure,
                modifier = Modifier.padding(bottom = 8.dp).testTag("post_ad_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Post Item")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post Ad", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = isRefreshing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = NeonMint,
                    trackColor = DarkBackground
                )
            }
            // Hot Sponsor Banner (Google AdSense Slot Mock)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(DarkSurface, DarkSurfaceCard)))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(GoldCrown.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Text("ADSENSE SLOT", color = GoldCrown, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Bump your listings to Top Placement. Plans start from $4.99!",
                        color = SlateGrey,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Condition Filtering tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Condition: ", color = SlateGrey, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(conditions) { cond ->
                        val active = conditionFilter == cond
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) CyberCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { viewModel.conditionFilter.value = cond }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cond,
                                color = if (active) CyberCyan else SlateGrey,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Main Product Grid list
            if (listings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "No items", tint = SlateGrey, modifier = Modifier.size(48.dp))
                        Text("No listings match your search options", color = SlateGrey, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.refreshMarketplace() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = NeonMint),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset & Refresh Catalog", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(listings) { item ->
                        val isWish = wishlistedIds.contains(item.id)
                        ProductGridCard(
                            item = item,
                            isWishlisted = isWish,
                            onItemClick = { onNavigateToDetails(item.id) },
                            onWishlistToggle = { viewModel.toggleWishlist(item.id) }
                        )
                    }
                }
            }
        }
    }

    // Geolocation Search sheet modal
    if (showGeoSheet) {
        AlertDialog(
            onDismissRequest = { showGeoSheet = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.maxRadiusKm.value = tempRadius
                        viewModel.zipCodeFilter.value = if (zipInput.isEmpty()) null else zipInput
                        showGeoSheet = false
                    }
                ) {
                    Text("Apply Search", color = NeonMint, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.maxRadiusKm.value = null
                        viewModel.zipCodeFilter.value = null
                        zipInput = ""
                        showGeoSheet = false
                    }
                ) {
                    Text("Reset Filters", color = SlateGrey)
                }
            },
            title = { Text("Local Deals Filters", color = WhitePure, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text("Pincode / ZIP Filtering", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = zipInput,
                        onValueChange = { zipInput = it },
                        placeholder = { Text("e.g. 10012 or 11201", color = SlateGrey, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Search Radius", color = SlateGrey, fontSize = 12.sp)
                        Text("${tempRadius.toInt()} KM", color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = tempRadius.toFloat(),
                        onValueChange = { tempRadius = it.toDouble() },
                        valueRange = 5f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = DarkBackground
                        )
                    )
                    Text(
                        "Nexkart filters listings live using geographic Haversine trigonometry based on your virtual location.",
                        color = SlateGrey,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ProductGridCard(
    item: ListingEntity,
    isWishlisted: Boolean,
    onItemClick: () -> Unit,
    onWishlistToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("product_item_card_${item.id}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Product image
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            // Badges Overlays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Condition Grade
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BlackPure.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(item.condition, color = WhitePure, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Wishlist Heart Indicator
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BlackPure.copy(alpha = 0.4f))
                        .clickable { onWishlistToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) AlertCoral else WhitePure,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Featured Ribbon
            if (item.isFeatured) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldCrown)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("FEATURED", color = BlackPure, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Body Text Information
        Column(modifier = Modifier.padding(10.dp)) {
            // Category header text
            Text(item.category, color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                item.title,
                color = WhitePure,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Spec Info preview line based on Category
            val specSummary = when (item.category) {
                "Shoes & Footwear" -> "Size: ${item.sizeChart}"
                "Fashion & Clothing" -> "Brand: ${item.brand}"
                "Electronics & Gadgets" -> item.warranty
                else -> "${item.material}"
            }
            if (specSummary.isNotEmpty()) {
                Text(specSummary, color = SlateGrey, fontSize = 10.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${item.price}",
                    color = NeonMint,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = GoldCrown, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(item.sellerRating.toString(), color = WhitePure, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = "Map", tint = SlateGrey, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    item.locationName,
                    color = SlateGrey,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
