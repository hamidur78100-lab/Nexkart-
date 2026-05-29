package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val category: String, // "Fashion & Clothing", "Electronics & Gadgets", "Shoes & Footwear", "Beautiful Products"
    val imageUrl: String,
    val videoUrl: String, // Used to simulate Reels
    val locationName: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val sellerName: String,
    val sellerRating: Float,
    val isVerifiedSeller: Boolean,
    val isFeatured: Boolean = false,
    val views: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Custom Fields per Category
    val brand: String = "",
    val condition: String = "", // "New", "Like New", "Good", "Fair"
    val sizeChart: String = "", // UK/US Size, Fits like M etc.
    val techSpecs: String = "", // Mobile RAM, Processor, Storage
    val warranty: String = "", // e.g. "6 Months Left"
    val deviceAge: String = "", // e.g. "1 Year"
    val material: String = "", // For Lifestyle & Beautiful Products
    val galleryUrls: String = "" // List of comma-separated URLs for multi-photo gallery
)

@Entity(tableName = "chats")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val sender: String,
    val recipient: String,
    val message: String,
    val imageUrl: String? = null,
    val isSystemBotMessage: Boolean = false,
    val isAntiScamFlagged: Boolean = false,
    val scamReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listingId: Int,
    val buyerName: String,
    val offerPrice: Double,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)
