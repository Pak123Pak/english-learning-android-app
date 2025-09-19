package com.example.englishlearningandroidapp.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object for managing Retrofit instances
 */
object RetrofitInstance {
    
    // Cambridge Dictionary API base URL (using the API from github.com/chenelias/cambridge-dictionary-api)
    private const val CAMBRIDGE_BASE_URL = "https://dictionary-api.eliaschen.dev/"
    
    // Free Dictionary API base URL (fallback)
    private const val FREE_DICTIONARY_BASE_URL = "https://api.dictionaryapi.dev/"
    
    // Timeout configuration
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    /**
     * Create OkHttp client with interceptors
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(ErrorInterceptor())
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * Create Gson instance with custom configuration
     */
    private fun createGson() = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()
    
    /**
     * Cambridge Dictionary API Retrofit instance
     */
    private val cambridgeRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CAMBRIDGE_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    /**
     * Free Dictionary API Retrofit instance (fallback)
     */
    private val freeDictionaryRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FREE_DICTIONARY_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    /**
     * Get Cambridge Dictionary API service
     */
    val cambridgeApiService: DictionaryApiService by lazy {
        // Use real Cambridge Dictionary API service
        cambridgeRetrofit.create(DictionaryApiService::class.java)
    }
    
    /**
     * Get Free Dictionary API service (fallback)
     */
    val freeDictionaryApiService: FreeDictionaryApiService by lazy {
        freeDictionaryRetrofit.create(FreeDictionaryApiService::class.java)
    }
    
    /**
     * Update base URL for Cambridge API (if needed for testing)
     */
    fun updateCambridgeBaseUrl(newBaseUrl: String): DictionaryApiService {
        val newRetrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
        
        return newRetrofit.create(DictionaryApiService::class.java)
    }
}

/**
 * Interceptor for adding common headers
 */
class HeaderInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        
        val newRequest = originalRequest.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "EnglishLearningApp/1.0")
            // Add API key header if required
            // .addHeader("X-API-Key", "your_api_key_here")
            .build()
        
        return chain.proceed(newRequest)
    }
}

/**
 * Interceptor for handling common errors
 */
class ErrorInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Log response for debugging
        if (!response.isSuccessful) {
            val responseBody = response.peekBody(2048).string()
            println("API Error - Code: ${response.code}, Message: ${response.message}, Body: $responseBody")
        }
        
        return response
    }
}

/**
 * Network state utility
 */
object NetworkUtils {
    
    /**
     * Check if response is successful
     */
    fun <T> isSuccessful(response: retrofit2.Response<T>): Boolean {
        return response.isSuccessful && response.body() != null
    }
    
    /**
     * Get error message from response
     */
    fun <T> getErrorMessage(response: retrofit2.Response<T>): String {
        return when (response.code()) {
            400 -> "Bad request. Please check your input."
            401 -> "Unauthorized access."
            403 -> "Access forbidden."
            404 -> "Word not found."
            429 -> "Too many requests. Please try again later."
            500 -> "Server error. Please try again later."
            else -> "Network error occurred. Please check your connection."
        }
    }
    
    /**
     * Create API exception from response
     */
    fun <T> createApiException(response: retrofit2.Response<T>): ApiException {
        val message = getErrorMessage(response)
        return when (response.code()) {
            in 400..499 -> ApiException.ServerException(response.code(), message)
            in 500..599 -> ApiException.ServerException(response.code(), message)
            else -> ApiException.UnknownException(message)
        }
    }
    
    /**
     * Create network exception
     */
    fun createNetworkException(throwable: Throwable): ApiException {
        return when (throwable) {
            is java.net.UnknownHostException -> 
                ApiException.NetworkException("No internet connection", throwable)
            is java.net.SocketTimeoutException -> 
                ApiException.TimeoutException("Request timeout")
            is java.net.ConnectException -> 
                ApiException.NetworkException("Cannot connect to server", throwable)
            else -> 
                ApiException.UnknownException("Network error: ${throwable.message}", throwable)
        }
    }
}
