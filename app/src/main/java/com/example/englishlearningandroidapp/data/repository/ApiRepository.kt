package com.example.englishlearningandroidapp.data.repository

import android.util.Log
import android.util.LruCache
import com.example.englishlearningandroidapp.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repository for managing API operations
 * Handles network requests and caching for dictionary API
 */
class ApiRepository(
    private val cambridgeApiService: DictionaryApiService,
    private val freeDictionaryApiService: FreeDictionaryApiService
) {
    
    companion object {
        private const val TAG = "ApiRepository"
        private const val DEBUG_ENABLED = true // Set to false in release builds
    }
    
    // Cache for API responses to reduce network calls
    private val cache: LruCache<String, WordDefinitionResponse> = LruCache(100)
    
    // Track cache hit/miss for debugging
    private var cacheHits = 0
    private var cacheMisses = 0
    
    /**
     * Fetch word definition from Cambridge Dictionary API with fallback
     * @param word The English word to look up
     * @param useCache Whether to use cached results
     * @return NetworkResult containing the word definition or error
     */
    suspend fun fetchWordDefinition(
        word: String, 
        useCache: Boolean = true
    ): NetworkResult<WordDefinitionResponse> = withContext(Dispatchers.IO) {
        
        debugLog("=== API REPOSITORY FETCH STARTED ===")
        debugLog("Word to fetch: '$word'")
        debugLog("Use cache: $useCache")
        
        val cacheKey = word.lowercase().trim()
        
        // Check cache first if enabled
        if (useCache) {
            cache.get(cacheKey)?.let { cachedResponse ->
                cacheHits++
                debugLog("Cache HIT for word: '$word'")
                debugLog("Cache statistics - Hits: $cacheHits, Misses: $cacheMisses")
                return@withContext NetworkResult.Success(cachedResponse)
            }
        }
        
        cacheMisses++
        debugLog("Cache MISS for word: '$word' (will fetch from API)")
        debugLog("Cache statistics - Hits: $cacheHits, Misses: $cacheMisses")
        
        try {
            // First try Cambridge Dictionary API
            debugLog("Trying Cambridge Dictionary API for word: '$word'")
            val cambridgeResult = fetchFromCambridgeApi(word)
            if (cambridgeResult.isSuccess()) {
                debugLog("Cambridge API SUCCESS for word: '$word'")
                cambridgeResult.getDataOrNull()?.let { response ->
                    cache.put(cacheKey, response)
                    debugLog("Response cached successfully")
                    debugLog("=== API REPOSITORY FETCH COMPLETED ===")
                    return@withContext cambridgeResult
                }
            } else {
                debugLog("Cambridge API FAILED for word: '$word'")
            }
            
            // Fallback to Free Dictionary API
            debugLog("Trying Free Dictionary API (fallback) for word: '$word'")
            val fallbackResult = fetchFromFreeDictionaryApi(word)
            if (fallbackResult.isSuccess()) {
                debugLog("Free Dictionary API SUCCESS for word: '$word'")
                fallbackResult.getDataOrNull()?.let { response ->
                    cache.put(cacheKey, response)
                    debugLog("Fallback response cached successfully")
                    debugLog("=== API REPOSITORY FETCH COMPLETED ===")
                    return@withContext fallbackResult
                }
            } else {
                debugLog("Free Dictionary API FAILED for word: '$word'")
            }
            
            // Both APIs failed
            debugLog("BOTH APIs failed for word: '$word'")
            debugLog("=== API REPOSITORY FETCH COMPLETED (FAILED) ===")
            NetworkResult.Error(
                ApiException.ServerException(404, "Word not found in any dictionary service"),
                "Word '$word' not found"
            )
            
        } catch (e: Exception) {
            debugLog("Exception during API fetch: ${e.message}")
            debugLog("Exception stack trace: ${e.stackTraceToString()}")
            debugLog("=== API REPOSITORY FETCH COMPLETED (EXCEPTION) ===")
            NetworkResult.Error(
                NetworkUtils.createNetworkException(e),
                "Failed to fetch word definition: ${e.message}"
            )
        }
    }
    
    /**
     * Fetch from Cambridge Dictionary API
     */
    private suspend fun fetchFromCambridgeApi(word: String): NetworkResult<WordDefinitionResponse> {
        return try {
            val response = cambridgeApiService.getWordDefinition(word)
            if (NetworkUtils.isSuccessful(response)) {
                val adaptedResponse = adaptCambridgeApiResponse(response, word)
                adaptedResponse
            } else {
                val exception = NetworkUtils.createApiException(response)
                NetworkResult.Error(exception, "Cambridge Dictionary API: ${exception.message}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                NetworkUtils.createNetworkException(e),
                "Cambridge API error: ${e.message}"
            )
        }
    }
    
    /**
     * Fetch from Free Dictionary API (fallback)
     */
    private suspend fun fetchFromFreeDictionaryApi(word: String): NetworkResult<WordDefinitionResponse> {
        return try {
            val response = freeDictionaryApiService.getWordDefinition(word)
            val adaptedResponse = adaptFreeDictionaryResponse(response, word)
            adaptedResponse
        } catch (e: Exception) {
            NetworkResult.Error(
                NetworkUtils.createNetworkException(e),
                "Free Dictionary API error: ${e.message}"
            )
        }
    }
    
    /**
     * Handle API response and convert to NetworkResult
     */
    private fun <T> handleApiResponse(
        response: Response<T>, 
        apiName: String
    ): NetworkResult<T> {
        return if (NetworkUtils.isSuccessful(response)) {
            NetworkResult.Success(response.body()!!)
        } else {
            val exception = NetworkUtils.createApiException(response)
            NetworkResult.Error(exception, "$apiName: ${exception.message}")
        }
    }
    
    /**
     * Adapt Free Dictionary API response to our format
     */
    private fun adaptFreeDictionaryResponse(
        response: Response<List<FreeDictionaryResponse>>,
        word: String
    ): NetworkResult<WordDefinitionResponse> {
        
        if (!NetworkUtils.isSuccessful(response)) {
            val exception = NetworkUtils.createApiException(response)
            return NetworkResult.Error(exception, "Free Dictionary API: ${exception.message}")
        }
        
        val freeDictionaryData = response.body()?.firstOrNull()
        if (freeDictionaryData == null) {
            return NetworkResult.Error(
                ApiException.ParseException("No data received from Free Dictionary API"),
                "Word not found"
            )
        }
        
        try {
            // Convert Free Dictionary response to our format
            val definitions = mutableListOf<Definition>()
            
            freeDictionaryData.meanings?.forEach { meaning ->
                meaning.definitions.forEach { freeDefinition ->
                    // Note: Free Dictionary API doesn't provide Chinese translations
                    // This is a limitation of the fallback API
                    val definition = Definition(
                        translation = freeDefinition.definition, // Using English definition as translation
                        partOfSpeech = meaning.partOfSpeech,
                        example = freeDefinition.example,
                        synonyms = freeDefinition.synonyms,
                        antonyms = freeDefinition.antonyms
                    )
                    definitions.add(definition)
                }
            }
            
            val adaptedResponse = WordDefinitionResponse(
                word = freeDictionaryData.word ?: word, // Use the word from API response, fallback to search term
                phonetic = freeDictionaryData.phonetic,
                phonetics = freeDictionaryData.phonetics,
                definitions = definitions,
                origin = freeDictionaryData.origin
            )
            
            return NetworkResult.Success(adaptedResponse)
            
        } catch (e: Exception) {
            return NetworkResult.Error(
                ApiException.ParseException("Failed to parse Free Dictionary response", e),
                "Failed to parse response"
            )
        }
    }
    
    /**
     * Adapt Cambridge Dictionary API response to our unified format
     */
    private fun adaptCambridgeApiResponse(
        response: Response<WordDefinitionResponse>,
        word: String
    ): NetworkResult<WordDefinitionResponse> {
        
        if (!NetworkUtils.isSuccessful(response)) {
            val exception = NetworkUtils.createApiException(response)
            return NetworkResult.Error(exception, "Cambridge Dictionary API: ${exception.message}")
        }
        
        val cambridgeData = response.body()
        if (cambridgeData == null) {
            return NetworkResult.Error(
                ApiException.ParseException("No data received from Cambridge Dictionary API"),
                "Word not found"
            )
        }
        
        try {
            // Convert Cambridge Dictionary response to our unified format
            val definitions = mutableListOf<Definition>()
            
            // Use the Cambridge definition field which contains the actual data
            cambridgeData.definition?.forEach { cambridgeDef ->
                val definition = Definition(
                    translation = cambridgeDef.translation,
                    partOfSpeech = cambridgeDef.pos,
                    example = cambridgeDef.example?.firstOrNull()?.text,
                    synonyms = null, // Cambridge API doesn't provide synonyms in this format
                    antonyms = null  // Cambridge API doesn't provide antonyms in this format
                )
                definitions.add(definition)
            }
            
            // Create phonetics from Cambridge pronunciation data
            val phonetics = cambridgeData.pronunciation?.mapNotNull { pron ->
                if (pron.hasAudio()) {
                    Phonetic(
                        text = pron.pron,
                        audio = pron.url
                    )
                } else {
                    Phonetic(
                        text = pron.pron,
                        audio = null
                    )
                }
            } ?: emptyList()
            
            val adaptedResponse = WordDefinitionResponse(
                word = cambridgeData.word, // Use the actual word from API response (base form)
                pos = cambridgeData.pos,
                phonetic = cambridgeData.pronunciation?.firstOrNull()?.pron,
                phonetics = phonetics,
                pronunciation = cambridgeData.pronunciation,
                verbs = cambridgeData.verbs,
                definitions = definitions,
                definition = cambridgeData.definition,
                origin = cambridgeData.origin
            )
            
            return NetworkResult.Success(adaptedResponse)
            
        } catch (e: Exception) {
            return NetworkResult.Error(
                ApiException.ParseException("Failed to parse Cambridge Dictionary response", e),
                "Failed to parse response"
            )
        }
    }
    
    /**
     * Search for similar words
     * @param query The search query
     * @param limit Maximum number of results
     * @return NetworkResult containing list of similar words
     */
    suspend fun searchSimilarWords(
        query: String, 
        limit: Int = 10
    ): NetworkResult<List<String>> = withContext(Dispatchers.IO) {
        
        try {
            val response = cambridgeApiService.searchSimilarWords(query, limit)
            handleApiResponse(response, "Cambridge Search API")
        } catch (e: Exception) {
            NetworkResult.Error(
                NetworkUtils.createNetworkException(e),
                "Failed to search similar words: ${e.message}"
            )
        }
    }
    
    /**
     * Get cached word definition if available
     * @param word The word to look up in cache
     * @return Cached response or null
     */
    fun getCachedDefinition(word: String): WordDefinitionResponse? {
        val cacheKey = word.lowercase().trim()
        return cache.get(cacheKey)
    }
    
    /**
     * Clear the API response cache
     */
    fun clearCache() {
        cache.evictAll()
        cacheHits = 0
        cacheMisses = 0
    }
    
    /**
     * Get cache statistics for debugging
     * @return Pair of (hits, misses)
     */
    fun getCacheStatistics(): Pair<Int, Int> {
        return Pair(cacheHits, cacheMisses)
    }
    
    /**
     * Get cache hit ratio
     * @return Cache hit ratio as percentage
     */
    fun getCacheHitRatio(): Float {
        val totalRequests = cacheHits + cacheMisses
        return if (totalRequests > 0) {
            (cacheHits.toFloat() / totalRequests) * 100
        } else {
            0f
        }
    }
    
    /**
     * Preload word definitions for better performance
     * @param words List of words to preload
     */
    suspend fun preloadDefinitions(words: List<String>) = withContext(Dispatchers.IO) {
        words.forEach { word ->
            try {
                fetchWordDefinition(word, useCache = false)
            } catch (e: Exception) {
                // Ignore errors during preloading
            }
        }
    }
    
    /**
     * Check if word exists in cache
     * @param word The word to check
     * @return True if word is cached
     */
    fun isWordCached(word: String): Boolean {
        val cacheKey = word.lowercase().trim()
        return cache.get(cacheKey) != null
    }
    
    /**
     * Get cache size information
     * @return Current cache size and max size
     */
    fun getCacheInfo(): Pair<Int, Int> {
        return Pair(cache.size(), cache.maxSize())
    }
    
    /**
     * Debug logging utility (only logs in debug builds)
     */
    private fun debugLog(message: String) {
        if (DEBUG_ENABLED) {
            Log.d(TAG, message)
            // Also print to system out for easier viewing in Android Studio
            println("[$TAG] $message")
        }
    }
}

/**
 * Extension functions for API repository operations
 */
object ApiRepositoryExtensions {
    
    /**
     * Check if API response contains valid translations
     */
    fun WordDefinitionResponse.hasValidTranslations(): Boolean {
        return definitions.any { it.translation.isNotBlank() }
    }
    
    /**
     * Get the best definition (first one with example or first one available)
     */
    fun WordDefinitionResponse.getBestDefinition(): Definition? {
        return definitions.firstOrNull { it.hasExample() } 
            ?: definitions.firstOrNull()
    }
    
    /**
     * Filter definitions by part of speech
     */
    fun WordDefinitionResponse.getDefinitionsByPartOfSpeech(partOfSpeech: String): List<Definition> {
        return definitions.filter { 
            it.partOfSpeech.equals(partOfSpeech, ignoreCase = true) 
        }
    }
    
    /**
     * Get all unique parts of speech from definitions
     */
    fun WordDefinitionResponse.getPartsOfSpeech(): List<String> {
        return definitions.map { it.partOfSpeech }.distinct()
    }
}
