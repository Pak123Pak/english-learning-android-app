package com.example.englishlearningandroidapp.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for Cambridge Dictionary API
 */
interface DictionaryApiService {
    
    /**
     * Get word definition from Cambridge Dictionary API
     * @param word The English word to look up
     * @return Response containing word definitions
     */
    @GET("api/dictionary/en-tw/{word}")
    suspend fun getWordDefinition(@Path("word") word: String): Response<WordDefinitionResponse>
    
    /**
     * Get word definition with additional phonetics
     * @param word The English word to look up
     * @param includePhonetics Whether to include phonetic information
     * @return Response containing word definitions with phonetics
     */
    @GET("api/dictionary/en-tw/{word}")
    suspend fun getWordWithPhonetics(
        @Path("word") word: String,
        @Query("phonetics") includePhonetics: Boolean = true
    ): Response<WordDefinitionResponse>
    
    /**
     * Search for similar words (if API supports it)
     * @param query The search query
     * @param limit Maximum number of results
     * @return Response containing list of similar words
     */
    @GET("api/v1/search")
    suspend fun searchSimilarWords(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): Response<List<String>>
}

/**
 * Alternative API service for Free Dictionary API (fallback)
 */
interface FreeDictionaryApiService {
    
    /**
     * Get word definition from Free Dictionary API
     * @param word The English word to look up
     * @return Response containing word definitions
     */
    @GET("api/v2/entries/en/{word}")
    suspend fun getWordDefinition(@Path("word") word: String): Response<List<FreeDictionaryResponse>>
}

/**
 * Free Dictionary API response model (for fallback)
 */
data class FreeDictionaryResponse(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<Phonetic>? = null,
    val meanings: List<Meaning>? = null,
    val origin: String? = null
)

/**
 * Meaning model for Free Dictionary API
 */
data class Meaning(
    val partOfSpeech: String,
    val definitions: List<FreeDefinition>
)

/**
 * Definition model for Free Dictionary API
 */
data class FreeDefinition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String>? = null,
    val antonyms: List<String>? = null
)
