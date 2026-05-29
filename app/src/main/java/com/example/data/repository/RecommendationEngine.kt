package com.example.data.repository

import com.example.data.model.ListingEntity
import kotlin.math.abs
import kotlin.math.max

object RecommendationEngine {

    /**
     * Calculates a similarity score between two listings on a scale of 0.0 to 14.0+
     * Uses hybrid content-based matching metrics:
     * - Category overlap (high weight: 5.0)
     * - Brand identity similarity (weight: 3.5)
     * - Price proximity (weight: 2.5)
     * - Condition match (weight: 1.0)
     * - Title Jaccard text similarity (weight: 3.0)
     */
    fun calculateSimilarity(itemA: ListingEntity, itemB: ListingEntity): Double {
        if (itemA.id == itemB.id) return 0.0

        var score = 0.0

        // 1. Category Match (Very Strong Indicator)
        if (itemA.category.equals(itemB.category, ignoreCase = true)) {
            score += 5.0
        }

        // 2. Brand Match
        if (itemA.brand.isNotBlank() && itemB.brand.isNotBlank() &&
            itemA.brand.trim().equals(itemB.brand.trim(), ignoreCase = true)) {
            score += 3.5
        }

        // 3. Price Proximity (closer price is more likely an alternative)
        val maxPrice = max(1.0, max(itemA.price, itemB.price))
        val priceDiff = abs(itemA.price - itemB.price)
        val proximity = 1.0 - (priceDiff / maxPrice) // Bound from 0.0 to 1.0
        score += proximity * 2.5

        // 4. Condition Similarity
        if (itemA.condition.isNotBlank() && itemB.condition.isNotBlank() &&
            itemA.condition.trim().equals(itemB.condition.trim(), ignoreCase = true)) {
            score += 1.0
        }

        // 5. Title Words Overlap (Jaccard Index)
        val stopwords = setOf("and", "or", "the", "a", "an", "with", "for", "of", "in", "on", "at", "to", "by")
        val tokensA = itemA.title.lowercase()
            .split("[\\s,.-/]+".toRegex())
            .filter { it.length > 1 && !stopwords.contains(it) }
            .toSet()
            
        val tokensB = itemB.title.lowercase()
            .split("[\\s,.-/]+".toRegex())
            .filter { it.length > 1 && !stopwords.contains(it) }
            .toSet()

        if (tokensA.isNotEmpty() && tokensB.isNotEmpty()) {
            val intersection = tokensA.intersect(tokensB).size
            val union = tokensA.union(tokensB).size
            val jaccard = intersection.toDouble() / union
            score += jaccard * 3.0
        }

        return score
    }

    /**
     * Recommends items similar to the currently viewed item.
     */
    fun getSimilarListings(
        current: ListingEntity,
        allListings: List<ListingEntity>,
        limit: Int = 4
    ): List<ListingEntity> {
        return allListings
            .filter { it.id != current.id }
            .map { it to calculateSimilarity(current, it) }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }

    /**
     * Personalized engine: Scans multiple recently viewed items, weights their categories & brands preference,
     * and suggests fresh, unviewed items fitting the user's signature.
     */
    fun getPersonalizedRecommendations(
        recentlyViewed: List<ListingEntity>,
        allListings: List<ListingEntity>,
        limit: Int = 4
    ): List<ListingEntity> {
        if (recentlyViewed.isEmpty()) {
            // Unpersonalized homepage fallback: top viewed / featured 
            return allListings.sortedByDescending { it.isFeatured }.sortedByDescending { it.views }.take(limit)
        }

        val viewedIds = recentlyViewed.map { it.id }.toSet()
        val candidates = allListings.filter { !viewedIds.contains(it.id) }

        return candidates
            .map { candidate ->
                // Average the similarity score against all recently viewed items
                val totalSim = recentlyViewed.sumOf { calculateSimilarity(it, candidate) }
                val score = totalSim / recentlyViewed.size
                candidate to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }
}
