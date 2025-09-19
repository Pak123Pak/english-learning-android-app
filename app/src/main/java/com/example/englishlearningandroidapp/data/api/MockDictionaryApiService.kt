package com.example.englishlearningandroidapp.data.api

import retrofit2.Response

/**
 * Mock implementation of DictionaryApiService for testing
 */
class MockDictionaryApiService : DictionaryApiService {
    
    private val mockService = MockApiService()
    
    override suspend fun getWordDefinition(word: String): Response<WordDefinitionResponse> {
        return mockService.getWordDefinition(word)
    }
    
    override suspend fun getWordWithPhonetics(word: String, includePhonetics: Boolean): Response<WordDefinitionResponse> {
        return mockService.getWordDefinition(word)
    }
    
    override suspend fun searchSimilarWords(query: String, limit: Int): Response<List<String>> {
        // Return some of the available words as suggestions
        val availableWords = mockService.getAvailableWords()
        val filtered = availableWords.filter { it.contains(query.lowercase()) }.take(limit)
        return Response.success(filtered)
    }
}
