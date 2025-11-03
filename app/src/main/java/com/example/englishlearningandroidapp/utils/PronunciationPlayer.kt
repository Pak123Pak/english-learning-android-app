package com.example.englishlearningandroidapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException

/**
 * Utility class for playing pronunciation audio
 */
object PronunciationPlayer {
    private const val TAG = "PronunciationPlayer"
    private const val PREPARE_TIMEOUT_MS = 10000L // 10 seconds timeout
    private var mediaPlayer: MediaPlayer? = null
    private var timeoutHandler: Handler? = null
    private var timeoutRunnable: Runnable? = null
    
    /**
     * Play pronunciation audio from a URL
     * @param context Application context for MediaPlayer
     * @param url The audio URL to play
     * @param onComplete Callback when playback completes (optional)
     * @param onError Callback when an error occurs (optional)
     */
    fun playPronunciation(
        context: Context,
        url: String,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            // Release any existing player and timeout
            release()
            
            if (url.isBlank()) {
                Log.w(TAG, "Empty URL provided")
                onError?.invoke("No audio URL available")
                return
            }
            
            // Ensure URL starts with https
            val audioUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else if (url.startsWith("//")) {
                "https:$url"
            } else {
                "https://$url"
            }
            
            Log.d(TAG, "Playing pronunciation from URL: $audioUrl")
            
            // Setup timeout handler
            timeoutHandler = Handler(Looper.getMainLooper())
            timeoutRunnable = Runnable {
                Log.w(TAG, "Audio preparation timeout - taking too long to load")
                release()
                onError?.invoke("Audio loading timeout. Please try again.")
            }
            timeoutHandler?.postDelayed(timeoutRunnable!!, PREPARE_TIMEOUT_MS)
            
            // Create and configure media player
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                
                // Set completion listener
                setOnCompletionListener {
                    Log.d(TAG, "Playback completed")
                    onComplete?.invoke()
                    release()
                }
                
                // Set error listener
                setOnErrorListener { mp, what, extra ->
                    // Cancel timeout
                    cancelTimeout()
                    
                    val errorMsg = when (what) {
                        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "Unknown error"
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "Server died"
                        else -> "Error code $what"
                    }
                    Log.e(TAG, "MediaPlayer error: what=$what ($errorMsg), extra=$extra")
                    Log.e(TAG, "URL was: $audioUrl")
                    onError?.invoke("Failed to play audio: $errorMsg")
                    release()
                    true
                }
                
                // Set info listener for additional debugging
                setOnInfoListener { mp, what, extra ->
                    Log.d(TAG, "MediaPlayer info: what=$what, extra=$extra")
                    false
                }
                
                // Set prepared listener
                setOnPreparedListener { mp ->
                    // Cancel timeout since we're prepared
                    cancelTimeout()
                    
                    Log.d(TAG, "MediaPlayer prepared, starting playback")
                    try {
                        mp.start()
                        Log.d(TAG, "Playback started successfully")
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "Failed to start playback", e)
                        onError?.invoke("Failed to start audio playback")
                        release()
                    }
                }
                
                // Set data source with headers and prepare asynchronously
                try {
                    // Add headers to help with server compatibility and speed
                    val headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
                        "Accept" to "audio/mpeg,audio/*;q=0.9,*/*;q=0.8",
                        "Accept-Language" to "en-US,en;q=0.9",
                        "Accept-Encoding" to "identity",
                        "Range" to "bytes=0-"
                    )
                    // Convert URL string to Uri and use context-based setDataSource
                    val uri = Uri.parse(audioUrl)
                    setDataSource(context, uri, headers)
                    Log.d(TAG, "Data source set with headers, preparing async...")
                    prepareAsync()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException setting data source", e)
                    release()
                    onError?.invoke("Invalid audio URL or MediaPlayer state")
                }
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "IOException while playing pronunciation: ${e.message}", e)
            release()
            onError?.invoke("Network error: Cannot load audio")
        } catch (e: Exception) {
            Log.e(TAG, "Exception while playing pronunciation: ${e.message}", e)
            release()
            onError?.invoke("Error: ${e.message}")
        }
    }
    
    /**
     * Cancel the timeout handler
     */
    private fun cancelTimeout() {
        timeoutRunnable?.let { runnable ->
            timeoutHandler?.removeCallbacks(runnable)
        }
        timeoutHandler = null
        timeoutRunnable = null
    }
    
    /**
     * Stop and release the media player
     */
    fun release() {
        // Cancel any pending timeout
        cancelTimeout()
        
        try {
            mediaPlayer?.let { player ->
                try {
                    // Don't check isPlaying, just stop if possible
                    player.stop()
                } catch (e: IllegalStateException) {
                    // Player might already be stopped or in invalid state, this is normal
                    Log.v(TAG, "Player already in stopped state")
                }
                
                try {
                    player.reset()
                } catch (e: IllegalStateException) {
                    Log.v(TAG, "Player already reset")
                }
                
                try {
                    player.release()
                } catch (e: IllegalStateException) {
                    Log.v(TAG, "Player already released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player", e)
        } finally {
            mediaPlayer = null
        }
    }
    
    /**
     * Check if audio is currently playing
     */
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.let { player ->
                try {
                    player.isPlaying
                } catch (e: IllegalStateException) {
                    false
                }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Stop current playback without releasing resources
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    prepare()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        }
    }
}

