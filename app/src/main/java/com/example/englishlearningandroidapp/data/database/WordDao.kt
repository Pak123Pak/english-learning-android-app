package com.example.englishlearningandroidapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WordDao {
    
    /**
     * Insert a new word into the database
     * @param word The word to insert
     * @return The ID of the inserted word
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: Word): Long
    
    /**
     * Insert multiple words into the database
     * @param words List of words to insert
     * @return List of IDs of the inserted words
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<Word>): List<Long>
    
    /**
     * Update an existing word in the database
     * @param word The word to update
     */
    @Update
    suspend fun update(word: Word)
    
    /**
     * Delete a word from the database
     * @param word The word to delete
     */
    @Delete
    suspend fun delete(word: Word)
    
    /**
     * Delete all words from the database
     */
    @Query("DELETE FROM words")
    suspend fun deleteAll()
    
    /**
     * Get a word by its ID
     * @param id The ID of the word
     * @return The word or null if not found
     */
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): Word?
    
    /**
     * Get all words from the database (LiveData for reactive UI)
     * @return LiveData list of all words ordered by creation time
     */
    @Query("SELECT * FROM words ORDER BY createdAt ASC")
    fun getAllWords(): LiveData<List<Word>>
    
    /**
     * Get all words from the database (non-LiveData for repository use)
     * @return List of all words ordered by creation time
     */
    @Query("SELECT * FROM words ORDER BY createdAt ASC")
    suspend fun getAllWordsSync(): List<Word>
    
    /**
     * Get words by revision stage (LiveData for reactive UI)
     * @param stage The revision stage (0-5)
     * @return LiveData list of words in the specified stage ordered by lastRevisedAt
     */
    @Query("SELECT * FROM words WHERE revisionStage = :stage ORDER BY lastRevisedAt ASC")
    fun getWordsByStage(stage: Int): LiveData<List<Word>>
    
    /**
     * Get words by revision stage (non-LiveData for repository use)
     * @param stage The revision stage (0-5)
     * @return List of words in the specified stage ordered by lastRevisedAt
     */
    @Query("SELECT * FROM words WHERE revisionStage = :stage ORDER BY lastRevisedAt ASC")
    suspend fun getWordsByStageSync(stage: Int): List<Word>
    
    /**
     * Get the first word from a specific stage (for revision)
     * @param stage The revision stage
     * @return The first word in the stage or null if stage is empty
     */
    @Query("SELECT * FROM words WHERE revisionStage = :stage ORDER BY lastRevisedAt ASC LIMIT 1")
    suspend fun getFirstWordInStage(stage: Int): Word?
    
    /**
     * Get total count of words in the database
     * @return Total number of words
     */
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int
    
    /**
     * Get count of words in a specific stage
     * @param stage The revision stage
     * @return Number of words in the specified stage
     */
    @Query("SELECT COUNT(*) FROM words WHERE revisionStage = :stage")
    suspend fun getWordCountByStage(stage: Int): Int
    
    /**
     * Get count of words per stage (for statistics)
     * @return List of stage statistics
     */
    @Query("SELECT revisionStage, COUNT(*) as count FROM words GROUP BY revisionStage")
    suspend fun getWordCountPerStage(): List<StageStatistics>
    
    /**
     * Search words by English word (for checking duplicates)
     * @param englishWord The English word to search for
     * @return List of matching words
     */
    @Query("SELECT * FROM words WHERE LOWER(englishWord) = LOWER(:englishWord)")
    suspend fun searchByEnglishWord(englishWord: String): List<Word>
    
    /**
     * Search words by Chinese translation
     * @param chineseTranslation The Chinese translation to search for
     * @return List of matching words
     */
    @Query("SELECT * FROM words WHERE chineseTranslation LIKE '%' || :chineseTranslation || '%'")
    suspend fun searchByChineseTranslation(chineseTranslation: String): List<Word>
    
    /**
     * Get words created after a specific timestamp
     * @param timestamp The timestamp threshold
     * @return List of words created after the specified time
     */
    @Query("SELECT * FROM words WHERE createdAt > :timestamp ORDER BY createdAt ASC")
    suspend fun getWordsCreatedAfter(timestamp: Long): List<Word>
    
    /**
     * Get words that need revision (not revised in last N days)
     * @param thresholdTimestamp Words not revised since this timestamp
     * @return List of words needing revision
     */
    @Query("SELECT * FROM words WHERE lastRevisedAt < :thresholdTimestamp ORDER BY lastRevisedAt ASC")
    suspend fun getWordsNeedingRevision(thresholdTimestamp: Long): List<Word>
    
    /**
     * Update revision stage for a specific word
     * @param id The word ID
     * @param newStage The new revision stage
     * @param timestamp The current timestamp
     */
    @Query("UPDATE words SET revisionStage = :newStage, lastRevisedAt = :timestamp WHERE id = :id")
    suspend fun updateWordStage(id: Long, newStage: Int, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Get words sorted by last revised time (oldest first)
     * @return List of words sorted by lastRevisedAt ascending
     */
    @Query("SELECT * FROM words ORDER BY lastRevisedAt ASC")
    suspend fun getWordsByLastRevised(): List<Word>
    
    /**
     * Check if a word with the same English word and Chinese translation already exists
     * @param englishWord The English word
     * @param chineseTranslation The Chinese translation
     * @return True if duplicate exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM words WHERE LOWER(englishWord) = LOWER(:englishWord) AND chineseTranslation = :chineseTranslation")
    suspend fun isDuplicateWord(englishWord: String, chineseTranslation: String): Boolean
}
