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
    val lastRevisedAt: Long = System.currentTimeMillis()
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
