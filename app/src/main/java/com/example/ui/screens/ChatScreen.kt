package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    listingId: Int,
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    var product by remember { mutableStateOf<ListingEntity?>(null) }
    val messages by viewModel.activeChatMessages.collectAsState()
    val offers by viewModel.activeOffers.collectAsState()
    val app = viewModel.getApplication<android.app.Application>()

    var textInput by remember { mutableStateOf("") }
    var sharedImagePresetUrl by remember { mutableStateOf<String?>(null) }
    var tempReportConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        val database = AppDatabase.getDatabase(app)
        product = database.marketplaceDao().getListingById(listingId)
        viewModel.selectListingForChat(listingId)
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyberCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.sellerName.take(1), color = BlackPure, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.sellerName, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified Profile", tint = NeonMint, modifier = Modifier.size(12.dp))
                            }
                            Text("Active • 5m response speed", color = SlateGrey, fontSize = 10.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WhitePure)
                    }
                },
                actions = {
                    // Safety Flag report
                    IconButton(onClick = { tempReportConfirm = true }) {
                        Icon(Icons.Default.ReportGmailerrorred, contentDescription = "Report Seller", tint = AlertCoral)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            // Message input bottom footer
            Column(
                modifier = Modifier
                    .background(DarkSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // If sharing custom product snapshot preview
                if (sharedImagePresetUrl != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                    ) {
                        AsyncImage(model = sharedImagePresetUrl, contentDescription = "Share item", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        IconButton(
                            onClick = { sharedImagePresetUrl = null },
                            modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(BlackPure.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = WhitePure, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Image attachment quick button
                    IconButton(
                        onClick = { sharedImagePresetUrl = item.imageUrl }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Share snap", tint = CyberCyan)
                    }

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ask: 'Is price negotiable?' or try bidding terms...", color = SlateGrey, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhitePure,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .testTag("chat_input_text")
                    )

                    IconButton(
                        onClick = {
                            viewModel.handleSendMessage(item.id, textInput, sharedImagePresetUrl)
                            textInput = ""
                            sharedImagePresetUrl = null
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonMint)
                            .testTag("send_chat_message_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send Message", tint = BlackPure, modifier = Modifier.size(18.dp))
                    }
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
            // Anchor product mini card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = "Listing review",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(item.title, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("$${item.price} • ${item.condition}", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonMint.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("SECURE DEAL", color = NeonMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick bidding prompts (helps with bargaining flow checks)
            if (offers.isNotEmpty()) {
                val latestOffer = offers.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (latestOffer.status) {
                                "ACCEPTED" -> NeonMint.copy(alpha = 0.15f)
                                "REJECTED" -> AlertCoral.copy(alpha = 0.15f)
                                else -> CyberCyan.copy(alpha = 0.15f)
                            }
                        )
                        .padding(12.dp)
                        .testTag("active_offer_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (latestOffer.status) {
                                        "ACCEPTED" -> Icons.Default.CheckCircle
                                        "REJECTED" -> Icons.Default.Cancel
                                        else -> Icons.Default.HourglassBottom
                                    },
                                    contentDescription = "Offer Status",
                                    tint = when (latestOffer.status) {
                                        "ACCEPTED" -> NeonMint
                                        "REJECTED" -> AlertCoral
                                        else -> CyberCyan
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Proposed Bargaining: $${latestOffer.offerPrice}",
                                    color = WhitePure,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text("Original: $${item.price}. Code validation state: ${latestOffer.status}", color = SlateGrey, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }

                        if (latestOffer.status == "PENDING") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.handleAcceptOffer(latestOffer.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonMint, contentColor = BlackPure),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(26.dp).testTag("accept_offer_btn")
                                ) {
                                    Text("Accept", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.handleRejectOffer(latestOffer.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertCoral, contentColor = WhitePure),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(26.dp).testTag("reject_offer_btn")
                                ) {
                                    Text("Reject", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(latestOffer.status, color = if (latestOffer.status == "ACCEPTED") NeonMint else AlertCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Messages timeline scroll lists
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    // Chat security header
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Lock, contentDescription = "Shield", tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Anti-Scam Bot Security Active", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    "For your protection, negotiations on Nexkart are monitored. Avoid sharing off-app billing passwords, Google forms, or phone digits.",
                                    color = SlateGrey,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    ChatBubbleRow(msg, isMe = msg.sender == viewModel.chatUser)
                }
            }
        }
    }

    // Manual listing reporting dialog confirm
    if (tempReportConfirm) {
        AlertDialog(
            onDismissRequest = { tempReportConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleSendMessage(item.id, "📢 SYSTEM NOTICE: buyer '${viewModel.chatUser}' has submitted a safety flag report against this listing. Our admin mods will review the dialog logs shortly.", imageUrl = null)
                        tempReportConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCoral, contentColor = WhitePure)
                ) {
                    Text("Submit Safety Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tempReportConfirm = false }) {
                    Text("Dismiss", color = SlateGrey)
                }
            },
            title = { Text("Report Listing?", color = AlertCoral, fontWeight = FontWeight.Bold) },
            text = { Text("Are you certain you wish to flag this classified ad? Nexkart's Admin moderators will isolate the seller's active postings, review recent chats, and terminate accounts attempting off-app phishing scams.", color = SlateGrey) },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ChatBubbleRow(msg: com.example.data.model.ChatMessageEntity, isMe: Boolean) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = formatter.format(Date(msg.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isSystemBotMessage) Alignment.CenterHorizontally else if (isMe) Alignment.End else Alignment.Start
    ) {
        if (msg.isSystemBotMessage) {
            // Anti scam bot alerts style
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AlertCoral.copy(alpha = 0.15f))
                    .border(1.dp, AlertCoral.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = "Report", tint = AlertCoral, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg.message, color = WhitePure, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        } else {
            // Standard Chats bubbles
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.isAntiScamFlagged) AlertCoral.copy(alpha = 0.2f) else if (isMe) NeonMint else DarkSurface
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (msg.imageUrl != null) {
                        AsyncImage(
                            model = msg.imageUrl,
                            contentDescription = "Shared Attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = msg.message,
                        color = if (isMe && !msg.isAntiScamFlagged) BlackPure else WhitePure,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            color = if (isMe && !msg.isAntiScamFlagged) BlackPure.copy(alpha = 0.6f) else SlateGrey,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}
