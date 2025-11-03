package com.example.englishlearningandroidapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.englishlearningandroidapp.data.api.Pronunciation
import com.example.englishlearningandroidapp.databinding.ActivityDictionaryBinding
import com.example.englishlearningandroidapp.ui.ViewModelFactory
import com.example.englishlearningandroidapp.ui.dictionary.DefinitionsAdapter
import com.example.englishlearningandroidapp.ui.dictionary.DictionaryViewModel
import com.example.englishlearningandroidapp.ui.dictionary.SaveWordState
import com.example.englishlearningandroidapp.ui.getViewModelFactory
import com.example.englishlearningandroidapp.utils.hideKeyboard
import com.example.englishlearningandroidapp.utils.PronunciationPlayer

class DictionaryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDictionaryBinding
    private lateinit var viewModel: DictionaryViewModel
    private lateinit var definitionsAdapter: DefinitionsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityDictionaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.dictionary_button)
    }
    
    private fun setupViewModel() {
        val factory = getViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[DictionaryViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        definitionsAdapter = DefinitionsAdapter { definition ->
            viewModel.selectDefinition(definition)
        }
        
        binding.definitionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DictionaryActivity)
            adapter = definitionsAdapter
        }
    }
    
    private fun setupClickListeners() {
        // Search button click
        binding.searchButton.setOnClickListener {
            performSearch()
        }
        
        // Search on IME action
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
        
        // Save button click
        binding.saveButton.setOnClickListener {
            viewModel.saveSelectedWord()
        }
        
        // Retry button click
        binding.retryButton.setOnClickListener {
            viewModel.retrySearch()
        }
        
        // Back button click
        binding.backButton.setOnClickListener {
            handleBackNavigation()
        }
    }
    
    private fun performSearch() {
        val query = binding.searchEditText.text.toString()
        viewModel.searchWord(query)
        // Hide keyboard after search
        binding.searchEditText.hideKeyboard()
        binding.searchEditText.clearFocus()
    }
    
    private fun observeViewModel() {
        // Observe pronunciation data
        viewModel.pronunciationData.observe(this) { pronunciationMap ->
            if (pronunciationMap.isNotEmpty()) {
                displayPronunciations(pronunciationMap)
            } else {
                hidePronunciations()
            }
        }
        
        // Observe search results
        viewModel.searchResults.observe(this) { definitions ->
            if (definitions.isNotEmpty()) {
                definitionsAdapter.submitList(definitions)
                showDefinitions()
            } else {
                hideDefinitions()
            }
        }
        
        // Observe selected definition
        viewModel.selectedDefinition.observe(this) { definition ->
            binding.saveButton.isEnabled = viewModel.isSaveButtonEnabled()
        }
        
        // Observe actual word (base form from API)
        viewModel.actualWord.observe(this) { actualWord ->
            binding.saveButton.isEnabled = viewModel.isSaveButtonEnabled()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.searchButton.isEnabled = !isLoading
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                showError(errorMessage)
            } else {
                hideError()
            }
        }
        
        // Observe search validation errors
        viewModel.searchValidationError.observe(this) { validationError ->
            if (validationError != null) {
                binding.searchInputLayout.error = validationError
            } else {
                binding.searchInputLayout.error = null
            }
        }
        
        // Observe save word state
        viewModel.saveWordState.observe(this) { saveState ->
            when (saveState) {
                is SaveWordState.Idle -> {
                    // Reset UI state
                }
                is SaveWordState.Saving -> {
                    binding.saveButton.isEnabled = false
                    binding.saveButton.text = "Saving..."
                }
                is SaveWordState.Success -> {
                    binding.saveButton.text = getString(R.string.save_button)
                    Toast.makeText(this, getString(R.string.word_saved_success), Toast.LENGTH_SHORT).show()
                    clearSearchAndResults()
                }
                is SaveWordState.Error -> {
                    binding.saveButton.isEnabled = true
                    binding.saveButton.text = getString(R.string.save_button)
                    Toast.makeText(this, saveState.message, Toast.LENGTH_LONG).show()
                    viewModel.clearSaveWordState()
                }
            }
        }
    }
    
    private fun showDefinitions() {
        binding.definitionsRecyclerView.visibility = View.VISIBLE
        binding.instructionsTextView.visibility = View.GONE
    }
    
    private fun hideDefinitions() {
        binding.definitionsRecyclerView.visibility = View.GONE
        binding.instructionsTextView.visibility = View.VISIBLE
    }
    
    private fun showError(errorMessage: String) {
        binding.errorMessageTextView.text = errorMessage
        binding.errorMessageTextView.visibility = View.VISIBLE
        binding.retryButton.visibility = View.VISIBLE
        hideDefinitions()
    }
    
    private fun hideError() {
        binding.errorMessageTextView.visibility = View.GONE
        binding.retryButton.visibility = View.GONE
    }
    
    private fun clearSearchAndResults() {
        binding.searchEditText.text?.clear()
        definitionsAdapter.submitList(emptyList())
        definitionsAdapter.clearSelection()
        hideDefinitions()
        hideError()
        hidePronunciations()
        binding.saveButton.isEnabled = false
    }
    
    /**
     * Display pronunciations grouped by part of speech
     */
    private fun displayPronunciations(pronunciationMap: Map<String, List<Pronunciation>>) {
        // Clear existing pronunciation views
        binding.pronunciationContainer.removeAllViews()
        
        // Re-add title
        val titleTextView = TextView(this).apply {
            text = "Pronunciation"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(getColor(com.google.android.material.R.color.design_default_color_primary))
            setPadding(0, 0, 0, dpToPx(8))
        }
        binding.pronunciationContainer.addView(titleTextView)
        
        // Group pronunciations by part of speech
        pronunciationMap.forEach { (pos, pronunciations) ->
            // Create a row for this part of speech
            val posRow = createPronunciationRow(pos, pronunciations)
            binding.pronunciationContainer.addView(posRow)
        }
        
        // Show the pronunciation card
        binding.pronunciationCard.visibility = View.VISIBLE
    }
    
    /**
     * Create a pronunciation row for a specific part of speech
     */
    private fun createPronunciationRow(partOfSpeech: String, pronunciations: List<Pronunciation>): LinearLayout {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
            gravity = Gravity.CENTER_VERTICAL
        }
        
        // Part of speech label
        val posLabel = TextView(this).apply {
            text = partOfSpeech
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.ITALIC)
            setTextColor(getColor(com.google.android.material.R.color.design_default_color_secondary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, dpToPx(12), 0)
            }
            minWidth = dpToPx(60)
        }
        rowLayout.addView(posLabel)
        
        // Group pronunciations by language (uk, us, etc.)
        val groupedByLang = pronunciations.groupBy { it.lang.lowercase() }
        
        // Create buttons for each language
        groupedByLang.forEach { (lang, pronList) ->
            val pron = pronList.firstOrNull() ?: return@forEach
            
            // Language button
            val langButton = Button(this).apply {
                text = "${lang.uppercase()} ${pron.pron}"
                textSize = 12f
                setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, dpToPx(8), 0)
                }
                setOnClickListener {
                    playPronunciation(pron)
                }
            }
            rowLayout.addView(langButton)
        }
        
        return rowLayout
    }
    
    /**
     * Play pronunciation audio
     */
    private fun playPronunciation(pronunciation: Pronunciation) {
        val url = pronunciation.getAudioUrl()
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show()
            return
        }
        
        PronunciationPlayer.playPronunciation(
            context = this,
            url = url,
            onComplete = {
                // Audio playback completed
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    /**
     * Hide pronunciation section
     */
    private fun hidePronunciations() {
        binding.pronunciationCard.visibility = View.GONE
        binding.pronunciationContainer.removeAllViews()
    }
    
    /**
     * Convert dp to pixels
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        handleBackNavigation()
        return true
    }
    
    private fun handleBackNavigation() {
        val sourceActivity = intent.getStringExtra("SOURCE_ACTIVITY")
        if (sourceActivity == "RevisionActivity") {
            // If came from RevisionActivity, go back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } else {
            // Otherwise, use normal back navigation
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Release media player when activity is destroyed
        PronunciationPlayer.release()
    }
}
