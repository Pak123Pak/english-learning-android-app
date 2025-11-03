package com.example.englishlearningandroidapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.englishlearningandroidapp.data.database.Word
import com.example.englishlearningandroidapp.databinding.ActivityRevisionBinding
import com.example.englishlearningandroidapp.ui.revision.AnswerResult
import com.example.englishlearningandroidapp.ui.revision.RevisionViewModel
import com.example.englishlearningandroidapp.ui.getViewModelFactory
import com.example.englishlearningandroidapp.utils.hideKeyboard
import com.example.englishlearningandroidapp.utils.PronunciationPlayer

class RevisionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRevisionBinding
    private lateinit var viewModel: RevisionViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityRevisionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupToolbar()
        setupViewModel()
        setupStageSpinner()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.revision_button)
    }
    
    private fun setupViewModel() {
        val factory = getViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[RevisionViewModel::class.java]
    }
    
    private fun setupStageSpinner() {
        val stageNames = viewModel.getStageNames()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.stageSpinner.adapter = adapter
        binding.stageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setCurrentStage(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun setupClickListeners() {
        // Submit answer button
        binding.submitButton.setOnClickListener {
            submitAnswer()
        }
        
        // Answer input field - submit on enter
        binding.answerEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitAnswer()
                true
            } else {
                false
            }
        }
        
        // Add new words button (navigate back to dictionary)
        binding.addWordsButton.setOnClickListener {
            val intent = Intent(this, DictionaryActivity::class.java)
            intent.putExtra("SOURCE_ACTIVITY", "RevisionActivity")
            startActivity(intent)
        }
        
        // Continue button click
        binding.continueButton.setOnClickListener {
            viewModel.continueToNextWord()
        }
        
        // Back button click
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // Delete word button click
        binding.deleteWordButton.setOnClickListener {
            showDeleteWordConfirmationDialog()
        }
        
        // Hint button click
        binding.hintButton.setOnClickListener {
            viewModel.revealNextLetter()
        }
    }
    
    private fun submitAnswer() {
        val answer = binding.answerEditText.text.toString()
        // Hide keyboard when submitting
        binding.answerEditText.hideKeyboard()
        viewModel.submitAnswer(answer)
    }
    
    private fun observeViewModel() {
        // Observe current word
        viewModel.currentWord.observe(this) { word ->
            if (word != null) {
                showWord(word)
            } else {
                showEmptyState()
            }
        }
        
        // Observe words in current stage
        viewModel.wordsInCurrentStage.observe(this) { words ->
            if (words.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
            }
        }
        
        // Observe progress text
        viewModel.progressText.observe(this) { progressText ->
            binding.progressTextView.text = progressText
            binding.progressTextView.visibility = if (progressText.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        // Observe answer result
        viewModel.answerResult.observe(this) { result ->
            when (result) {
                is AnswerResult.Idle -> {
                    hideAnswerFeedback()
                    hideContinueButton()
                }
                is AnswerResult.Correct -> {
                    showAnswerFeedback(getString(R.string.correct_answer), true)
                    clearAnswerInput()
                    showContinueButton()
                    disableSubmit()
                }
                is AnswerResult.Incorrect -> {
                    val message = getString(R.string.incorrect_answer, result.correctAnswer)
                    showAnswerFeedback(message, false)
                    clearAnswerInput()
                    showContinueButton()
                    disableSubmit()
                }
                is AnswerResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.submitButton.isEnabled = !isLoading
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
        
        // Observe success messages
        viewModel.successMessage.observe(this) { successMessage ->
            if (successMessage != null) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }
        
        // Observe show feedback state
        viewModel.showFeedback.observe(this) { showFeedback ->
            binding.answerFeedbackTextView.visibility = if (showFeedback) View.VISIBLE else View.GONE
        }
        
        // Observe displayed example sentence (with hints)
        viewModel.displayedExampleSentence.observe(this) { displayedSentence ->
            if (displayedSentence != null) {
                binding.exampleSentenceTextView.text = displayedSentence
            }
        }
        
        // Observe hint button enabled state
        viewModel.isHintButtonEnabled.observe(this) { isEnabled ->
            binding.hintButton.isEnabled = isEnabled
        }
    }
    
    private fun showWord(word: Word) {
        binding.wordCard.visibility = View.VISIBLE
        binding.answerInputLayout.visibility = View.VISIBLE
        binding.hintButton.visibility = View.VISIBLE
        binding.submitButton.visibility = View.VISIBLE
        binding.deleteWordButton.visibility = View.VISIBLE
        binding.submitButton.isEnabled = true
        
        binding.chineseTranslationTextView.text = word.chineseTranslation
        binding.partOfSpeechTextView.text = word.partOfSpeech
        // Note: exampleSentenceTextView will be updated by the displayedExampleSentence observer
        
        // Display pronunciation buttons
        displayPronunciationButtons(word)
        
        // Clear previous answer and feedback
        binding.answerEditText.text?.clear()
        binding.answerEditText.requestFocus()
        hideAnswerFeedback()
        hideContinueButton()
    }
    
    private fun showEmptyState() {
        binding.wordCard.visibility = View.GONE
        binding.answerInputLayout.visibility = View.GONE
        binding.hintButton.visibility = View.GONE
        binding.submitButton.visibility = View.GONE
        binding.deleteWordButton.visibility = View.GONE
        binding.progressTextView.visibility = View.GONE
        
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.emptyStateTextView.text = getString(R.string.empty_stage_message)
    }
    
    private fun hideEmptyState() {
        binding.emptyStateLayout.visibility = View.GONE
    }
    
    private fun showAnswerFeedback(message: String, isCorrect: Boolean) {
        binding.answerFeedbackTextView.text = message
        binding.answerFeedbackTextView.visibility = View.VISIBLE
        
        // Set background color based on correctness
        val colorResId = if (isCorrect) {
            android.R.color.holo_green_light
        } else {
            android.R.color.holo_red_light
        }
        binding.answerFeedbackTextView.setBackgroundColor(getColor(colorResId))
    }
    
    private fun hideAnswerFeedback() {
        binding.answerFeedbackTextView.visibility = View.GONE
    }
    
    private fun clearAnswerInput() {
        binding.answerEditText.text?.clear()
    }
    
    private fun showContinueButton() {
        binding.continueButton.visibility = View.VISIBLE
    }
    
    private fun hideContinueButton() {
        binding.continueButton.visibility = View.GONE
    }
    
    private fun disableSubmit() {
        binding.submitButton.isEnabled = false
        // Hide submit and hint buttons after answer submission
        binding.submitButton.visibility = View.GONE
        binding.hintButton.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning from dictionary
        viewModel.refreshCurrentStage()
    }
    
    private fun showDeleteWordConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_word_button))
            .setMessage(getString(R.string.delete_word_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteCurrentWord()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    /**
     * Display pronunciation buttons for the current word
     */
    private fun displayPronunciationButtons(word: Word) {
        // Clear existing buttons
        binding.pronunciationButtonsContainer.removeAllViews()
        
        // Debug logging
        android.util.Log.d("RevisionActivity", "=== DISPLAYING PRONUNCIATIONS ===")
        android.util.Log.d("RevisionActivity", "Word: ${word.englishWord}")
        android.util.Log.d("RevisionActivity", "Part of Speech: ${word.partOfSpeech}")
        android.util.Log.d("RevisionActivity", "Pronunciation Data: ${word.pronunciationData ?: "null"}")
        android.util.Log.d("RevisionActivity", "Has Pronunciation: ${word.hasPronunciation()}")
        
        // Get pronunciations from word
        val pronunciations = word.getPronunciations()
        android.util.Log.d("RevisionActivity", "Parsed Pronunciations Count: ${pronunciations.size}")
        
        if (pronunciations.isEmpty()) {
            android.util.Log.d("RevisionActivity", "No pronunciations found, hiding container")
            binding.pronunciationButtonsContainer.visibility = View.GONE
            return
        }
        
        // Group by language
        val groupedByLang = pronunciations.groupBy { it.lang.lowercase() }
        android.util.Log.d("RevisionActivity", "Languages: ${groupedByLang.keys}")
        
        // Create button for each language
        groupedByLang.forEach { (lang, pronList) ->
            val pron = pronList.firstOrNull() ?: return@forEach
            
            android.util.Log.d("RevisionActivity", "Creating button for $lang: ${pron.pron} (${pron.url})")
            
            val button = Button(this).apply {
                text = "${lang.uppercase()} ${pron.pron}"
                textSize = 11f
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, dpToPx(6), 0)
                }
                setOnClickListener {
                    playPronunciation(pron.url)
                }
            }
            
            binding.pronunciationButtonsContainer.addView(button)
        }
        
        binding.pronunciationButtonsContainer.visibility = View.VISIBLE
        android.util.Log.d("RevisionActivity", "Pronunciation buttons displayed: ${binding.pronunciationButtonsContainer.childCount}")
        android.util.Log.d("RevisionActivity", "=== END DISPLAYING PRONUNCIATIONS ===")
    }
    
    /**
     * Play pronunciation audio from URL
     */
    private fun playPronunciation(url: String) {
        if (url.isBlank()) {
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
     * Convert dp to pixels
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Release media player when activity is destroyed
        PronunciationPlayer.release()
    }
}
