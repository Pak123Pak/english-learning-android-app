package com.example.englishlearningandroidapp.ui.revision

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningandroidapp.data.database.Word
import com.example.englishlearningandroidapp.data.repository.WordRepository
import com.example.englishlearningandroidapp.utils.StringUtils
import com.example.englishlearningandroidapp.utils.ValidationUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for Revision functionality
 */
class RevisionViewModel(
    private val wordRepository: WordRepository
) : ViewModel() {
    
    // Current revision stage
    private val _currentStage = MutableLiveData<Int>()
    val currentStage: LiveData<Int> = _currentStage
    
    // Current word being reviewed
    private val _currentWord = MutableLiveData<Word?>()
    val currentWord: LiveData<Word?> = _currentWord
    
    // Words in current stage
    private val _wordsInCurrentStage = MutableLiveData<List<Word>>()
    val wordsInCurrentStage: LiveData<List<Word>> = _wordsInCurrentStage
    
    // Current position in stage
    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition
    
    // Answer result
    private val _answerResult = MutableLiveData<AnswerResult>()
    val answerResult: LiveData<AnswerResult> = _answerResult
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Success messages
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    // Stage statistics
    private val _stageStatistics = MutableLiveData<Map<Int, Int>>()
    val stageStatistics: LiveData<Map<Int, Int>> = _stageStatistics
    
    // Progress information
    private val _progressText = MutableLiveData<String>()
    val progressText: LiveData<String> = _progressText
    
    // Show feedback state
    private val _showFeedback = MutableLiveData<Boolean>()
    val showFeedback: LiveData<Boolean> = _showFeedback
    
    init {
        _currentStage.value = 0 // Start with "Not revised"
        _currentPosition.value = 0
        _isLoading.value = false
        _showFeedback.value = false
        loadStageStatistics()
        loadWordsForCurrentStage()
    }
    
    /**
     * Set the current revision stage
     * @param stage The stage to switch to (0-5)
     */
    fun setCurrentStage(stage: Int) {
        if (stage != _currentStage.value) {
            _currentStage.value = stage
            _currentPosition.value = 0
            _answerResult.value = AnswerResult.Idle
            _showFeedback.value = false
            loadWordsForCurrentStage()
        }
    }
    
    /**
     * Load words for the current stage
     */
    private fun loadWordsForCurrentStage() {
        val stage = _currentStage.value ?: 0
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val words = wordRepository.getWordsByStageSync(stage)
                _wordsInCurrentStage.value = words
                
                if (words.isNotEmpty()) {
                    _currentWord.value = words.first()
                    _currentPosition.value = 1
                    updateProgressText(1, words.size)
                } else {
                    _currentWord.value = null
                    _currentPosition.value = 0
                    _progressText.value = ""
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load words: ${e.message}"
                _wordsInCurrentStage.value = emptyList()
                _currentWord.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Submit an answer for the current word
     * @param userAnswer The user's input answer
     */
    fun submitAnswer(userAnswer: String) {
        val currentWord = _currentWord.value
        if (currentWord == null) {
            _answerResult.value = AnswerResult.Error("No word to check")
            return
        }
        
        val trimmedAnswer = userAnswer.trim()
        if (trimmedAnswer.isEmpty()) {
            _answerResult.value = AnswerResult.Error("Please enter an answer")
            return
        }
        
        val isCorrect = ValidationUtils.isAnswerCorrect(currentWord.englishWord, trimmedAnswer)
        
        _showFeedback.value = true
        
        if (isCorrect) {
            _answerResult.value = AnswerResult.Correct(currentWord)
            handleCorrectAnswer(currentWord)
        } else {
            _answerResult.value = AnswerResult.Incorrect(currentWord, currentWord.englishWord)
            handleIncorrectAnswer(currentWord)
        }
    }
    
    /**
     * Handle correct answer logic
     */
    private fun handleCorrectAnswer(word: Word) {
        viewModelScope.launch {
            try {
                // Advance word to next stage or keep in "5th or above"
                val result = if (word.revisionStage < 5) {
                    wordRepository.advanceWordStage(word)
                } else {
                    wordRepository.keepWordInStage(word)
                }
                
                if (result.isFailure) {
                    _errorMessage.value = "Failed to update word progress"
                }
                
                // Note: Don't automatically move to next word, wait for user to click continue
                
            } catch (e: Exception) {
                _errorMessage.value = "Error updating progress: ${e.message}"
            }
        }
    }
    
    /**
     * Handle incorrect answer logic
     */
    private fun handleIncorrectAnswer(word: Word) {
        viewModelScope.launch {
            try {
                // Keep word in current stage but update last revised time
                val result = wordRepository.keepWordInStage(word)
                
                if (result.isFailure) {
                    _errorMessage.value = "Failed to update word"
                }
                
                // Note: Don't automatically move to next word, wait for user to click continue
                
            } catch (e: Exception) {
                _errorMessage.value = "Error updating word: ${e.message}"
            }
        }
    }
    
    /**
     * Move to the next word in the current stage
     */
    private fun moveToNextWord() {
        val words = _wordsInCurrentStage.value ?: return
        val currentPos = _currentPosition.value ?: 0
        
        if (currentPos < words.size) {
            // Move to next word in the list
            val nextPosition = currentPos + 1
            _currentWord.value = words[nextPosition - 1]
            _currentPosition.value = nextPosition
            updateProgressText(nextPosition, words.size)
        } else {
            // Reload the stage (words may have moved)
            loadWordsForCurrentStage()
        }
        
        // Clear feedback after moving to next word
        _showFeedback.value = false
        _answerResult.value = AnswerResult.Idle
    }
    
    /**
     * Skip the current word (move to next without answering)
     */
    fun skipCurrentWord() {
        moveToNextWord()
    }
    
    /**
     * Load stage statistics for display
     */
    private fun loadStageStatistics() {
        viewModelScope.launch {
            try {
                val stats = wordRepository.getStageStatistics()
                _stageStatistics.value = stats
            } catch (e: Exception) {
                _stageStatistics.value = emptyMap()
            }
        }
    }
    
    /**
     * Refresh current stage data
     */
    fun refreshCurrentStage() {
        loadWordsForCurrentStage()
        loadStageStatistics()
    }
    
    /**
     * Get the number of words in a specific stage
     * @param stage The stage number
     * @return Number of words in that stage
     */
    fun getWordCountInStage(stage: Int): Int {
        return _stageStatistics.value?.get(stage) ?: 0
    }
    
    /**
     * Update progress text
     */
    private fun updateProgressText(current: Int, total: Int) {
        _progressText.value = StringUtils.formatProgress(current, total)
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
    
    /**
     * Clear answer result and feedback
     */
    fun clearAnswerResult() {
        _answerResult.value = AnswerResult.Idle
        _showFeedback.value = false
    }
    
    /**
     * Manually continue to next word (triggered by user clicking continue button)
     */
    fun continueToNextWord() {
        moveToNextWord()
    }
    
    /**
     * Get all stage names for spinner
     */
    fun getStageNames(): List<String> {
        return listOf(
            "Not revised",
            "1st",
            "2nd",
            "3rd",
            "4th",
            "5th or above"
        )
    }
    
    /**
     * Check if current stage has words
     */
    fun hasWordsInCurrentStage(): Boolean {
        return !_wordsInCurrentStage.value.isNullOrEmpty()
    }
    
    /**
     * Get total word count across all stages
     */
    fun getTotalWordCount(): Int {
        return _stageStatistics.value?.values?.sum() ?: 0
    }
    
    /**
     * Delete the current word
     */
    fun deleteCurrentWord() {
        val currentWord = _currentWord.value
        if (currentWord == null) {
            _errorMessage.value = "No word to delete"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = wordRepository.deleteWord(currentWord)
                if (result.isSuccess) {
                    _successMessage.value = "Word deleted successfully"
                    // Refresh the current stage to update the word list
                    loadWordsForCurrentStage()
                    loadStageStatistics()
                } else {
                    _errorMessage.value = "Failed to delete word"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting word: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Sealed class representing answer results
 */
sealed class AnswerResult {
    object Idle : AnswerResult()
    data class Correct(val word: Word) : AnswerResult()
    data class Incorrect(val word: Word, val correctAnswer: String) : AnswerResult()
    data class Error(val message: String) : AnswerResult()
}
