package com.example.englishlearningandroidapp.data.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Word::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class WordDatabase : RoomDatabase() {
    
    abstract fun wordDao(): WordDao
    
    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null
        
        /**
         * Migration from version 1 to 2 - adds pronunciationData column
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new pronunciationData column with default null value
                database.execSQL(
                    "ALTER TABLE words ADD COLUMN pronunciationData TEXT DEFAULT NULL"
                )
            }
        }
        
        /**
         * Get database instance (singleton pattern)
         * @param context Application context
         * @return Database instance
         */
        fun getDatabase(context: Context): WordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordDatabase::class.java,
                    "word_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(WordDatabaseCallback())
                // NOTE: fallbackToDestructiveMigration() is disabled to protect user data
                // All schema changes must have proper migrations defined
                // Uncomment only during development if needed:
                // .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Close database instance (for testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
    
    /**
     * Database callback for initialization
     */
    private class WordDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            // Populate database with sample data if needed
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.wordDao())
                }
            }
        }
        
        /**
         * Populate database with sample data
         * @param wordDao The DAO instance
         */
        private suspend fun populateDatabase(wordDao: WordDao) {
            // Optional: Add sample words for testing
            // Uncomment and customize as needed
            /*
            val sampleWords = listOf(
                Word(
                    englishWord = "hello",
                    chineseTranslation = "你好",
                    partOfSpeech = "exclamation",
                    exampleSentence = "Hello, how are you?",
                    blankExampleSentence = "___, how are you?",
                    revisionStage = 0
                ),
                Word(
                    englishWord = "book",
                    chineseTranslation = "書",
                    partOfSpeech = "noun",
                    exampleSentence = "I am reading a book.",
                    blankExampleSentence = "I am reading a ___.",
                    revisionStage = 0
                )
            )
            
            wordDao.insertAll(sampleWords)
            */
        }
    }
}

/**
 * Type converters for Room database
 */
class DatabaseTypeConverters {
    
    /**
     * Convert timestamp to date string (if needed)
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): String? {
        return value?.let { 
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(it))
        }
    }
    
    /**
     * Convert date string to timestamp (if needed)
     */
    @TypeConverter
    fun dateToTimestamp(date: String?): Long? {
        return date?.let {
            try {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .parse(it)?.time
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Convert comma-separated string to list (for future use with synonyms/antonyms)
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    
    /**
     * Convert list to comma-separated string (for future use with synonyms/antonyms)
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }
}
