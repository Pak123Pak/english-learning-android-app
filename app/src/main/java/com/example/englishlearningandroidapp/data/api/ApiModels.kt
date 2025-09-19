package com.example.englishlearningandroidapp.data.api

import com.google.gson.annotations.SerializedName

/**
 * Response model for Cambridge Dictionary API
 */
data class WordDefinitionResponse(
    @SerializedName("word")
    val word: String,
    
    @SerializedName("pos")
    val pos: List<String>? = null,
    
    @SerializedName("phonetic")
    val phonetic: String? = null,
    
    @SerializedName("phonetics")
    val phonetics: List<Phonetic>? = null,
    
    @SerializedName("pronunciation")
    val pronunciation: List<Pronunciation>? = null,
    
    @SerializedName("verbs")
    val verbs: List<VerbForm>? = null,
    
    @SerializedName("definitions")
    val definitions: List<Definition>,
    
    @SerializedName("definition")
    val definition: List<CambridgeDefinition>? = null,
    
    @SerializedName("origin")
    val origin: String? = null
) {
    /**
     * Get the main definitions from Cambridge API format or unified format
     */
    fun getMainDefinitions(): List<Definition> {
        // If we have the unified format, use it
        if (definitions.isNotEmpty()) {
            return definitions.filter { it.translation.isNotBlank() }
        }
        
        // Otherwise, convert from Cambridge format
        return definition?.map { cambridgeDef ->
            Definition(
                translation = cambridgeDef.translation,
                partOfSpeech = cambridgeDef.pos,
                example = cambridgeDef.example?.firstOrNull()?.text,
                synonyms = null,
                antonyms = null
            )
        } ?: emptyList()
    }
    
    /**
     * Check if response has audio pronunciation
     */
    fun hasAudio(): Boolean {
        return phonetics?.any { it.hasAudio() } == true ||
               pronunciation?.any { it.hasAudio() } == true
    }
    
    /**
     * Get primary phonetic (first one with audio or first one available)
     */
    fun getPrimaryPhonetic(): Phonetic? {
        return phonetics?.firstOrNull { it.hasAudio() } 
            ?: phonetics?.firstOrNull()
    }
    
    /**
     * Get primary pronunciation from Cambridge API
     */
    fun getPrimaryPronunciation(): String? {
        return pronunciation?.firstOrNull()?.pron 
            ?: phonetic
    }
}

/**
 * Definition model containing translation and example
 */
data class Definition(
    @SerializedName("translation")
    val translation: String,
    
    @SerializedName("partOfSpeech")
    val partOfSpeech: String,
    
    @SerializedName("example")
    val example: String? = null,
    
    @SerializedName("synonyms")
    val synonyms: List<String>? = null,
    
    @SerializedName("antonyms")
    val antonyms: List<String>? = null
) {
    /**
     * Check if definition has an example sentence
     */
    fun hasExample(): Boolean {
        return !example.isNullOrBlank()
    }
    
    /**
     * Create blank example sentence by replacing the word with blank
     */
    fun createBlankExample(word: String): String {
        return if (hasExample()) {
            example!!.replace(
                word, 
                "___", 
                ignoreCase = true
            ).replace(
                word.lowercase(), 
                "___", 
                ignoreCase = true
            ).replace(
                word.uppercase(), 
                "___", 
                ignoreCase = true
            ).replace(
                word.replaceFirstChar { it.uppercase() }, 
                "___", 
                ignoreCase = true
            )
        } else {
            "No example available for ___."
        }
    }
    
    /**
     * Get formatted synonyms string
     */
    fun getSynonymsString(): String? {
        return if (!synonyms.isNullOrEmpty()) {
            "Synonyms: ${synonyms.joinToString(", ")}"
        } else null
    }
    
    /**
     * Get formatted antonyms string
     */
    fun getAntonymsString(): String? {
        return if (!antonyms.isNullOrEmpty()) {
            "Antonyms: ${antonyms.joinToString(", ")}"
        } else null
    }
}

/**
 * Phonetic pronunciation model
 */
data class Phonetic(
    @SerializedName("text")
    val text: String? = null,
    
    @SerializedName("audio")
    val audio: String? = null
) {
    /**
     * Check if phonetic has audio file
     */
    fun hasAudio(): Boolean {
        return !audio.isNullOrBlank()
    }
    
    /**
     * Get full audio URL (add prefix if needed)
     */
    fun getAudioUrl(): String? {
        return if (hasAudio()) {
            if (audio!!.startsWith("http")) {
                audio
            } else {
                "https:$audio"
            }
        } else null
    }
}

/**
 * Error response model for API errors
 */
data class ApiErrorResponse(
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("status")
    val status: Int? = null
) {
    fun getErrorMessage(): String {
        return message ?: error ?: "Unknown error occurred"
    }
}

/**
 * Network result wrapper for API responses
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Exception, val message: String? = null) : NetworkResult<T>()
    data class Loading<T>(val message: String? = null) : NetworkResult<T>()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
    
    fun getDataOrNull(): T? = if (this is Success) data else null
    fun getErrorOrNull(): Exception? = if (this is Error) exception else null
}

/**
 * API exception types
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class ServerException(val code: Int, message: String) : ApiException(message)
    class ParseException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class TimeoutException(message: String) : ApiException(message)
    class UnknownException(message: String, cause: Throwable? = null) : ApiException(message, cause)
}

/**
 * Extension functions for creating Network Results
 */
object NetworkResultExtensions {
    
    fun <T> success(data: T): NetworkResult<T> = NetworkResult.Success(data)
    
    fun <T> error(exception: Exception, message: String? = null): NetworkResult<T> = 
        NetworkResult.Error(exception, message)
    
    fun <T> loading(message: String? = null): NetworkResult<T> = 
        NetworkResult.Loading(message)
    
    fun <T> networkError(message: String, cause: Throwable? = null): NetworkResult<T> = 
        NetworkResult.Error(ApiException.NetworkException(message, cause))
    
    fun <T> serverError(code: Int, message: String): NetworkResult<T> = 
        NetworkResult.Error(ApiException.ServerException(code, message))
    
    fun <T> parseError(message: String, cause: Throwable? = null): NetworkResult<T> = 
        NetworkResult.Error(ApiException.ParseException(message, cause))
}

/**
 * Cambridge Dictionary API specific models
 */
data class CambridgeDefinition(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("pos")
    val pos: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("translation")
    val translation: String,
    
    @SerializedName("example")
    val example: List<CambridgeExample>? = null
)

data class CambridgeExample(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("translation")
    val translation: String? = null
)

data class Pronunciation(
    @SerializedName("pos")
    val pos: String,
    
    @SerializedName("lang")
    val lang: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("pron")
    val pron: String
) {
    fun hasAudio(): Boolean {
        return url.isNotBlank()
    }
    
    fun getAudioUrl(): String? {
        return if (hasAudio()) {
            if (url.startsWith("http")) {
                url
            } else {
                "https:$url"
            }
        } else null
    }
}

data class VerbForm(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("text")
    val text: String
)
