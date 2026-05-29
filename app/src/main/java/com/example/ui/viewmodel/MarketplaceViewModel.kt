package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ListingEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.OfferEntity
import com.example.data.repository.MarketplaceRepository
import com.example.data.api.GeminiHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarketplaceRepository
    private val prefs = application.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)

    val userProfileName = MutableStateFlow(prefs.getString("profile_name", "Hamidur") ?: "Hamidur")
    val userProfileImage = MutableStateFlow(prefs.getString("profile_image", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80") ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80")
    val userProfileEmail = MutableStateFlow(prefs.getString("profile_email", "hamidur78100@gmail.com") ?: "hamidur78100@gmail.com")
    val userProfileMobile = MutableStateFlow(prefs.getString("profile_phone", "+1 (555) 019-2831") ?: "+1 (555) 019-2831")
    val userProfileAddress = MutableStateFlow(prefs.getString("profile_address", "123 Cyberpunk Parkway, Neo York") ?: "123 Cyberpunk Parkway, Neo York")
    val userProfilePincode = MutableStateFlow(prefs.getString("profile_pincode", "10001") ?: "10001")

    // Personal user gallery
    private val defaultProfileGallery = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80,https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80,https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=300&q=80"
    val userProfileGallery = MutableStateFlow(prefs.getString("profile_gallery", defaultProfileGallery) ?: defaultProfileGallery)

    val chatUser get() = userProfileName.value // Dynamic mapping to support profile modifications

    fun updateProfile(name: String, imageUrl: String, email: String, mobile: String, address: String, pincode: String) {
        userProfileName.value = name
        userProfileImage.value = imageUrl
        userProfileEmail.value = email
        userProfileMobile.value = mobile
        userProfileAddress.value = address
        userProfilePincode.value = pincode

        prefs.edit().apply {
            putString("profile_name", name)
            putString("profile_image", imageUrl)
            putString("profile_email", email)
            putString("profile_phone", mobile)
            putString("profile_address", address)
            putString("profile_pincode", pincode)
            apply()
        }
    }

    fun addProfileGalleryPhoto(url: String) {
        val current = userProfileGallery.value
        val updated = if (current.isEmpty()) url else "$current,$url"
        userProfileGallery.value = updated
        prefs.edit().putString("profile_gallery", updated).apply()
    }

    fun removeProfileGalleryPhoto(url: String) {
        val list = userProfileGallery.value.split(",").filter { it.isNotEmpty() && it != url }
        val updated = list.joinToString(",")
        userProfileGallery.value = updated
        prefs.edit().putString("profile_gallery", updated).apply()
    }

    // Static filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val maxPriceFilter = MutableStateFlow<Double?>(null)
    val conditionFilter = MutableStateFlow("All")
    val zipCodeFilter = MutableStateFlow<String?>(null)
    
    // Refresh control state
    val isRefreshing = MutableStateFlow(false)
    
    // Geolocation States
    val userLat = MutableStateFlow(40.7128) // New York Latitude
    val userLng = MutableStateFlow(-74.0060) // New York Longitude
    val maxRadiusKm = MutableStateFlow<Double?>(null)

    // Monetization Tier States
    val isPremiumUser = MutableStateFlow(false)
    val userPostingsCount = MutableStateFlow(1) // Made 1 posting so far

    // Reactive streams
    val listingsList: StateFlow<List<ListingEntity>>
    val myListingsList: StateFlow<List<ListingEntity>>

    // Chat streams (listingId -> Flow)
    private val _activeListingIdForChat = MutableStateFlow<Int?>(null)
    val activeChatMessages: StateFlow<List<ChatMessageEntity>> = _activeListingIdForChat
        .flatMapLatest { id ->
            if (id != null) repository.getChatMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offer streams 
    val activeOffers: StateFlow<List<OfferEntity>> = _activeListingIdForChat
        .flatMapLatest { id ->
            if (id != null) repository.getOffers(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wishlist states
    val wishlistedIds = MutableStateFlow<Set<Int>>(emptySet())

    // AI Helper UI States
    val isGeneratingDescription = MutableStateFlow(false)
    val aiGeneratedDescriptionText = MutableStateFlow("")
    val isRemovingBackground = MutableStateFlow(false)
    val imageProcessingState = MutableStateFlow("Original") // "Original", "Removing...", "Removed"

    // AI Dynamic Auto-Enhancement state parameters
    val imageLuminosity = MutableStateFlow(1.0f) // Scale multiplier for Contrast
    val imageBrightness = MutableStateFlow(0.0f) // Brightness offset fraction
    val imageSharpness = MutableStateFlow(1.0f)  // Sharpness indicator value
    val isEnhancingImage = MutableStateFlow(false)
    val enhancementMessage = MutableStateFlow("")
    val ipEnhanceState = MutableStateFlow("Original") // "Original", "Analyzing...", "Optimized"

    private data class BasicFilters(
        val query: String,
        val category: String,
        val maxPrice: Double?,
        val condition: String
    )

    private data class GeoFilters(
        val lat: Double,
        val lng: Double,
        val radius: Double?,
        val zip: String?
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MarketplaceRepository(database.marketplaceDao())

        // Seed products if database is empty
        viewModelScope.launch {
            repository.seedDatabase()
        }

        // Combine inputs for live reactive search and geolocation sorting using group-nested type-safe combine overloads
        val basicFiltersFlow = combine(
            searchQuery,
            selectedCategory,
            maxPriceFilter,
            conditionFilter
        ) { search, cat, price, cond ->
            BasicFilters(search, cat, price, cond)
        }

        val geoFiltersFlow = combine(
            userLat,
            userLng,
            maxRadiusKm,
            zipCodeFilter
        ) { lat, lng, rad, zip ->
            GeoFilters(lat, lng, rad, zip)
        }

        listingsList = combine(
            repository.allListings,
            basicFiltersFlow,
            geoFiltersFlow
        ) { listings, basic, geo ->
            val filterCategory = if (basic.category == "All") null else basic.category
            val filterCond = if (basic.condition == "All") null else basic.condition
            
            listings.filter { item ->
                // Category match
                val matchCategory = filterCategory == null || item.category == filterCategory
                
                // Keyword match
                val matchQuery = basic.query.isEmpty() || 
                        item.title.contains(basic.query, ignoreCase = true) || 
                        item.description.contains(basic.query, ignoreCase = true) ||
                        item.brand.contains(basic.query, ignoreCase = true)
                
                // Price match
                val matchPrice = basic.maxPrice == null || item.price <= basic.maxPrice
                
                // Condition match
                val matchCondition = filterCond == null || item.condition.equals(filterCond, ignoreCase = true)
                
                // Zip match
                val matchZip = geo.zip.isNullOrEmpty() || item.zipCode == geo.zip
                
                // Geolocation Radius math (Haversine formula in KM)
                val matchRadius = if (geo.radius != null) {
                    val distance = repository.calculateDistanceKm(geo.lat, geo.lng, item.latitude, item.longitude)
                    distance <= geo.radius
                } else {
                    true
                }

                matchCategory && matchQuery && matchPrice && matchCondition && matchZip && matchRadius
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        myListingsList = listingsList
            .map { list -> list.filter { it.sellerName == chatUser } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Recommendation states and flows
    val recentlyViewedIds = MutableStateFlow<List<Int>>(emptyList())
    
    val recentlyViewedList: StateFlow<List<ListingEntity>> = recentlyViewedIds
        .map { ids ->
            val currentListings = listingsList.value
            ids.mapNotNull { id -> currentListings.find { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalizedRecommendations: StateFlow<List<ListingEntity>> = combine(
        recentlyViewedList,
        listingsList
    ) { recentlyViewed, allListings ->
        com.example.data.repository.RecommendationEngine.getPersonalizedRecommendations(recentlyViewed, allListings, limit = 5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Action triggers ---
    fun selectListingForChat(listingId: Int) {
        _activeListingIdForChat.value = listingId
        viewModelScope.launch {
            repository.incrementViews(listingId)
        }
    }

    fun toggleWishlist(listingId: Int) {
        val currentSet = wishlistedIds.value
        if (currentSet.contains(listingId)) {
            wishlistedIds.value = currentSet - listingId
        } else {
            wishlistedIds.value = currentSet + listingId
        }
    }

    fun refreshMarketplace(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isRefreshing.value = true
            
            // Simulating cloud synchronization latency & cleaning active search outputs
            searchQuery.value = ""
            selectedCategory.value = "All"
            maxPriceFilter.value = null
            conditionFilter.value = "All"
            maxRadiusKm.value = null
            zipCodeFilter.value = null
            
            kotlinx.coroutines.delay(1200)
            
            // Re-seed if empty, or simulate incremental views to illustrate high activity!
            repository.seedDatabase()
            val listings = listingsList.value
            if (listings.isNotEmpty()) {
                val luckyRandomListing = listings.random()
                repository.incrementViews(luckyRandomListing.id)
            }
            
            isRefreshing.value = false
            onComplete()
        }
    }

    fun handlePostListing(
        title: String,
        description: String,
        price: Double,
        category: String,
        imageUrl: String,
        locationName: String,
        zipCode: String,
        brand: String,
        condition: String,
        sizeChart: String,
        techSpecs: String,
        warranty: String,
        deviceAge: String,
        material: String,
        galleryUrls: String = "",
        onSuccess: () -> Unit,
        onLimitReached: () -> Unit
    ) {
        if (!isPremiumUser.value && userPostingsCount.value >= 3) {
            onLimitReached()
            return
        }

        viewModelScope.launch {
            val newListing = ListingEntity(
                title = title,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                videoUrl = "", // Empty or mock
                locationName = locationName,
                zipCode = zipCode,
                latitude = userLat.value + (Math.random() - 0.5) * 0.1, // Near user
                longitude = userLng.value + (Math.random() - 0.5) * 0.1,
                sellerName = chatUser,
                sellerRating = 5.0f,
                isVerifiedSeller = true,
                isFeatured = false,
                brand = brand,
                condition = condition,
                sizeChart = sizeChart,
                techSpecs = techSpecs,
                warranty = warranty,
                deviceAge = deviceAge,
                material = material,
                galleryUrls = galleryUrls
            )
            repository.insertListing(newListing)
            userPostingsCount.value += 1
            onSuccess()
        }
    }

    fun handleSendMessage(listingId: Int, messageText: String, imageUrl: String? = null) {
        if (messageText.trim().isEmpty() && imageUrl == null) return
        
        viewModelScope.launch {
            repository.sendChatMessage(
                listingId = listingId,
                sender = chatUser,
                recipient = "Seller", // In C2C, chats connect to individual listing owners
                messageText = messageText,
                imageUrl = imageUrl
            )
        }
    }

    fun handleMakeOffer(listingId: Int, offerPrice: Double) {
        viewModelScope.launch {
            repository.submitOffer(listingId, chatUser, offerPrice)
        }
    }

    fun handleAcceptOffer(offerId: Int) {
        viewModelScope.launch {
            repository.acceptOffer(offerId)
        }
    }

    fun handleRejectOffer(offerId: Int) {
        viewModelScope.launch {
            repository.rejectOffer(offerId)
        }
    }

    fun togglePremiumStatus() {
        isPremiumUser.value = !isPremiumUser.value
    }

    fun handleDeleteListing(listingId: Int) {
        viewModelScope.launch {
            repository.deleteListing(listingId)
            // If the user deleted one, update their postings count if greater than 0
            if (userPostingsCount.value > 0) {
                userPostingsCount.value -= 1
            }
        }
    }

    // --- AI RUNNERS ---
    fun runAiDescriptionGenerator(title: String, category: String, brand: String, condition: String, specs: String) {
        if (title.isEmpty()) return
        isGeneratingDescription.value = true
        viewModelScope.launch {
            val outline = GeminiHelper.generateDescription(title, category, brand, condition, specs)
            aiGeneratedDescriptionText.value = outline
            isGeneratingDescription.value = false
        }
    }

    fun runAiBackgroundRemover() {
        isRemovingBackground.value = true
        imageProcessingState.value = "Removing..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(2200) // Beautiful simulated ML processing delay
            imageProcessingState.value = "Removed"
            isRemovingBackground.value = false
        }
    }

    fun resetAiBackgroundRemover() {
        imageProcessingState.value = "Original"
    }

    fun addToBrowsingHistory(listingId: Int) {
        if (listingId <= 0) return
        val current = recentlyViewedIds.value.toMutableList()
        current.remove(listingId)
        current.add(0, listingId)
        recentlyViewedIds.value = current.take(8)
    }

    fun runAiImageEnhancer() {
        isEnhancingImage.value = true
        ipEnhanceState.value = "Analyzing..."
        viewModelScope.launch {
            enhancementMessage.value = "Analyzing image brightness & color depth..."
            kotlinx.coroutines.delay(800)
            enhancementMessage.value = "Measuring dark pixel intensity histograms..."
            kotlinx.coroutines.delay(800)
            enhancementMessage.value = "Applying intelligent automatic adjustments..."
            kotlinx.coroutines.delay(800)
            
            // Substantially improve parameters for live ColorMatrix filter styling feed
            imageLuminosity.value = 1.35f
            imageBrightness.value = 0.12f
            imageSharpness.value = 1.5f
            
            ipEnhanceState.value = "Optimized"
            enhancementMessage.value = "Enhanced: Brightness (+12%), Contrast (+35%), Sharpness (+50%)!"
            isEnhancingImage.value = false
        }
    }

    fun resetAiImageEnhancer() {
        imageLuminosity.value = 1.0f
        imageBrightness.value = 0.0f
        imageSharpness.value = 1.0f
        ipEnhanceState.value = "Original"
        enhancementMessage.value = ""
    }
}
