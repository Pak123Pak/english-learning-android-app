package com.example.englishlearningandroidapp.data.repository

import androidx.lifecycle.LiveData
import com.example.englishlearningandroidapp.data.database.Word
import com.example.englishlearningandroidapp.data.database.WordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing word data operations
 * Provides a clean API for data access to ViewModels
 */
class WordRepository(
    private val wordDao: WordDao
) {
    
    /**
     * Get all words as LiveData for reactive UI updates
     */
    fun getAllWords(): LiveData<List<Word>> = wordDao.getAllWords()
    
    /**
     * Get words by revision stage as LiveData
     */
    fun getWordsByStage(stage: Int): LiveData<List<Word>> = wordDao.getWordsByStage(stage)
    
    /**
     * Insert a new word into the database
     * @param word The word to insert
     * @return Result containing the inserted word ID or error
     */
    suspend fun insertWord(word: Word): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Check for duplicates before inserting
            val isDuplicate = wordDao.isDuplicateWord(word.englishWord, word.chineseTranslation)
            if (isDuplicate) {
                return@withContext Result.failure(
                    DuplicateWordException("Word '${word.englishWord}' with translation '${word.chineseTranslation}' already exists")
                )
            }
            
            val id = wordDao.insert(word)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(DatabaseException("Failed to insert word: ${e.message}", e))
        }
    }
    
    /**
     * Update an existing word
     * @param word The word to update
     * @return Result indicating success or failure
     */
    suspend fun updateWord(word: Word): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            wordDao.update(word)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DatabaseException("Failed to update word: ${e.message}", e))
        }
    }
    
    /**
     * Delete a word from the database
     * @param word The word to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteWord(word: Word): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            wordDao.delete(word)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DatabaseException("Failed to delete word: ${e.message}", e))
        }
    }
    
    /**
     * Get a word by its ID
     * @param id The word ID
     * @return The word or null if not found
     */
    suspend fun getWordById(id: Long): Word? = withContext(Dispatchers.IO) {
        try {
            wordDao.getWordById(id)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get words by revision stage (non-LiveData)
     * @param stage The revision stage
     * @return List of words in the specified stage
     */
    suspend fun getWordsByStageSync(stage: Int): List<Word> = withContext(Dispatchers.IO) {
        try {
            wordDao.getWordsByStageSync(stage)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get the first word from a specific stage for revision
     * @param stage The revision stage
     * @return The first word in the stage or null if empty
     */
    suspend fun getFirstWordInStage(stage: Int): Word? = withContext(Dispatchers.IO) {
        try {
            wordDao.getFirstWordInStage(stage)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get total word count
     * @return Total number of words in database
     */
    suspend fun getWordCount(): Int = withContext(Dispatchers.IO) {
        try {
            wordDao.getWordCount()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get word count by stage
     * @param stage The revision stage
     * @return Number of words in the specified stage
     */
    suspend fun getWordCountByStage(stage: Int): Int = withContext(Dispatchers.IO) {
        try {
            wordDao.getWordCountByStage(stage)
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get statistics of words per stage
     * @return Map of stage to word count
     */
    suspend fun getStageStatistics(): Map<Int, Int> = withContext(Dispatchers.IO) {
        try {
            val statisticsList = wordDao.getWordCountPerStage()
            // Convert List<StageStatistics> to Map<Int, Int>
            statisticsList.associate { it.revisionStage to it.count }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Search words by English word
     * @param englishWord The English word to search for
     * @return List of matching words
     */
    suspend fun searchByEnglishWord(englishWord: String): List<Word> = withContext(Dispatchers.IO) {
        try {
            wordDao.searchByEnglishWord(englishWord)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Search words by Chinese translation
     * @param chineseTranslation The Chinese translation to search for
     * @return List of matching words
     */
    suspend fun searchByChineseTranslation(chineseTranslation: String): List<Word> = withContext(Dispatchers.IO) {
        try {
            wordDao.searchByChineseTranslation(chineseTranslation)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Advance word to next revision stage
     * @param word The word to advance
     * @return Result containing the updated word or error
     */
    suspend fun advanceWordStage(word: Word): Result<Word> = withContext(Dispatchers.IO) {
        try {
            val updatedWord = word.advanceStage()
            wordDao.update(updatedWord)
            Result.success(updatedWord)
        } catch (e: Exception) {
            Result.failure(DatabaseException("Failed to advance word stage: ${e.message}", e))
        }
    }
    
    /**
     * Keep word in current stage but update last revised time
     * @param word The word to update
     * @return Result containing the updated word or error
     */
    suspend fun keepWordInStage(word: Word): Result<Word> = withContext(Dispatchers.IO) {
        try {
            val updatedWord = word.updateLastRevised()
            wordDao.update(updatedWord)
            Result.success(updatedWord)
        } catch (e: Exception) {
            Result.failure(DatabaseException("Failed to update word: ${e.message}", e))
        }
    }
    
    /**
     * Get words that need revision (not revised in specified time)
     * @param thresholdTimestamp Words not revised since this timestamp
     * @return List of words needing revision
     */
    suspend fun getWordsNeedingRevision(thresholdTimestamp: Long): List<Word> = withContext(Dispatchers.IO) {
        try {
            wordDao.getWordsNeedingRevision(thresholdTimestamp)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Check if a word already exists with the same English word and Chinese translation
     * @param englishWord The English word
     * @param chineseTranslation The Chinese translation
     * @return True if duplicate exists
     */
    suspend fun isDuplicateWord(englishWord: String, chineseTranslation: String): Boolean = withContext(Dispatchers.IO) {
        try {
            wordDao.isDuplicateWord(englishWord, chineseTranslation)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Delete all words from the database (for testing or reset)
     * @return Result indicating success or failure
     */
    suspend fun deleteAllWords(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            wordDao.deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DatabaseException("Failed to delete all words: ${e.message}", e))
        }
    }
    
    /**
     * Get words created after a specific timestamp
     * @param timestamp The timestamp threshold
     * @return List of words created after the specified time
     */
    suspend fun getWordsCreatedAfter(timestamp: Long): List<Word> = withContext(Dispatchers.IO) {
        try {
            wordDao.getWordsCreatedAfter(timestamp)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Custom exceptions for repository operations
 */
sealed class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)

class DatabaseException(message: String, cause: Throwable? = null) : RepositoryException(message, cause)

class DuplicateWordException(message: String) : RepositoryException(message)

class WordNotFoundException(message: String) : RepositoryException(message)
