package com.example.data.repository

import com.example.data.database.MarketplaceDao
import com.example.data.model.ListingEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.OfferEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.*

class MarketplaceRepository(private val dao: MarketplaceDao) {

    // Fetch reactive flows
    val allListings: Flow<List<ListingEntity>> = dao.getAllListings()

    suspend fun getListingById(id: Int): ListingEntity? = dao.getListingById(id)

    suspend fun insertListing(listing: ListingEntity): Long = dao.insertListing(listing)

    suspend fun deleteListing(id: Int) = dao.deleteListing(id)

    suspend fun incrementViews(id: Int) = dao.incrementViews(id)

    // --- Dynamic Search, Filter, and Distance Calculations ---
    fun searchAndFilterListings(
        query: String,
        category: String?,
        maxPrice: Double?,
        condition: String?,
        userLat: Double?,
        userLng: Double?,
        maxRadiusKm: Double?,
        zipCodeFilter: String?
    ): Flow<List<ListingEntity>> {
        return allListings.map { list ->
            list.filter { item ->
                // Category match
                val matchCategory = category == null || category == "All" || item.category == category
                
                // Keyword match
                val matchQuery = query.isEmpty() || 
                        item.title.contains(query, ignoreCase = true) || 
                        item.description.contains(query, ignoreCase = true) ||
                        item.brand.contains(query, ignoreCase = true)
                
                // Price match
                val matchPrice = maxPrice == null || item.price <= maxPrice
                
                // Condition match
                val matchCondition = condition == null || condition == "All" || item.condition.equals(condition, ignoreCase = true)
                
                // Zip match
                val matchZip = zipCodeFilter.isNullOrEmpty() || item.zipCode == zipCodeFilter
                
                // Geolocation Radius math (Haversine formula in KM)
                val matchRadius = if (maxRadiusKm != null && userLat != null && userLng != null) {
                    val distance = calculateDistanceKm(userLat, userLng, item.latitude, item.longitude)
                    distance <= maxRadiusKm
                } else {
                    true
                }

                matchCategory && matchQuery && matchPrice && matchCondition && matchZip && matchRadius
            }
        }
    }

    // Haversine Formula for distance filtering
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // --- SECURE CHAT & ANTI-SCAM BOT LOGIC ---
    fun getChatMessages(listingId: Int): Flow<List<ChatMessageEntity>> =
        dao.getChatMessagesForListing(listingId)

    suspend fun sendChatMessage(listingId: Int, sender: String, recipient: String, messageText: String, imageUrl: String? = null) {
        val scamKeywords = listOf(
            "western union", "wire transfer", "gift card", "pay outside", "bank account", 
            "crypto deposit", "security code", "cvv", "cash in advance", "click description link", 
            "whatsapp me at", "telegram me", "escrow service", "no refund", "google pay link"
        )
        
        var isFlagged = false
        var flagReason: String? = null
        
        val lowercaseMsg = messageText.lowercase()
        for (keyword in scamKeywords) {
            if (lowercaseMsg.contains(keyword)) {
                isFlagged = true
                flagReason = "Suspicious keyword detected: '$keyword'. Always conduct deals securely within the app."
                break
            }
        }

        val messageEntity = ChatMessageEntity(
            listingId = listingId,
            sender = sender,
            recipient = recipient,
            message = messageText,
            imageUrl = imageUrl,
            isAntiScamFlagged = isFlagged,
            scamReason = flagReason,
            timestamp = System.currentTimeMillis()
        )
        
        dao.insertChatMessage(messageEntity)

        // If automated scam filter picks it up, trigger bot reply!
        if (isFlagged) {
            val botReply = ChatMessageEntity(
                listingId = listingId,
                sender = "NEXKART ANTI-SCAM BOT",
                recipient = sender,
                message = "🚨 WARNING TO SENDER & RECIPIENT: Nexkart Bot detected suspicious or high-risk language related to off-app payments, wire transfers, or contact shares. For your safety, never share personal passwords, phone numbers, or move payments outside Nexkart. Report user if suspicious.",
                isSystemBotMessage = true,
                timestamp = System.currentTimeMillis() + 500
            )
            dao.insertChatMessage(botReply)
        }
    }

    // --- OFFERS ---
    fun getOffers(listingId: Int): Flow<List<OfferEntity>> = dao.getOffersForListing(listingId)

    suspend fun submitOffer(listingId: Int, buyerName: String, price: Double) {
        val offerEntity = OfferEntity(
            listingId = listingId,
            buyerName = buyerName,
            offerPrice = price,
            status = "PENDING"
        )
        dao.insertOffer(offerEntity)
    }

    suspend fun acceptOffer(offerId: Int) {
        dao.updateOfferStatus(offerId, "ACCEPTED")
    }

    suspend fun rejectOffer(offerId: Int) {
        dao.updateOfferStatus(offerId, "REJECTED")
    }

    // --- DB PREPOPULATION ---
    suspend fun seedDatabase() {
        val count = allListings.first().size
        if (count == 0) {
            val seedListings = listOf(
                // 1. Fashion & Clothing
                ListingEntity(
                    title = "Vintage Ralph Lauren Leather Aviator Jacket",
                    description = "Extremely rare collector's leather aviator jacket. Features genuine shearling collar, embossed hardware, and smooth inner lining. Kept in a climate-controlled wardrobe, flawless patination.",
                    price = 450.0,
                    category = "Fashion & Clothing",
                    imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-man-showing-off-his-stylish-jacket-42289-large.mp4", // Reel fallback
                    locationName = "SoHo, Manhattan",
                    zipCode = "10012",
                    latitude = 40.7233,
                    longitude = -74.0030,
                    sellerName = "Julius V.",
                    sellerRating = 4.8f,
                    isVerifiedSeller = true,
                    isFeatured = true,
                    brand = "Ralph Lauren Purple Label",
                    condition = "Like New",
                    sizeChart = "US L / Fit 42",
                    material = "100% Calfskin & Shearling",
                    galleryUrls = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500,https://images.unsplash.com/photo-1521223890158-f9f7c3d5ded1?w=500,https://images.unsplash.com/photo-1620138546344-7b2c0b050edb?w=500"
                ),
                ListingEntity(
                    title = "Modern Tailored Silk Waistcoat Set",
                    description = "Fluid silhouette linen-silk waistcoat set featuring elegant double-breasted closures and tailored wide-leg trousers. Unworn, high-quality drape for aesthetic/minimalist aesthetics.",
                    price = 115.0,
                    category = "Fashion & Clothing",
                    imageUrl = "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500",
                    videoUrl = "",
                    locationName = "Downtown Brooklyn",
                    zipCode = "11201",
                    latitude = 40.6925,
                    longitude = -73.9903,
                    sellerName = "Elena Rose",
                    sellerRating = 4.9f,
                    isVerifiedSeller = true,
                    brand = "Studio Arket",
                    condition = "New",
                    sizeChart = "EU 38 / US Medium",
                    material = "Silk, Organic Flax Linen"
                ),
                
                // 2. Electronics & Gadgets
                ListingEntity(
                    title = "Google Pixel 8 Pro (Obsidian Black)",
                    description = "Excellent high-efficiency compact powerhouse with Google AI features intact. Unlocked for all carriers. Camera glass is pristine. Screen has had a glass protector since unboxing.",
                    price = 620.0,
                    category = "Electronics & Gadgets",
                    imageUrl = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-holding-a-smartphone-with-a-green-screen-34185-large.mp4",
                    locationName = "Williamsburg, Brooklyn",
                    zipCode = "11211",
                    latitude = 40.7081,
                    longitude = -73.9571,
                    sellerName = "Alex Tech Loft",
                    sellerRating = 4.7f,
                    isVerifiedSeller = true,
                    isFeatured = true,
                    brand = "Google",
                    condition = "Like New",
                    techSpecs = "Tensor G3, 12GB LPDDR5X RAM, 256GB UFS 4.0 Storage, 6.7\" 120Hz LTPO",
                    warranty = "6 Months Google Warranty remaining",
                    deviceAge = "4 Months",
                    galleryUrls = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500,https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500,https://images.unsplash.com/photo-1580910051074-3eb694886505?w=500"
                ),
                ListingEntity(
                    title = "Sony WH-1000XM5 Noise Cancelling Headphones",
                    description = "Industry-leading ANC headphones in flawless working order. Exceptional soundstage, custom dynamic EQ, and 30-hour battery. Original carrying case and type-C braided cable included.",
                    price = 240.0,
                    category = "Electronics & Gadgets",
                    imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
                    videoUrl = "",
                    locationName = "West Village, NYC",
                    zipCode = "10014",
                    latitude = 40.7358,
                    longitude = -74.0048,
                    sellerName = "Marc Chen",
                    sellerRating = 4.5f,
                    isVerifiedSeller = false,
                    brand = "Sony",
                    condition = "Good",
                    techSpecs = "30mm drivers, Bluetooth 5.2, LDAC/AAC codecs, Multipoint connection, Voice assistant",
                    warranty = "None",
                    deviceAge = "1 Year"
                ),

                // 3. Shoes & Footwear
                ListingEntity(
                    title = "Nike Air Jordan 1 Retro 'Chicago' OG",
                    description = "Deadstock retro basketball high-top in Chicago colors. Red, white, and black leather panels featuring the historic wing logo on the lateral collar. Complete with original box and extra wax laces.",
                    price = 380.0,
                    category = "Shoes & Footwear",
                    imageUrl = "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-very-close-up-of-running-shoes-4853-large.mp4",
                    locationName = "Harlem, NYC",
                    zipCode = "10027",
                    latitude = 40.8078,
                    longitude = -73.9454,
                    sellerName = "KicksCollectorNYC",
                    sellerRating = 5.0f,
                    isVerifiedSeller = true,
                    isFeatured = true,
                    brand = "Nike Jordan",
                    condition = "New",
                    sizeChart = "US Men 10 / UK 9 / EU 44",
                    material = "Premium Full-Grain Leather",
                    galleryUrls = "https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500,https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=500,https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=500"
                ),
                ListingEntity(
                    title = "Adidas UltraBoost Light Carbon Core",
                    description = "Super lightweight running daily sneakers with energetic Boost technology. Unmatched cushioned response for long days walking. Worn only twice, mesh is stain-free.",
                    price = 110.0,
                    category = "Shoes & Footwear",
                    imageUrl = "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=500",
                    videoUrl = "",
                    locationName = "Queens, NY",
                    zipCode = "11101",
                    latitude = 40.7498,
                    longitude = -73.9399,
                    sellerName = "RunSarah",
                    sellerRating = 4.6f,
                    isVerifiedSeller = false,
                    brand = "Adidas",
                    condition = "Good",
                    sizeChart = "US Men 8.5 / UK 7.5",
                    material = "Primeknit & Recycled Light Boost"
                ),

                // 4. Beautiful Products
                ListingEntity(
                    title = "Minimalist Ceramic Ribbed Flower Vase Set",
                    description = "Pair of abstract clay-matte ceramic decorative vases. High-contrast sculptural look that sits beautifully on wood shelves, credenzas, or workspace setups. Ideal for dried bunny-tails or pampas glass.",
                    price = 55.0,
                    category = "Beautiful Products",
                    imageUrl = "https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=500",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-decorating-a-wooden-shelf-with-plants-and-vases-44111-large.mp4",
                    locationName = "Astoria, NYC",
                    zipCode = "11102",
                    latitude = 40.7712,
                    longitude = -73.9242,
                    sellerName = "Nordic Nestings",
                    sellerRating = 4.9f,
                    isVerifiedSeller = true,
                    isFeatured = true,
                    brand = "Maison Clay",
                    condition = "New",
                    material = "Porcelain Matte Glaze & Earthen Clay"
                ),
                ListingEntity(
                    title = "Aesthetic Hand-Poured Lavender & Sandalwood Candle",
                    description = "Slow-burning soy wax aromatherapy candle nestled in a hand-crafted travertine jar. Emits subtle, premium floral tones balanced with dry sandalwood. Over 50 hours burn time.",
                    price = 28.0,
                    category = "Beautiful Products",
                    imageUrl = "https://images.unsplash.com/photo-1603006905003-be475563bc59?w=500",
                    videoUrl = "",
                    locationName = "Upper East Side, Manhattan",
                    zipCode = "10028",
                    latitude = 40.7769,
                    longitude = -73.9526,
                    sellerName = "Maison de Scent",
                    sellerRating = 4.8f,
                    isVerifiedSeller = true,
                    brand = "AromaTravertine",
                    condition = "New",
                    material = "100% Vegan Soy Wax & Sandalwood Oil"
                )
            )
            for (item in seedListings) {
                dao.insertListing(item)
            }
        }
    }
}
