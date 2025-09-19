package com.example.englishlearningandroidapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupNavigationButtons()
    }
    
    private fun setupNavigationButtons() {
        val dictionaryButton = findViewById<Button>(R.id.dictionaryButton)
        val revisionButton = findViewById<Button>(R.id.revisionButton)
        
        dictionaryButton.setOnClickListener {
            val intent = Intent(this, DictionaryActivity::class.java)
            startActivity(intent)
        }
        
        revisionButton.setOnClickListener {
            val intent = Intent(this, RevisionActivity::class.java)
            startActivity(intent)
        }
    }
}