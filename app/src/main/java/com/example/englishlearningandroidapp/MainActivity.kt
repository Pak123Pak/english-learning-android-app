package com.example.englishlearningandroidapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.englishlearningandroidapp.data.repository.WordRepository
import com.example.englishlearningandroidapp.data.database.WordDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var wordRepository: WordRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupRepository()
        setupNavigationButtons()
    }
    
    private fun setupRepository() {
        val database = WordDatabase.getDatabase(this)
        wordRepository = WordRepository(database.wordDao())
    }
    
    private fun setupNavigationButtons() {
        val dictionaryButton = findViewById<Button>(R.id.dictionaryButton)
        val revisionButton = findViewById<Button>(R.id.revisionButton)
        val deleteAllButton = findViewById<Button>(R.id.deleteAllButton)
        
        dictionaryButton.setOnClickListener {
            val intent = Intent(this, DictionaryActivity::class.java)
            startActivity(intent)
        }
        
        revisionButton.setOnClickListener {
            val intent = Intent(this, RevisionActivity::class.java)
            startActivity(intent)
        }
        
        deleteAllButton.setOnClickListener {
            showDeleteAllConfirmationDialog()
        }
    }
    
    private fun showDeleteAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_all_button))
            .setMessage(getString(R.string.delete_all_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAllWords()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun deleteAllWords() {
        lifecycleScope.launch {
            val result = wordRepository.deleteAllWords()
            if (result.isSuccess) {
                Toast.makeText(this@MainActivity, getString(R.string.delete_all_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}