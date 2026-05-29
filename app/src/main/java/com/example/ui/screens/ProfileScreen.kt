package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ListingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToPost: () -> Unit
) {
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val myListings by viewModel.myListingsList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Dynamic Profile fields
    val profileName by viewModel.userProfileName.collectAsState()
    val profileImage by viewModel.userProfileImage.collectAsState()
    val profileEmail by viewModel.userProfileEmail.collectAsState()
    val profileMobile by viewModel.userProfileMobile.collectAsState()
    val profileAddress by viewModel.userProfileAddress.collectAsState()
    val profilePincode by viewModel.userProfilePincode.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf<Int?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val portfolioPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.addProfileGalleryPhoto(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = WhitePure, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshMarketplace() },
                        modifier = Modifier.testTag("refresh_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Profile Data",
                            tint = NeonMint
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Metadata",
                            tint = NeonMint
                        )
                    }

                    IconButton(onClick = { viewModel.togglePremiumStatus() }) {
                        Icon(
                            imageVector = if (isPremium) Icons.Default.Stars else Icons.Default.StarOutline,
                            contentDescription = "Toggle Premium Membership",
                            tint = if (isPremium) GoldCrown else SlateGrey
                        )
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("profile_screen_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
            }

            // Profile Card Segment
            item {
                ProfileHeaderCard(
                    profileName = profileName,
                    profileImage = profileImage,
                    isPremium = isPremium,
                    onEditProfileClick = { showEditProfileDialog = true }
                )
            }

            // Stats row Segment
            item {
                ProfileQuickStatsRow(
                    isPremium = isPremium,
                    listingCount = myListings.size
                )
            }

            // Address, Email, Mobile, Pincode card
            item {
                ProfileDetailsCard(
                    email = profileEmail,
                    mobile = profileMobile,
                    address = profileAddress,
                    pincode = profilePincode,
                    onEditClick = { showEditProfileDialog = true }
                )
            }

            // Profile Photo Gallery upload system
            item {
                val userGalleryStr by viewModel.userProfileGallery.collectAsState()
                val galleryUrls = remember(userGalleryStr) {
                    userGalleryStr.split(",").filter { it.isNotEmpty() }
                }

                var showAddPhotoDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonMint.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = NeonMint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "My Photo Portfolio Gallery",
                                    color = WhitePure,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Button(
                                onClick = { showAddPhotoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp).testTag("add_profile_gallery_photo")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Text(
                            text = "Showcase your professional seller badge, verified location selfies, or aesthetic store vibe photos to build trust with buyers.",
                            color = SlateGrey,
                            fontSize = 11.sp
                        )

                        if (galleryUrls.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .border(1.dp, SlateGrey.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Your profile gallery is empty", color = SlateGrey, fontSize = 12.sp)
                            }
                        } else {
                            // Beautiful Grid of Gallery Photos
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 3
                            ) {
                                galleryUrls.forEach { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, SlateGrey.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Profile Gallery Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Delete button on picture
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red.copy(alpha = 0.85f))
                                                .clickable { viewModel.removeProfileGalleryPhoto(url) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = WhitePure,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddPhotoDialog) {
                    var newPhotoUrl by remember { mutableStateOf("") }
                    val stockAvatarSuggestions = listOf(
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80",
                        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80",
                        "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=300&q=80"
                    )

                    AlertDialog(
                        onDismissRequest = { showAddPhotoDialog = false },
                        title = { Text("Upload Profile Photos", color = WhitePure, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        portfolioPhotoLauncher.launch("image/*")
                                        showAddPhotoDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = BlackPure),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(45.dp)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pick from Device Gallery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = SlateGrey.copy(alpha = 0.3f))
                                    Text(" OR ", color = SlateGrey, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = SlateGrey.copy(alpha = 0.3f))
                                }

                                Text("Choose from high-definition stock presets or paste a custom link to simulated upload.", color = SlateGrey, fontSize = 12.sp)
                                
                                Text("Select a Stock Preset photo:", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    stockAvatarSuggestions.forEach { imgUrl ->
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    2.dp,
                                                    if (newPhotoUrl == imgUrl) NeonMint else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { newPhotoUrl = imgUrl }
                                        ) {
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = "Avatar Suggestion",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Or input custom image web URL:", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                TextField(
                                    value = newPhotoUrl,
                                    onValueChange = { newPhotoUrl = it },
                                    placeholder = { Text("https://example.com/item.jpg", color = SlateGrey) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground, focusedTextColor = WhitePure, unfocusedTextColor = WhitePure),
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newPhotoUrl.isNotEmpty()) {
                                        viewModel.addProfileGalleryPhoto(newPhotoUrl)
                                        showAddPhotoDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure)
                            ) {
                                Text("Save Portfolio", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddPhotoDialog = false }) {
                                Text("Cancel", color = SlateGrey)
                            }
                        },
                        containerColor = DarkSurfaceCard
                    )
                }
            }

            // Section Header: My Uploads
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = "My Uploads Icon",
                            tint = NeonMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "My Uploaded Listings",
                            color = WhitePure,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = "(${myListings.size})",
                        color = CyberCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Check if user has uploaded anything
            if (myListings.isEmpty()) {
                item {
                    UploadEmptyState(onNavigateToPost = onNavigateToPost)
                }
            } else {
                items(myListings, key = { it.id }) { listing ->
                    MyUploadedListingItem(
                        listing = listing,
                        onViewDetails = { onNavigateToDetails(listing.id) },
                        onDeleteClick = { showDeleteConfirmDialog = listing.id }
                    )
                }
            }
        }
    }

    // Deletion Modal Dialog
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = DarkSurface,
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete Icon",
                    tint = AlertCoral,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Listing?",
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently remove this listing from the C2C Marketplace? This action cannot be undone.",
                    color = SlateGrey,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog?.let { id ->
                            viewModel.handleDeleteListing(id)
                        }
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCoral, contentColor = BlackPure)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = WhitePure)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Profile Modal Sheet/Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = profileName,
            initialImage = profileImage,
            initialEmail = profileEmail,
            initialMobile = profileMobile,
            initialAddress = profileAddress,
            initialPincode = profilePincode,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, image, email, mobile, address, pincode ->
                viewModel.updateProfile(name, image, email, mobile, address, pincode)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun ProfileHeaderCard(
    profileName: String,
    profileImage: String,
    isPremium: Boolean,
    onEditProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isPremium) GoldCrown.copy(alpha = 0.08f) else NeonMint.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Glow avatar profile circle with custom image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                if (isPremium) listOf(GoldCrown, CyberCyan) else listOf(NeonMint, CyberCyan)
                            ),
                            shape = CircleShape
                        )
                        .background(DarkSurfaceCard)
                        .clickable(onClick = onEditProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImage.isNotEmpty() && profileImage.startsWith("http")) {
                        AsyncImage(
                            model = profileImage,
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar Placeholder",
                            tint = if (isPremium) GoldCrown else CyberCyan,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    
                    // Small overlay edit badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(DarkBackground, CircleShape)
                            .border(1.dp, NeonMint, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = NeonMint,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Profile descriptions
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.clickable(onClick = onEditProfileClick),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = profileName,
                            color = WhitePure,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Identity Symbol",
                            tint = NeonMint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Verified Marketplace Citizen",
                        color = SlateGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tier Badge Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPremium) GoldCrown.copy(alpha = 0.15f) else DarkSurfaceCard,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isPremium) GoldCrown.copy(alpha = 0.3f) else SlateGrey.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPremium) Icons.Default.Stars else Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (isPremium) GoldCrown else NeonMint,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isPremium) "Premium Member" else "Standard Level Tier",
                                color = if (isPremium) GoldCrown else CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileQuickStatsRow(
    isPremium: Boolean,
    listingCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Star reviews card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(84.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Seller Rating", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("4.9", color = WhitePure, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star score indicator",
                        tint = GoldCrown,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Active listing counters
        Card(
            modifier = Modifier
                .weight(1f)
                .height(84.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Uploaded Ads", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$listingCount",
                    color = NeonMint,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Limit Quota card indicator
        Card(
            modifier = Modifier
                .weight(1f)
                .height(84.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Quota Limit", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPremium) "∞ / Unlimited" else "$listingCount / 3",
                    color = if (isPremium) GoldCrown else CyberCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileDetailsCard(
    email: String,
    mobile: String,
    address: String,
    pincode: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_details_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contact & Shipping Address",
                    color = WhitePure,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Edit Info",
                    color = NeonMint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            HorizontalDivider(color = SlateGrey.copy(alpha = 0.15f))

            // Email Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email icon",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text("Email Address", color = SlateGrey, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = email.ifEmpty { "Not Added" },
                        color = if (email.isEmpty()) AlertCoral else WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Mobile Number Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone icon",
                        tint = NeonMint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text("Mobile Number", color = SlateGrey, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = mobile.ifEmpty { "Not Added" },
                        color = if (mobile.isEmpty()) AlertCoral else WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Address Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address icon",
                        tint = GoldCrown,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text("Primary Address", color = SlateGrey, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = address.ifEmpty { "Not Added" },
                        color = if (address.isEmpty()) AlertCoral else WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Pincode/Zip Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(DarkSurfaceCard, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PinDrop,
                        contentDescription = "Pincode icon",
                        tint = AlertCoral,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text("Pincode / Postal Zip Code", color = SlateGrey, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = pincode.ifEmpty { "Not Added" },
                        color = if (pincode.isEmpty()) AlertCoral else WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun UploadEmptyState(
    onNavigateToPost: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SlateGrey.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(DarkSurfaceCard, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = SlateGrey,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "No active uploads",
                color = WhitePure,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "You haven't posted any classified ads yet. Publish list items in electronics, lifestyle, fashion, or shoes category to view them here.",
                color = SlateGrey,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onNavigateToPost,
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post First Listing", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MyUploadedListingItem(
    listing: ListingEntity,
    onViewDetails: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onViewDetails)
            .testTag("user_listing_${listing.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Uploaded product image
            AsyncImage(
                model = listing.imageUrl,
                contentDescription = "Listing Thumbnail",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceCard),
                contentScale = ContentScale.Crop
            )

            // Content info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = listing.title,
                    color = WhitePure,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = listing.category,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = SlateGrey,
                        fontSize = 10.sp
                    )
                    Text(
                        text = listing.condition,
                        color = if (listing.condition.equals("New", ignoreCase = true)) NeonMint else SlateGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "$${listing.price}",
                        color = NeonMint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    // Show views indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views icon",
                            tint = SlateGrey,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${listing.views}",
                            color = SlateGrey,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick deletion button targets
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkSurfaceCard, CircleShape)
                    .testTag("delete_btn_${listing.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Ad permanently",
                    tint = AlertCoral,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    initialName: String,
    initialImage: String,
    initialEmail: String,
    initialMobile: String,
    initialAddress: String,
    initialPincode: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var imageUrl by remember { mutableStateOf(initialImage) }
    var email by remember { mutableStateOf(initialEmail) }
    var mobile by remember { mutableStateOf(initialMobile) }
    var address by remember { mutableStateOf(initialAddress) }
    var pincode by remember { mutableStateOf(initialPincode) }

    // Upload simulation states
    var isUploadingSimulated by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Highly aesthetic curated avatar presets
    val avatarPresets = listOf(
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80" to "Modern Tech",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80" to "Creative",
        "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=300&q=80" to "Studio",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=300&q=80" to "Minimalist",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80" to "Corporate",
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80" to "Casual"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Update Profile Metadata",
                color = WhitePure,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
        },
        containerColor = DarkSurface,
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .testTag("edit_profile_dialog_scroll"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Profile Image Upload System
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Profile Image Selection & Upload",
                            color = NeonMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Selected Avatar Preview
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, CyberCyan, CircleShape)
                                    .background(DarkSurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = SlateGrey)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isUploadingSimulated = true
                                            uploadStatusMessage = "Connecting to file explorer..."
                                            delay(500)
                                            uploadStatusMessage = "Compressing profile image asset..."
                                            delay(600)
                                            uploadStatusMessage = "Uploading secure cloud token..."
                                            delay(700)
                                            
                                            // Assign a randomized beautiful avatar upon upload completion
                                            val randomIndex = (0..5).random()
                                            imageUrl = avatarPresets[randomIndex].first
                                            
                                            uploadStatusMessage = "Asset added successfully!"
                                            delay(400)
                                            isUploadingSimulated = false
                                            uploadStatusMessage = ""
                                        }
                                    },
                                    enabled = !isUploadingSimulated,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurfaceCard,
                                        contentColor = NeonMint
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulate Upload File", fontSize = 12.sp)
                                }
                                
                                Text(
                                    "Supports PNG, JPG. Max 2MB size.",
                                    color = SlateGrey,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Status log for simulation
                        AnimatedVisibility(
                            visible = isUploadingSimulated || uploadStatusMessage.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyberCyan.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = CyberCyan,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = uploadStatusMessage,
                                        color = CyberCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Presets
                        Text("Or choose an instant Avatar Preset:", color = SlateGrey, fontSize = 11.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(avatarPresets) { (url, label) ->
                                val isSelected = imageUrl == url
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { imageUrl = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = label,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) NeonMint else SlateGrey.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        label,
                                        color = if (isSelected) NeonMint else SlateGrey,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Input Information Fields
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = SlateGrey.copy(alpha = 0.4f),
                            focusedLabelColor = NeonMint,
                            unfocusedLabelColor = SlateGrey,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Custom Profile Image URL") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = SlateGrey.copy(alpha = 0.4f),
                            focusedLabelColor = NeonMint,
                            unfocusedLabelColor = SlateGrey,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_image_url")
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email ID") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = SlateGrey.copy(alpha = 0.4f),
                            focusedLabelColor = NeonMint,
                            unfocusedLabelColor = SlateGrey,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_email")
                    )
                }

                item {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = SlateGrey.copy(alpha = 0.4f),
                            focusedLabelColor = NeonMint,
                            unfocusedLabelColor = SlateGrey,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_mobile")
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Primary Shipping Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = SlateGrey.copy(alpha = 0.4f),
                            focusedLabelColor = NeonMint,
                            unfocusedLabelColor = SlateGrey,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_address")
                    )
                }

                item {
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it },
                        label = { Text("Pincode / Postal Zip") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMint,
                            unfocusedBorderColor = SlateGrey.copy(alpha = 0.4f),
                            focusedLabelColor = NeonMint,
                            unfocusedLabelColor = SlateGrey,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_pincode")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, imageUrl, email, mobile, address, pincode) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = WhitePure)
            ) {
                Text("Cancel")
            }
        }
    )
}
