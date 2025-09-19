package com.example.englishlearningandroidapp.utils

/**
 * Utility class for input validation and answer checking
 */
object ValidationUtils {
    
    /**
     * Check if an answer is correct (case-insensitive and whitespace-tolerant)
     * @param correctAnswer The correct English word
     * @param userAnswer The user's input
     * @return True if answers match
     */
    fun isAnswerCorrect(correctAnswer: String, userAnswer: String): Boolean {
        val normalizedCorrect = normalizeAnswer(correctAnswer)
        val normalizedUser = normalizeAnswer(userAnswer)
        return normalizedCorrect.equals(normalizedUser, ignoreCase = true)
    }
    
    /**
     * Normalize an answer by trimming whitespace and converting to lowercase
     * @param answer The answer to normalize
     * @return Normalized answer
     */
    fun normalizeAnswer(answer: String): String {
        return answer.trim().lowercase()
    }
    
    /**
     * Validate if a search query is valid
     * @param query The search query
     * @return ValidationResult indicating if valid or not
     */
    fun validateSearchQuery(query: String): ValidationResult {
        val trimmedQuery = query.trim()
        
        return when {
            trimmedQuery.isEmpty() -> ValidationResult.Invalid("Please enter a word to search")
            trimmedQuery.length < 2 -> ValidationResult.Invalid("Word must be at least 2 characters long")
            trimmedQuery.length > 50 -> ValidationResult.Invalid("Word is too long")
            !isValidEnglishWord(trimmedQuery) -> ValidationResult.Invalid("Please enter a valid English word")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Check if a string contains only valid English word characters
     * @param word The word to validate
     * @return True if valid English word
     */
    fun isValidEnglishWord(word: String): Boolean {
        val englishWordPattern = Regex("^[a-zA-Z\\-'\\s]+$")
        return englishWordPattern.matches(word.trim())
    }
    
    /**
     * Validate definition selection
     * @param selectedDefinition The selected definition (can be null)
     * @return ValidationResult indicating if valid or not
     */
    fun validateDefinitionSelection(selectedDefinition: Any?): ValidationResult {
        return if (selectedDefinition != null) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Please select a definition")
        }
    }
    
    /**
     * Check if a word is suitable for saving (not too short/long, contains meaning)
     * @param englishWord The English word
     * @param chineseTranslation The Chinese translation
     * @return ValidationResult
     */
    fun validateWordForSaving(englishWord: String, chineseTranslation: String): ValidationResult {
        return when {
            englishWord.trim().isEmpty() -> ValidationResult.Invalid("English word cannot be empty")
            chineseTranslation.trim().isEmpty() -> ValidationResult.Invalid("Chinese translation cannot be empty")
            englishWord.trim().length < 2 -> ValidationResult.Invalid("English word too short")
            englishWord.trim().length > 50 -> ValidationResult.Invalid("English word too long")
            chineseTranslation.trim().length > 100 -> ValidationResult.Invalid("Chinese translation too long")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Sealed class for validation results
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    fun getErrorOrNull(): String? = if (this is Invalid) errorMessage else null
}
