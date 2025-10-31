package com.example.englishlearningandroidapp

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.englishlearningandroidapp.databinding.ActivityDictionaryBinding
import com.example.englishlearningandroidapp.ui.ViewModelFactory
import com.example.englishlearningandroidapp.ui.dictionary.DefinitionsAdapter
import com.example.englishlearningandroidapp.ui.dictionary.DictionaryViewModel
import com.example.englishlearningandroidapp.ui.dictionary.SaveWordState
import com.example.englishlearningandroidapp.ui.getViewModelFactory

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
            finish()
        }
    }
    
    private fun performSearch() {
        val query = binding.searchEditText.text.toString()
        viewModel.searchWord(query)
        // Hide keyboard after search
        binding.searchEditText.clearFocus()
    }
    
    private fun observeViewModel() {
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
        binding.saveButton.isEnabled = false
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
