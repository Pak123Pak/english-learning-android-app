package com.example.englishlearningandroidapp.ui.dictionary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningandroidapp.data.api.ApiException
import com.example.englishlearningandroidapp.data.api.Definition
import com.example.englishlearningandroidapp.data.api.NetworkResult
import com.example.englishlearningandroidapp.data.api.WordDefinitionResponse
import com.example.englishlearningandroidapp.data.database.Word
import com.example.englishlearningandroidapp.data.repository.ApiRepository
import com.example.englishlearningandroidapp.data.repository.WordRepository
import com.example.englishlearningandroidapp.utils.StringUtils
import com.example.englishlearningandroidapp.utils.ValidationResult
import com.example.englishlearningandroidapp.utils.ValidationUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for Dictionary functionality
 */
class DictionaryViewModel(
    private val wordRepository: WordRepository,
    private val apiRepository: ApiRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "DictionaryViewModel"
        private const val DEBUG_ENABLED = true // Set to false in release builds
    }
    
    // Search results
    private val _searchResults = MutableLiveData<List<Definition>>()
    val searchResults: LiveData<List<Definition>> = _searchResults
    
    // Selected definition
    private val _selectedDefinition = MutableLiveData<Definition?>()
    val selectedDefinition: LiveData<Definition?> = _selectedDefinition
    
    // Current search query
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery
    
    // The actual word returned from API (base form)
    private val _actualWord = MutableLiveData<String?>()
    val actualWord: LiveData<String?> = _actualWord
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Save word state
    private val _saveWordState = MutableLiveData<SaveWordState>()
    val saveWordState: LiveData<SaveWordState> = _saveWordState
    
    // Validation error for search input
    private val _searchValidationError = MutableLiveData<String?>()
    val searchValidationError: LiveData<String?> = _searchValidationError
    
    init {
        _saveWordState.value = SaveWordState.Idle
        _selectedDefinition.value = null
        _isLoading.value = false
    }
    
    /**
     * Search for word definitions
     * @param query The English word to search
     */
    fun searchWord(query: String) {
        val trimmedQuery = query.trim()
        _searchQuery.value = trimmedQuery
        
        debugLog("=== DICTIONARY SEARCH STARTED ===")
        debugLog("Search Query: '$trimmedQuery'")
        
        // Validate search query
        val validationResult = ValidationUtils.validateSearchQuery(trimmedQuery)
        if (!validationResult.isValid()) {
            debugLog("Validation Failed: ${validationResult.getErrorOrNull()}")
            _searchValidationError.value = validationResult.getErrorOrNull()
            return
        }
        
        debugLog("Validation Passed: Query is valid")
        _searchValidationError.value = null
        _isLoading.value = true
        _errorMessage.value = null
        _searchResults.value = emptyList()
        _selectedDefinition.value = null
        _saveWordState.value = SaveWordState.Idle
        
        viewModelScope.launch {
            try {
                debugLog("Making API call to fetch word definition...")
                val result = apiRepository.fetchWordDefinition(trimmedQuery)
                
                when (result) {
                    is NetworkResult.Success -> {
                        debugLog("API Call Success!")
                        logApiResponse(result.data)
                        
                        // Store the actual word from API response (e.g., "apple" instead of "apples")
                        _actualWord.value = result.data.word
                        debugLog("Actual word from API: '${result.data.word}' (searched: '$trimmedQuery')")
                        
                        val definitions = result.data.getMainDefinitions()
                        if (definitions.isNotEmpty()) {
                            debugLog("Found ${definitions.size} definitions")
                            logDefinitions(definitions)
                            _searchResults.value = definitions
                        } else {
                            debugLog("No valid definitions found in response")
                            _errorMessage.value = "No translations found for '$trimmedQuery'"
                        }
                    }
                    is NetworkResult.Error -> {
                        debugLog("API Call Failed: ${result.message}")
                        debugLog("Exception: ${result.exception}")
                        
                        // Check if it's a network connectivity issue
                        val errorMsg = when (result.exception) {
                            is ApiException.NetworkException -> "No Internet"
                            is ApiException.TimeoutException -> "Request timeout. Please try again."
                            else -> result.message ?: "Failed to search word"
                        }
                        _errorMessage.value = errorMsg
                    }
                    is NetworkResult.Loading -> {
                        debugLog("API Call Loading...")
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                debugLog("Exception during search: ${e.message}")
                debugLog("Exception stack trace: ${e.stackTraceToString()}")
                _errorMessage.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
                debugLog("=== DICTIONARY SEARCH COMPLETED ===")
            }
        }
    }
    
    /**
     * Select a definition from search results
     * @param definition The selected definition
     */
    fun selectDefinition(definition: Definition) {
        debugLog("=== DEFINITION SELECTED ===")
        debugLog("Selected Translation: ${definition.translation}")
        debugLog("Part of Speech: ${definition.partOfSpeech}")
        debugLog("Example: ${definition.example ?: "N/A"}")
        debugLog("=== END SELECTION ===")
        
        _selectedDefinition.value = definition
        _saveWordState.value = SaveWordState.Idle
    }
    
    /**
     * Clear the selected definition
     */
    fun clearDefinitionSelection() {
        _selectedDefinition.value = null
        _saveWordState.value = SaveWordState.Idle
    }
    
    /**
     * Save the selected word and definition to database
     */
    fun saveSelectedWord() {
        val currentDefinition = _selectedDefinition.value
        val wordToSave = _actualWord.value // Use the actual word from API (base form)
        
        if (wordToSave.isNullOrBlank() || currentDefinition == null) {
            _saveWordState.value = SaveWordState.Error("No word or definition selected")
            return
        }
        
        // Validate word before saving
        val validationResult = ValidationUtils.validateWordForSaving(
            wordToSave,
            currentDefinition.translation
        )
        
        if (!validationResult.isValid()) {
            _saveWordState.value = SaveWordState.Error(
                validationResult.getErrorOrNull() ?: "Invalid word data"
            )
            return
        }
        
        _saveWordState.value = SaveWordState.Saving
        
        viewModelScope.launch {
            try {
                // Create example sentence with blank using the actual word (base form)
                val exampleSentence = currentDefinition.example ?: "Example with ${wordToSave}."
                val blankExample = StringUtils.createBlankSentence(exampleSentence, wordToSave)
                
                val word = Word(
                    englishWord = wordToSave, // Save the actual word from API (e.g., "apple" not "apples")
                    chineseTranslation = currentDefinition.translation,
                    partOfSpeech = currentDefinition.partOfSpeech,
                    exampleSentence = exampleSentence,
                    blankExampleSentence = blankExample,
                    revisionStage = 0 // Start in "Not revised" stage
                )
                
                logWordSaveOperation(word)
                val result = wordRepository.insertWord(word)
                
                if (result.isSuccess) {
                    debugLog("Word saved successfully to database!")
                    _saveWordState.value = SaveWordState.Success(word)
                    // Clear selection after successful save
                    clearSearchResults()
                } else {
                    val exception = result.exceptionOrNull()
                    debugLog("Failed to save word to database: ${exception?.message}")
                    _saveWordState.value = SaveWordState.Error(
                        exception?.message ?: "Failed to save word"
                    )
                }
            } catch (e: Exception) {
                _saveWordState.value = SaveWordState.Error("Save failed: ${e.message}")
            }
        }
    }
    
    /**
     * Retry the last search (for network error recovery)
     */
    fun retrySearch() {
        val currentQuery = _searchQuery.value
        if (!currentQuery.isNullOrBlank()) {
            searchWord(currentQuery)
        }
    }
    
    /**
     * Clear all search results and reset state
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _selectedDefinition.value = null
        _searchQuery.value = ""
        _actualWord.value = null
        _errorMessage.value = null
        _searchValidationError.value = null
        _saveWordState.value = SaveWordState.Idle
    }
    
    /**
     * Clear error messages
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Clear save word state
     */
    fun clearSaveWordState() {
        _saveWordState.value = SaveWordState.Idle
    }
    
    /**
     * Check if save button should be enabled
     * @return True if word and definition are selected
     */
    fun isSaveButtonEnabled(): Boolean {
        return !_actualWord.value.isNullOrBlank() && 
               _selectedDefinition.value != null &&
               _saveWordState.value !is SaveWordState.Saving
    }
    
    /**
     * Get current word count for statistics
     */
    fun loadWordStatistics() {
        viewModelScope.launch {
            try {
                val totalWords = wordRepository.getWordCount()
                val stageStats = wordRepository.getStageStatistics()
                // You can emit this data if needed for UI
            } catch (e: Exception) {
                // Handle error silently for statistics
            }
        }
    }
    
    // ==================== DEBUG LOGGING METHODS ====================
    
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
    
    /**
     * Log the complete API response for debugging
     */
    private fun logApiResponse(response: WordDefinitionResponse) {
        if (!DEBUG_ENABLED) return
        
        debugLog("=== API RESPONSE DETAILS ===")
        debugLog("Word: ${response.word}")
        debugLog("Phonetic: ${response.phonetic ?: "N/A"}")
        debugLog("Origin: ${response.origin ?: "N/A"}")
        debugLog("Total Definitions: ${response.definitions.size}")
        
        // Log phonetics if available
        response.phonetics?.let { phonetics ->
            debugLog("--- Phonetics (${phonetics.size}) ---")
            phonetics.forEachIndexed { index, phonetic ->
                debugLog("  Phonetic $index:")
                debugLog("    Text: ${phonetic.text ?: "N/A"}")
                debugLog("    Audio: ${phonetic.audio ?: "N/A"}")
                debugLog("    Has Audio: ${phonetic.hasAudio()}")
            }
        }
        
        // Log all definitions
        debugLog("--- All Definitions ---")
        response.definitions.forEachIndexed { index, definition ->
            debugLog("  Definition ${index + 1}:")
            debugLog("    Translation: ${definition.translation}")
            debugLog("    Part of Speech: ${definition.partOfSpeech}")
            debugLog("    Example: ${definition.example ?: "N/A"}")
            debugLog("    Has Example: ${definition.hasExample()}")
            
            definition.synonyms?.let { synonyms ->
                if (synonyms.isNotEmpty()) {
                    debugLog("    Synonyms: ${synonyms.joinToString(", ")}")
                }
            }
            
            definition.antonyms?.let { antonyms ->
                if (antonyms.isNotEmpty()) {
                    debugLog("    Antonyms: ${antonyms.joinToString(", ")}")
                }
            }
        }
        
        debugLog("=== END API RESPONSE ===")
    }
    
    /**
     * Log the filtered definitions that will be shown to the user
     */
    private fun logDefinitions(definitions: List<Definition>) {
        if (!DEBUG_ENABLED) return
        
        debugLog("=== FILTERED DEFINITIONS FOR UI ===")
        definitions.forEachIndexed { index, definition ->
            debugLog("  UI Definition ${index + 1}:")
            debugLog("    Translation: ${definition.translation}")
            debugLog("    Part of Speech: ${definition.partOfSpeech}")
            debugLog("    Example: ${definition.example ?: "N/A"}")
            if (definition.hasExample()) {
                // Show what the blank example would look like
                val searchQuery = _searchQuery.value ?: ""
                if (searchQuery.isNotBlank()) {
                    val blankExample = definition.createBlankExample(searchQuery)
                    debugLog("    Blank Example: $blankExample")
                }
            }
        }
        debugLog("=== END FILTERED DEFINITIONS ===")
    }
    
    /**
     * Log word save operation details
     */
    private fun logWordSaveOperation(word: Word) {
        if (!DEBUG_ENABLED) return
        
        debugLog("=== WORD SAVE OPERATION ===")
        debugLog("English Word: ${word.englishWord}")
        debugLog("Chinese Translation: ${word.chineseTranslation}")
        debugLog("Part of Speech: ${word.partOfSpeech}")
        debugLog("Example Sentence: ${word.exampleSentence}")
        debugLog("Blank Example: ${word.blankExampleSentence}")
        debugLog("Revision Stage: ${word.revisionStage} (${word.getStageDisplayName()})")
        debugLog("Created At: ${word.createdAt}")
        debugLog("Last Revised At: ${word.lastRevisedAt}")
        debugLog("=== END WORD SAVE ===")
    }
}

/**
 * Sealed class representing the state of saving a word
 */
sealed class SaveWordState {
    object Idle : SaveWordState()
    object Saving : SaveWordState()
    data class Success(val word: Word) : SaveWordState()
    data class Error(val message: String) : SaveWordState()
}
