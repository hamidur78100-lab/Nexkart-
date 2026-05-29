package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.data.model.ListingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val app = viewModel.getApplication<android.app.Application>()
    val coroutineScope = rememberCoroutineScope()
    var reportedListings by remember { mutableStateOf<List<ListingEntity>>(emptyList()) }
    
    // Admin Telemetries mock state values
    var totalUsers by remember { mutableStateOf(349) }
    var premiumSubscriptionsRevenue by remember { mutableStateOf(1745.50) }
    var adsViewedCount by remember { mutableStateOf(4850) }

    fun refreshReported() {
        coroutineScope.launch {
            val db = AppDatabase.getDatabase(app)
            // Simulating reported items (let's flag the ones that are NOT verified just as mock, or provide mock lists)
            db.marketplaceDao().getAllListings().collect { list ->
                reportedListings = list.filter { !it.isVerifiedSeller || it.views > 2 }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshReported()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nexkart Admin Portal", color = WhitePure, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Platform Diagnostics", color = SlateGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonMint.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("LIVE SYNCING", color = NeonMint, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Diagnostic stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        icon = Icons.Default.People,
                        label = "Total Users",
                        value = totalUsers.toString(),
                        tint = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        icon = Icons.Default.Payments,
                        label = "Collections",
                        value = "$${premiumSubscriptionsRevenue.toInt()}",
                        tint = GoldCrown,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        icon = Icons.Default.Visibility,
                        label = "Ad Views",
                        value = adsViewedCount.toString(),
                        tint = NeonMint,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                HorizontalDivider(color = DarkSurfaceCard)
            }

            item {
                Text(
                    text = "Flagged Classifieds for Review (${reportedListings.size})",
                    color = AlertCoral,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (reportedListings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = "Clear", tint = NeonMint, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All clear! Zero reported listings pending logs review.", color = SlateGrey, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(reportedListings) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reported_item_card_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(item.title, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Seller: ${item.sellerName} • Category: ${item.category}", color = SlateGrey, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AlertCoral.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("SUSPICIOUS CHATS", color = AlertCoral, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Report Reason: Trigger keyword matched direct chats. Manual review requested by safety anti-scam bot.",
                                color = SlateGrey,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Moderator Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Deleting listings action
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val db = AppDatabase.getDatabase(app)
                                            db.marketplaceDao().deleteListing(item.id)
                                            totalUsers -= 1 // Simulate ban action
                                            premiumSubscriptionsRevenue -= 4.99 // Deduct fine fee mock
                                            refreshReported()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertCoral, contentColor = WhitePure),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("admin_delete_listing_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Ban Listing", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete Listing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Dismiss actions
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val db = AppDatabase.getDatabase(app)
                                            // Mock dismiss by inserting verified status
                                            val updated = item.copy(isVerifiedSeller = true)
                                            db.marketplaceDao().insertListing(updated)
                                            refreshReported()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard, contentColor = NeonMint),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("admin_dismiss_report_btn")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Approve Listing", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve Listing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = WhitePure, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = SlateGrey, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}
