package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.database.AppDatabase
import com.example.data.model.ListingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    listingId: Int,
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Int) -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    var product by remember { mutableStateOf<ListingEntity?>(null) }
    val wishlistedIds by viewModel.wishlistedIds.collectAsState()
    val allListings by viewModel.listingsList.collectAsState()
    val isWish = wishlistedIds.contains(listingId)
    val app = viewModel.getApplication<android.app.Application>()
    val coroutineScope = rememberCoroutineScope()

    var showOfferDialog by remember { mutableStateOf(false) }
    var offerPriceInput by remember { mutableStateOf("") }
    
    LaunchedEffect(listingId) {
        val database = AppDatabase.getDatabase(app)
        product = database.marketplaceDao().getListingById(listingId)
        viewModel.selectListingForChat(listingId)
        viewModel.addToBrowsingHistory(listingId)
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonMint)
        }
        return
    }

    val item = product!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Details", color = WhitePure, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhitePure)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist(item.id) }) {
                        Icon(
                            imageVector = if (isWish) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isWish) AlertCoral else WhitePure
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            // Dual actions bottom rail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bidding sliding trigger
                Button(
                    onClick = { 
                        offerPriceInput = (item.price * 0.9).toInt().toString() // Preload 10% lower
                        showOfferDialog = true 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = CyberCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("make_offer_trigger")
                ) {
                    Icon(Icons.Default.LocalOffer, contentDescription = "Bargain")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Make an Offer", fontWeight = FontWeight.Bold)
                }

                // Chat direct shortcut
                Button(
                    onClick = { onNavigateToChat(item.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("chat_seller_trigger")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Sellers")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("In-App Chat", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Immersive Gallery Spot
            val urls = remember(item.galleryUrls, item.imageUrl) {
                if (item.galleryUrls.isNotEmpty()) {
                    item.galleryUrls.split(",").filter { it.isNotEmpty() }
                } else {
                    listOf(item.imageUrl)
                }
            }
            var activeImageIndex by remember { mutableStateOf(0) }
            val safeIndex = if (activeImageIndex in urls.indices) activeImageIndex else 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = urls.getOrNull(safeIndex) ?: item.imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category overlay chip
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(item.category, color = BlackPure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Photo quantity indicator overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Photo ${safeIndex + 1} of ${urls.size}",
                        color = NeonMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (urls.size > 1) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(urls.size) { idx ->
                        val isSel = safeIndex == idx
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, if (isSel) NeonMint else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { activeImageIndex = idx }
                        ) {
                            AsyncImage(
                                model = urls[idx],
                                contentDescription = "Gallery Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Main Info Group
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = WhitePure,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = "ZIP", tint = SlateGrey, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Location: ${item.locationName} (${item.zipCode})",
                                color = SlateGrey,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "$${item.price}",
                        color = NeonMint,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DarkSurfaceCard)
                Spacer(modifier = Modifier.height(16.dp))

                // Seller Trust Card
                Text("Seller Information", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CyberCyan, NeonMint))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.sellerName.take(2).uppercase(),
                            color = BlackPure,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.sellerName, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (item.isVerifiedSeller) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonMint.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("VERIFIED SELLER", color = NeonMint, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row {
                                repeat(5) { starIndex ->
                                    val checked = starIndex < item.sellerRating.toInt()
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = if (checked) GoldCrown else DarkBackground,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.sellerRating.toString(), color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(10.dp))
                            // Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("QUICK RESPONDER", color = CyberCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Specific Attributes Section per Category
                Text("Item Specifications", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecRow(label = "Condition", value = item.condition)
                    
                    if (item.brand.isNotEmpty()) {
                        SpecRow(label = "Brand", value = item.brand)
                    }

                    when (item.category) {
                        "Fashion & Clothing" -> {
                            SpecRow(label = "Fit / Sizes", value = item.sizeChart)
                            SpecRow(label = "Composition", value = item.material)
                        }
                        "Shoes & Footwear" -> {
                            SpecRow(label = "Shoe Sizes (UK/US)", value = item.sizeChart)
                            SpecRow(label = "Structure / Sole", value = item.material)
                        }
                        "Electronics & Gadgets" -> {
                            SpecRow(label = "Technical Specs", value = item.techSpecs)
                            SpecRow(label = "Device Age", value = item.deviceAge)
                            SpecRow(label = "Seller Warranty", value = item.warranty)
                        }
                        "Beautiful Products" -> {
                            SpecRow(label = "Aesthetic Styles", value = "Modern Scandinavian Art")
                            SpecRow(label = "Materials", value = item.material)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description Box
                Text("Detailed Description", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(14.dp)
                ) {
                    Text(
                        text = item.description,
                        color = WhitePure,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Safety Guarantee
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AlertCoral.copy(alpha = 0.08f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = AlertCoral,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Nexkart Safety Escrow Guaranteed", color = AlertCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "Avoid direct wire transactions. Always make offers and converse safely using our local bidding tools to retain protection.",
                            color = SlateGrey,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Similar Listings Shelf
                val similarListings = remember(item, allListings) {
                    com.example.data.repository.RecommendationEngine.getSimilarListings(item, allListings, limit = 5)
                }

                if (similarListings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Similar Deals Near You",
                        color = NeonMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("similar_deals_header")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().testTag("similar_listings_row"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(similarListings.size) { index ->
                            val simItem = similarListings[index]
                            Card(
                                onClick = { onNavigateToDetails(simItem.id) },
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                modifier = Modifier
                                    .width(155.dp)
                                    .testTag("similar_item_card_${simItem.id}"),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SlateGrey.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DarkSurfaceCard),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = simItem.imageUrl,
                                            contentDescription = simItem.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (simItem.isVerifiedSeller) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified",
                                                    tint = NeonMint,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = simItem.title,
                                        color = WhitePure,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                        )
                                    Text(
                                        text = "${simItem.brand} • ${simItem.condition}",
                                        color = SlateGrey,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$${simItem.price}",
                                        color = NeonMint,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Making an Offer bidding picker dialog
    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val bidAmt = offerPriceInput.toDoubleOrNull() ?: 0.0
                        if (bidAmt > 0.0) {
                            viewModel.handleMakeOffer(item.id, bidAmt)
                            viewModel.handleSendMessage(item.id, "Hi! I made an offer of $$bidAmt for your ${item.title}. Let me know if you can accept.")
                            showOfferDialog = false
                            onNavigateToChat(item.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure)
                ) {
                    Text("Submit Offer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfferDialog = false }) {
                    Text("Cancel", color = SlateGrey)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddModerator, contentDescription = "Shield", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bargaining Table", color = WhitePure, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Original Listing Price: $${item.price}", color = SlateGrey, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Enter Bidding Offer ($)", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = offerPriceInput,
                        onValueChange = { offerPriceInput = it },
                        placeholder = { Text("e.g. 500", color = SlateGrey) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Bidding presets row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.9, 0.85, 0.8).forEach { multiplier ->
                            val amt = (item.price * multiplier).toInt()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkBackground)
                                    .clickable { offerPriceInput = amt.toString() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$${amt}\n(-${((1-multiplier)*100).toInt()}%)", color = CyberCyan, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        "Offers submitted here create a structured active bargaining agreement block directly inside the in-app chat thread.",
                        color = SlateGrey,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = SlateGrey, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}
