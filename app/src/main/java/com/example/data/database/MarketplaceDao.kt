package com.example.data.database

import androidx.room.*
import com.example.data.model.ListingEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.OfferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {

    // --- Listings ---
    @Query("SELECT * FROM listings ORDER BY isFeatured DESC, timestamp DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE category = :category ORDER BY timestamp DESC")
    fun getListingsByCategory(category: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getListingById(id: Int): ListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity): Long

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: Int)

    @Query("UPDATE listings SET views = views + 1 WHERE id = :id")
    suspend fun incrementViews(id: Int)

    // --- In-App Chats ---
    @Query("SELECT * FROM chats WHERE listingId = :listingId ORDER BY timestamp ASC")
    fun getChatMessagesForListing(listingId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    // --- Offers / Bidding ---
    @Query("SELECT * FROM offers WHERE listingId = :listingId ORDER BY timestamp DESC")
    fun getOffersForListing(listingId: Int): Flow<List<OfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferEntity)

    @Query("UPDATE offers SET status = :status WHERE id = :offerId")
    suspend fun updateOfferStatus(offerId: Int, status: String)
}
