package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.database.AppDatabase
import com.example.data.model.ListingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToChat: (Int) -> Unit
) {
    var reelsList by remember { mutableStateOf<List<ListingEntity>>(emptyList()) }
    var activeIndex by remember { mutableStateOf(0) }
    val app = viewModel.getApplication<android.app.Application>()
    val coroutineScope = rememberCoroutineScope()

    // Floating heart reaction trigger counts
    var heartSpawnCount by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val database = AppDatabase.getDatabase(app)
        // Load the 4 seed listings which contain high-quality reels urls
        database.marketplaceDao().getAllListings().collect { list ->
            reelsList = list.filter { it.videoUrl.isNotEmpty() }
        }
    }

    if (reelsList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NeonMint)
        }
        return
    }

    val safeIndex = activeIndex.coerceIn(0, reelsList.size - 1)
    val currentReel = reelsList[safeIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackPure)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.y < -50) {
                        // Swipe Up -> Next Video
                        if (activeIndex < reelsList.size - 1) {
                            activeIndex++
                            heartSpawnCount = 0
                        }
                    } else if (dragAmount.y > 50) {
                        // Swipe Down -> Prev Video
                        if (activeIndex > 0) {
                            activeIndex--
                            heartSpawnCount = 0
                        }
                    }
                }
            }
            .testTag("reels_feed_container")
    ) {
        // High-Quality Product Image serving as the Video Frame fallback
        AsyncImage(
            model = currentReel.imageUrl,
            contentDescription = "Visual product scan",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Vignette Backdrop Overlap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BlackPure.copy(alpha = 0.5f),
                            Color.Transparent,
                            BlackPure.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // double tap floating heart reactions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    heartSpawnCount++
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = heartSpawnCount > 0,
                enter = scaleIn(animationSpec = tween(150)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(300)) + fadeOut()
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Heart Pop",
                    tint = AlertCoral,
                    modifier = Modifier.size(100.dp)
                )
                
                LaunchedEffect(heartSpawnCount) {
                    if (heartSpawnCount > 0) {
                        delay(600)
                        heartSpawnCount = 0
                    }
                }
            }
        }

        // Top navigation indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", color = BlackPure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nexkart Reels", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Sound indicator
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BlackPure.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = "Sound Toggle",
                    tint = WhitePure
                )
            }
        }

        // Sidebar Actions Grid Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Likes Trigger
            ReelActionItem(
                icon = Icons.Default.Favorite,
                label = "${120 + safeIndex * 35}",
                tint = if (heartSpawnCount > 0) AlertCoral else WhitePure,
                onClick = { heartSpawnCount = 1 }
            )

            // Direct Details Click
            ReelActionItem(
                icon = Icons.Default.Loyalty,
                label = "Details",
                tint = CyberCyan,
                onClick = { onNavigateToDetails(currentReel.id) }
            )

            // Direct Chat shortcut
            ReelActionItem(
                icon = Icons.Default.ChatBubble,
                label = "Bargain",
                tint = NeonMint,
                onClick = { onNavigateToChat(currentReel.id) }
            )

            // Share icon
            ReelActionItem(
                icon = Icons.Default.Share,
                label = "Share",
                tint = WhitePure,
                onClick = {}
            )
        }

        // Bottom Details & Floating Product Card Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 16.dp, end = 100.dp)
        ) {
            // Seller Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyberCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentReel.sellerName.take(1), color = BlackPure, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(currentReel.sellerName, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Verified, contentDescription = "Verified Profile", tint = NeonMint, modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Listing summary caption
            Text(
                text = "${currentReel.title} - ${currentReel.condition} Grade",
                color = WhitePure,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentReel.description,
                color = SlateGrey,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Compact Floating product banner inside Reels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(currentReel.category, color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$${currentReel.price}", color = NeonMint, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Button(
                    onClick = { onNavigateToDetails(currentReel.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Direct Buy", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(10.dp))
                }
            }
        }

        // Scroll tip text bottom indicators
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.KeyboardDoubleArrowUp, contentDescription = "Swipe up", tint = SlateGrey, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Swipe up for more products", color = SlateGrey, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ReelActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BlackPure.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = WhitePure,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.background(BlackPure.copy(alpha = 0.1f)).padding(horizontal = 4.dp)
        )
    }
}
