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
    
    // Hint state - number of letters revealed
    private val _revealedLettersCount = MutableLiveData<Int>()
    val revealedLettersCount: LiveData<Int> = _revealedLettersCount
    
    // Current displayed example sentence (with partially revealed word)
    private val _displayedExampleSentence = MutableLiveData<String>()
    val displayedExampleSentence: LiveData<String> = _displayedExampleSentence
    
    // Hint button enabled state
    private val _isHintButtonEnabled = MutableLiveData<Boolean>()
    val isHintButtonEnabled: LiveData<Boolean> = _isHintButtonEnabled
    
    // Track if the last answer moved the word out of current stage
    private var lastAnswerMovedWord = false
    
    init {
        _currentStage.value = 0 // Start with "Not revised"
        _currentPosition.value = 0
        _isLoading.value = false
        _showFeedback.value = false
        _revealedLettersCount.value = 0
        _isHintButtonEnabled.value = true
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
            lastAnswerMovedWord = false
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
                    resetHintState()
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
                    // Word will move to next stage
                    lastAnswerMovedWord = true
                    wordRepository.advanceWordStage(word)
                } else {
                    // Word stays in "5th or above" stage
                    lastAnswerMovedWord = false
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
                // Word stays in same stage, just moves to end
                lastAnswerMovedWord = false
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
     * 
     * Logic:
     * - If last answer moved word out of stage (correct answer in stages 0-4):
     *   Stay at same position, but total decreases (e.g., "Word 1 of 4" → "Word 1 of 3")
     * - If last answer kept word in stage (incorrect or stage 5):
     *   Move to next position with same total (e.g., "Word 2 of 10" → "Word 3 of 10")
     */
    private fun moveToNextWord() {
        val currentPos = _currentPosition.value ?: 0
        
        // Reload words from database to get the updated list
        viewModelScope.launch {
            try {
                val stage = _currentStage.value ?: 0
                val updatedWords = wordRepository.getWordsByStageSync(stage)
                _wordsInCurrentStage.value = updatedWords
                
                if (updatedWords.isEmpty()) {
                    // No more words in this stage
                    _currentWord.value = null
                    _currentPosition.value = 0
                    _progressText.value = ""
                } else {
                    // Determine next position based on whether word moved out of stage
                    val nextPosition = if (lastAnswerMovedWord) {
                        // Word moved to another stage, stay at same position
                        currentPos
                    } else {
                        // Word stayed in stage (moved to end), advance position
                        currentPos + 1
                    }
                    
                    // Check if we have a word at this position
                    if (nextPosition <= updatedWords.size) {
                        _currentWord.value = updatedWords[nextPosition - 1]
                        _currentPosition.value = nextPosition
                        updateProgressText(nextPosition, updatedWords.size)
                        resetHintState()
                    } else {
                        // We've gone through all words, reload from beginning
                        loadWordsForCurrentStage()
                    }
                }
                
                // Reset the flag for next iteration
                lastAnswerMovedWord = false
                
                // Clear feedback after moving to next word
                _showFeedback.value = false
                _answerResult.value = AnswerResult.Idle
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load next word: ${e.message}"
            }
        }
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
     * Navigate to a specific word position in the current stage
     * @param position The 1-based position to navigate to (e.g., 1 for first word)
     */
    fun navigateToWordAtPosition(position: Int) {
        val words = _wordsInCurrentStage.value ?: return
        
        if (position < 1 || position > words.size) {
            _errorMessage.value = "Invalid word position"
            return
        }
        
        // Update current position and word
        _currentPosition.value = position
        _currentWord.value = words[position - 1]
        updateProgressText(position, words.size)
        resetHintState()
        
        // Clear any previous answer state
        _answerResult.value = AnswerResult.Idle
        _showFeedback.value = false
        lastAnswerMovedWord = false
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
    
    /**
     * Reset hint state when showing a new word
     */
    private fun resetHintState() {
        _revealedLettersCount.value = 0
        _isHintButtonEnabled.value = true
        updateDisplayedSentence()
    }
    
    /**
     * Reveal one more letter in the blank of the example sentence
     */
    fun revealNextLetter() {
        val currentWord = _currentWord.value ?: return
        val currentRevealed = _revealedLettersCount.value ?: 0
        val wordLength = currentWord.englishWord.length
        
        // Only reveal if there are more letters to reveal
        if (currentRevealed < wordLength) {
            val newRevealedCount = currentRevealed + 1
            _revealedLettersCount.value = newRevealedCount
            
            // Disable hint button if all letters are now revealed
            if (newRevealedCount >= wordLength) {
                _isHintButtonEnabled.value = false
            }
            
            updateDisplayedSentence()
        }
    }
    
    /**
     * Update the displayed example sentence based on revealed letters count
     */
    private fun updateDisplayedSentence() {
        val currentWord = _currentWord.value ?: return
        val revealedCount = _revealedLettersCount.value ?: 0
        
        if (revealedCount == 0) {
            // No letters revealed, show original blank sentence
            _displayedExampleSentence.value = currentWord.blankExampleSentence
        } else {
            // Replace the blank with partially revealed word
            val englishWord = currentWord.englishWord.lowercase()
            val revealedPart = englishWord.substring(0, revealedCount.coerceAtMost(englishWord.length))
            val blankPart = if (revealedCount < englishWord.length) "_" else ""
            val partialWord = revealedPart + blankPart
            
            // Replace the blank (represented by multiple underscores or _) with partial word
            val displayedSentence = currentWord.blankExampleSentence.replace(Regex("_{1,}"), partialWord)
            _displayedExampleSentence.value = displayedSentence
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
