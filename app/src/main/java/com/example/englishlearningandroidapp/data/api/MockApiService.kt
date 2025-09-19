package com.example.englishlearningandroidapp.data.api

import android.util.Log
import retrofit2.Response

/**
 * Mock implementation of dictionary API for testing without internet
 */
class MockApiService {
    
    companion object {
        private const val TAG = "MockApiService"
        private const val DEBUG_ENABLED = true
    }
    
    /**
     * Mock word definitions database
     */
    private val mockDefinitions = mapOf(
        "cook" to WordDefinitionResponse(
            word = "cook",
            phonetic = "kʊk",
            definitions = listOf(
                Definition(
                    translation = "做飯，烹調;燒，煮",
                    partOfSpeech = "verb",
                    example = "She cooks dinner every evening.",
                    synonyms = listOf("prepare", "make"),
                    antonyms = emptyList()
                ),
                Definition(
                    translation = "廚師",
                    partOfSpeech = "noun",
                    example = "She's a wonderful cook.",
                    synonyms = listOf("chef", "culinary artist"),
                    antonyms = emptyList()
                )
            )
        ),
        "book" to WordDefinitionResponse(
            word = "book",
            phonetic = "bʊk",
            definitions = listOf(
                Definition(
                    translation = "書",
                    partOfSpeech = "noun",
                    example = "I am reading a good book.",
                    synonyms = listOf("tome", "volume"),
                    antonyms = emptyList()
                ),
                Definition(
                    translation = "預訂",
                    partOfSpeech = "verb",
                    example = "I need to book a hotel room.",
                    synonyms = listOf("reserve", "schedule"),
                    antonyms = listOf("cancel")
                )
            )
        ),
        "hello" to WordDefinitionResponse(
            word = "hello",
            phonetic = "həˈləʊ",
            definitions = listOf(
                Definition(
                    translation = "你好",
                    partOfSpeech = "exclamation",
                    example = "Hello, how are you?",
                    synonyms = listOf("hi", "greetings"),
                    antonyms = listOf("goodbye")
                )
            )
        ),
        "learn" to WordDefinitionResponse(
            word = "learn",
            phonetic = "lɜːn",
            definitions = listOf(
                Definition(
                    translation = "學習",
                    partOfSpeech = "verb",
                    example = "I want to learn English.",
                    synonyms = listOf("study", "acquire knowledge"),
                    antonyms = listOf("forget", "unlearn")
                )
            )
        ),
        "english" to WordDefinitionResponse(
            word = "english",
            phonetic = "ˈɪŋɡlɪʃ",
            definitions = listOf(
                Definition(
                    translation = "英語",
                    partOfSpeech = "noun",
                    example = "I am learning English.",
                    synonyms = emptyList(),
                    antonyms = emptyList()
                )
            )
        ),
        "love" to WordDefinitionResponse(
            word = "love",
            phonetic = "lʌv",
            definitions = listOf(
                Definition(
                    translation = "愛",
                    partOfSpeech = "noun",
                    example = "Love is in the air.",
                    synonyms = listOf("affection", "adoration"),
                    antonyms = listOf("hate", "dislike")
                ),
                Definition(
                    translation = "愛，喜歡",
                    partOfSpeech = "verb",
                    example = "I love chocolate.",
                    synonyms = listOf("adore", "cherish"),
                    antonyms = listOf("hate", "despise")
                )
            )
        ),
        "house" to WordDefinitionResponse(
            word = "house",
            phonetic = "haʊs",
            definitions = listOf(
                Definition(
                    translation = "房子",
                    partOfSpeech = "noun",
                    example = "We live in a big house.",
                    synonyms = listOf("home", "dwelling"),
                    antonyms = emptyList()
                )
            )
        ),
        "water" to WordDefinitionResponse(
            word = "water",
            phonetic = "ˈwɔːtər",
            definitions = listOf(
                Definition(
                    translation = "水",
                    partOfSpeech = "noun",
                    example = "I need a glass of water.",
                    synonyms = listOf("H2O"),
                    antonyms = emptyList()
                )
            )
        ),
        "happy" to WordDefinitionResponse(
            word = "happy",
            phonetic = "ˈhæpi",
            definitions = listOf(
                Definition(
                    translation = "快樂的",
                    partOfSpeech = "adjective",
                    example = "She looks very happy today.",
                    synonyms = listOf("joyful", "cheerful", "glad"),
                    antonyms = listOf("sad", "unhappy", "miserable")
                )
            )
        ),
        "good" to WordDefinitionResponse(
            word = "good",
            phonetic = "ɡʊd",
            definitions = listOf(
                Definition(
                    translation = "好的",
                    partOfSpeech = "adjective",
                    example = "This is a good book.",
                    synonyms = listOf("excellent", "great", "fine"),
                    antonyms = listOf("bad", "poor", "terrible")
                )
            )
        )
    )
    
    /**
     * Mock word definition lookup
     */
    suspend fun getWordDefinition(word: String): Response<WordDefinitionResponse> {
        val normalizedWord = word.lowercase().trim()
        
        debugLog("=== MOCK API SERVICE CALLED ===")
        debugLog("Requested word: '$word'")
        debugLog("Normalized word: '$normalizedWord'")
        debugLog("Available words: ${mockDefinitions.keys.joinToString(", ")}")
        
        // Simulate network delay
        debugLog("Simulating network delay (500ms)...")
        kotlinx.coroutines.delay(500)
        
        return if (mockDefinitions.containsKey(normalizedWord)) {
            val definition = mockDefinitions[normalizedWord]!!
            debugLog("FOUND definition for '$normalizedWord':")
            debugLog("  Word: ${definition.word}")
            debugLog("  Phonetic: ${definition.phonetic}")
            debugLog("  Definitions count: ${definition.definitions.size}")
            definition.definitions.forEachIndexed { index, def ->
                debugLog("    Definition ${index + 1}:")
                debugLog("      Translation: ${def.translation}")
                debugLog("      Part of Speech: ${def.partOfSpeech}")
                debugLog("      Example: ${def.example ?: "N/A"}")
                debugLog("      Synonyms: ${def.synonyms?.joinToString(", ") ?: "None"}")
                debugLog("      Antonyms: ${def.antonyms?.joinToString(", ") ?: "None"}")
            }
            debugLog("=== MOCK API SUCCESS ===")
            Response.success(definition)
        } else {
            debugLog("NOT FOUND: '$normalizedWord' is not in mock database")
            debugLog("=== MOCK API NOT FOUND ===")
            // Return 404 for unknown words
            Response.error(404, okhttp3.ResponseBody.create(null, "Word not found"))
        }
    }
    
    /**
     * Get all available mock words (for testing)
     */
    fun getAvailableWords(): List<String> {
        return mockDefinitions.keys.toList()
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
 * Create mock response for testing
 */
fun createMockResponse(
    word: String,
    definitions: List<Definition>
): Response<WordDefinitionResponse> {
    val response = WordDefinitionResponse(
        word = word,
        phonetic = "mock",
        definitions = definitions
    )
    return Response.success(response)
}
