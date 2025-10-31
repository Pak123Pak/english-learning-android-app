package com.example.englishlearningandroidapp.utils

/**
 * Utility class for string operations
 */
object StringUtils {
    
    /**
     * Create a blank sentence by replacing the word with "___"
     * @param sentence The original sentence
     * @param word The word to replace (base form)
     * @return Sentence with word replaced by blank
     */
    fun createBlankSentence(sentence: String, word: String): String {
        if (sentence.isEmpty() || word.isEmpty()) {
            return "Example with ___."
        }
        
        // Create a regex pattern that matches the word with word boundaries
        // This ensures we match whole words only, not parts of other words
        val pattern = "\\b${Regex.escape(word)}\\b".toRegex(RegexOption.IGNORE_CASE)
        
        // Replace all occurrences of the word (case-insensitive) with blank
        var result = pattern.replace(sentence, "___")
        
        // If no replacement was made, the word might not be in the sentence exactly
        // In that case, return the original sentence with a note
        if (result == sentence) {
            // Try to find inflected forms (plural, past tense, etc.)
            result = replaceInflectedForms(sentence, word)
        }
        
        return result
    }
    
    /**
     * Attempt to replace inflected forms of a word in a sentence
     * @param sentence The sentence
     * @param baseWord The base form of the word
     * @return Sentence with inflected forms replaced
     */
    private fun replaceInflectedForms(sentence: String, baseWord: String): String {
        var result = sentence
        
        // Common inflection patterns
        val inflections = mutableListOf<String>()
        
        // Add the base word itself
        inflections.add(baseWord)
        
        // Plural forms
        inflections.add("${baseWord}s")        // book -> books
        inflections.add("${baseWord}es")       // box -> boxes
        if (baseWord.endsWith("y")) {
            inflections.add("${baseWord.dropLast(1)}ies")  // baby -> babies
        }
        
        // Past tense forms
        inflections.add("${baseWord}ed")       // cook -> cooked
        if (baseWord.endsWith("e")) {
            inflections.add("${baseWord}d")    // bake -> baked
        }
        if (baseWord.endsWith("y")) {
            inflections.add("${baseWord.dropLast(1)}ied")  // carry -> carried
        }
        
        // Present tense (third person singular)
        inflections.add("${baseWord}s")
        if (baseWord.endsWith("y")) {
            inflections.add("${baseWord.dropLast(1)}ies")
        }
        
        // Continuous forms
        inflections.add("${baseWord}ing")      // cook -> cooking
        if (baseWord.endsWith("e")) {
            inflections.add("${baseWord.dropLast(1)}ing")  // bake -> baking
        }
        
        // Try to replace each inflected form
        for (inflection in inflections.distinct()) {
            val pattern = "\\b${Regex.escape(inflection)}\\b".toRegex(RegexOption.IGNORE_CASE)
            result = pattern.replace(result, "___")
            // If we found a match, stop looking
            if (result != sentence) {
                break
            }
        }
        
        return result
    }
    
    /**
     * Extract potential word from a sentence (remove punctuation)
     * @param sentence The sentence containing the word
     * @return Cleaned word
     */
    fun extractWordFromSentence(sentence: String): String {
        return sentence.replace(Regex("[^a-zA-Z\\s]"), "").trim()
    }
    
    /**
     * Format stage display name
     * @param stage The stage number (0-5)
     * @return Formatted stage name
     */
    fun formatStageDisplay(stage: Int): String {
        return when (stage) {
            0 -> "Not revised"
            1 -> "1st review"
            2 -> "2nd review"
            3 -> "3rd review"
            4 -> "4th review"
            5 -> "5th or above"
            else -> "Unknown stage"
        }
    }
    
    /**
     * Truncate text to specified length with ellipsis
     * @param text The text to truncate
     * @param maxLength Maximum length
     * @return Truncated text
     */
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - 3) + "..."
        }
    }
    
    /**
     * Format progress string
     * @param current Current position
     * @param total Total count
     * @return Formatted progress string
     */
    fun formatProgress(current: Int, total: Int): String {
        return "Word $current of $total"
    }
    
    /**
     * Capitalize first letter of each word
     * @param text The text to capitalize
     * @return Capitalized text
     */
    fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
    
    /**
     * Remove extra whitespace and normalize text
     * @param text The text to normalize
     * @return Normalized text
     */
    fun normalizeText(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }
    
    /**
     * Check if text contains Chinese characters
     * @param text The text to check
     * @return True if contains Chinese characters
     */
    fun containsChinese(text: String): Boolean {
        return text.any { char ->
            char.code in 0x4E00..0x9FFF || // CJK Unified Ideographs
            char.code in 0x3400..0x4DBF || // CJK Extension A
            char.code in 0xF900..0xFAFF    // CJK Compatibility Ideographs
        }
    }
    
    /**
     * Get safe substring without index out of bounds
     * @param text The source text
     * @param startIndex Start index
     * @param length Length of substring
     * @return Safe substring
     */
    fun safeSubstring(text: String, startIndex: Int, length: Int): String {
        val start = maxOf(0, startIndex)
        val end = minOf(text.length, start + length)
        return if (start < text.length) text.substring(start, end) else ""
    }
}
