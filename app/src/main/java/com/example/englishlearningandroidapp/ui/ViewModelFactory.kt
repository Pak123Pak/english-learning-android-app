package com.example.englishlearningandroidapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.englishlearningandroidapp.data.api.RetrofitInstance
import com.example.englishlearningandroidapp.data.database.WordDatabase
import com.example.englishlearningandroidapp.data.repository.ApiRepository
import com.example.englishlearningandroidapp.data.repository.WordRepository
import com.example.englishlearningandroidapp.ui.dictionary.DictionaryViewModel
import com.example.englishlearningandroidapp.ui.revision.RevisionViewModel

/**
 * Factory for creating ViewModels with proper dependencies
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    private val database by lazy { WordDatabase.getDatabase(context) }
    private val wordRepository by lazy { WordRepository(database.wordDao()) }
    private val apiRepository by lazy { 
        ApiRepository(
            RetrofitInstance.cambridgeApiService,
            RetrofitInstance.freeDictionaryApiService
        ) 
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DictionaryViewModel::class.java) -> {
                DictionaryViewModel(wordRepository, apiRepository) as T
            }
            modelClass.isAssignableFrom(RevisionViewModel::class.java) -> {
                RevisionViewModel(wordRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * Extension function to get ViewModelFactory instance
 */
fun Context.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(this.applicationContext)
}
