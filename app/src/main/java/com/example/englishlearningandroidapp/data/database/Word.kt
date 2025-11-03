package com.example.englishlearningandroidapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val englishWord: String,
    val chineseTranslation: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val blankExampleSentence: String,
    val revisionStage: Int = 0, // 0: Not revised, 1: 1st, ..., 5: 5th or above
    val createdAt: Long = System.currentTimeMillis(),
    val lastRevisedAt: Long = System.currentTimeMillis(),
    // Pronunciation URLs (stores JSON-encoded list of pronunciation objects for the selected part of speech)
    val pronunciationData: String? = null // JSON format: [{"lang":"uk","url":"...","pron":"..."},{"lang":"us","url":"...","pron":"..."}]
) {
    /**
     * Get display name for the revision stage
     */
    fun getStageDisplayName(): String {
        return when (revisionStage) {
            0 -> "Not revised"
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            4 -> "4th"
            5 -> "5th or above"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if word is ready for next stage progression
     */
    fun canAdvanceStage(): Boolean {
        return revisionStage < 5
    }
    
    /**
     * Get the next stage number
     */
    fun getNextStage(): Int {
        return if (canAdvanceStage()) revisionStage + 1 else 5
    }
    
    /**
     * Create a copy with incremented stage
     */
    fun advanceStage(): Word {
        return copy(
            revisionStage = getNextStage(),
            lastRevisedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Create a copy with updated lastRevisedAt (for incorrect answers)
     */
    fun updateLastRevised(): Word {
        return copy(lastRevisedAt = System.currentTimeMillis())
    }
    
    /**
     * Get pronunciation URLs as a list of PronunciationInfo objects
     */
    fun getPronunciations(): List<PronunciationInfo> {
        if (pronunciationData.isNullOrBlank()) return emptyList()
        
        return try {
            // Parse JSON format: [{"lang":"uk","url":"...","pron":"..."},...]
            val pronunciations = mutableListOf<PronunciationInfo>()
            
            // Remove outer brackets and split by objects
            val trimmed = pronunciationData.trim()
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
                return emptyList()
            }
            
            val content = trimmed.substring(1, trimmed.length - 1)
            if (content.isBlank()) return emptyList()
            
            // Split by "},{" to separate objects
            val objects = content.split("},{")
            
            for (obj in objects) {
                var objStr = obj.trim()
                // Remove leading { and trailing }
                if (objStr.startsWith("{")) objStr = objStr.substring(1)
                if (objStr.endsWith("}")) objStr = objStr.substring(0, objStr.length - 1)
                
                if (objStr.isBlank()) continue
                
                var lang = ""
                var url = ""
                var pron = ""
                
                // Parse key-value pairs
                // Handle URLs that contain commas or colons by using regex
                val regex = """"(\w+)":"([^"]*)"""".toRegex()
                val matches = regex.findAll(objStr)
                
                for (match in matches) {
                    val key = match.groupValues[1]
                    val value = match.groupValues[2]
                    when (key) {
                        "lang" -> lang = value
                        "url" -> url = value
                        "pron" -> pron = value
                    }
                }
                
                if (lang.isNotBlank() && url.isNotBlank()) {
                    pronunciations.add(PronunciationInfo(lang, url, pron))
                }
            }
            
            pronunciations
        } catch (e: Exception) {
            // Log error for debugging
            android.util.Log.e("Word", "Error parsing pronunciation data: ${e.message}", e)
            android.util.Log.e("Word", "Pronunciation data was: $pronunciationData")
            emptyList()
        }
    }
    
    /**
     * Check if word has pronunciation data
     */
    fun hasPronunciation(): Boolean {
        return !pronunciationData.isNullOrBlank()
    }
    
    companion object {
        const val STAGE_NOT_REVISED = 0
        const val STAGE_FIRST = 1
        const val STAGE_SECOND = 2
        const val STAGE_THIRD = 3
        const val STAGE_FOURTH = 4
        const val STAGE_FIFTH_OR_ABOVE = 5
        
        /**
         * Get all available stage numbers
         */
        fun getAllStages(): List<Int> {
            return listOf(
                STAGE_NOT_REVISED,
                STAGE_FIRST,
                STAGE_SECOND,
                STAGE_THIRD,
                STAGE_FOURTH,
                STAGE_FIFTH_OR_ABOVE
            )
        }
        
        /**
         * Get stage display name by number
         */
        fun getStageDisplayName(stage: Int): String {
            return when (stage) {
                STAGE_NOT_REVISED -> "Not revised"
                STAGE_FIRST -> "1st"
                STAGE_SECOND -> "2nd"
                STAGE_THIRD -> "3rd"
                STAGE_FOURTH -> "4th"
                STAGE_FIFTH_OR_ABOVE -> "5th or above"
                else -> "Unknown"
            }
        }
    }
}

/**
 * Simple data class for pronunciation information
 */
data class PronunciationInfo(
    val lang: String,  // "uk" or "us" or other
    val url: String,   // Audio URL
    val pron: String   // Phonetic transcription
)
