package com.example.englishlearningandroidapp.ui.dictionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.englishlearningandroidapp.R
import com.example.englishlearningandroidapp.data.api.Definition

/**
 * Adapter for displaying word definitions in a RecyclerView with single selection
 */
class DefinitionsAdapter(
    private val onDefinitionSelected: (Definition) -> Unit
) : ListAdapter<Definition, DefinitionsAdapter.DefinitionViewHolder>(DefinitionDiffCallback()) {
    
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_definition, parent, false)
        return DefinitionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        val definition = getItem(position)
        holder.bind(definition, position == selectedPosition) { selectedDefinition ->
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            
            // Notify changes for radio button states
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition)
            }
            notifyItemChanged(selectedPosition)
            
            onDefinitionSelected(selectedDefinition)
        }
    }
    
    /**
     * Get the currently selected definition
     * @return Selected definition or null if none selected
     */
    fun getSelectedDefinition(): Definition? {
        return if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < itemCount) {
            getItem(selectedPosition)
        } else {
            null
        }
    }
    
    /**
     * Clear the current selection
     */
    fun clearSelection() {
        val previousSelectedPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition)
        }
    }
    
    /**
     * Set selection programmatically
     * @param definition The definition to select
     */
    fun setSelection(definition: Definition) {
        val position = currentList.indexOf(definition)
        if (position != -1) {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition)
            }
            notifyItemChanged(selectedPosition)
            
            onDefinitionSelected(definition)
        }
    }
    
    inner class DefinitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val radioButton: RadioButton = itemView.findViewById(R.id.definitionRadioButton)
        private val chineseTranslationTextView: TextView = itemView.findViewById(R.id.chineseTranslationTextView)
        private val partOfSpeechTextView: TextView = itemView.findViewById(R.id.partOfSpeechTextView)
        private val exampleSentenceTextView: TextView = itemView.findViewById(R.id.exampleSentenceTextView)
        private val synonymsTextView: TextView = itemView.findViewById(R.id.synonymsTextView)
        
        fun bind(
            definition: Definition, 
            isSelected: Boolean, 
            onItemSelected: (Definition) -> Unit
        ) {
            // Set radio button state
            radioButton.isChecked = isSelected
            
            // Set definition content
            chineseTranslationTextView.text = definition.translation
            partOfSpeechTextView.text = definition.partOfSpeech
            
            // Set example sentence
            if (!definition.example.isNullOrBlank()) {
                exampleSentenceTextView.text = definition.example
                exampleSentenceTextView.visibility = View.VISIBLE
            } else {
                exampleSentenceTextView.visibility = View.GONE
            }
            
            // Set synonyms if available
            val synonymsText = definition.getSynonymsString()
            if (!synonymsText.isNullOrBlank()) {
                synonymsTextView.text = synonymsText
                synonymsTextView.visibility = View.VISIBLE
            } else {
                synonymsTextView.visibility = View.GONE
            }
            
            // Set click listeners
            itemView.setOnClickListener {
                onItemSelected(definition)
            }
            
            radioButton.setOnClickListener {
                onItemSelected(definition)
            }
            
            // Add visual feedback for selection
            itemView.isSelected = isSelected
            if (isSelected) {
                itemView.elevation = 8f
            } else {
                itemView.elevation = 2f
            }
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates
     */
    class DefinitionDiffCallback : DiffUtil.ItemCallback<Definition>() {
        override fun areItemsTheSame(oldItem: Definition, newItem: Definition): Boolean {
            return oldItem.translation == newItem.translation && 
                   oldItem.partOfSpeech == newItem.partOfSpeech
        }
        
        override fun areContentsTheSame(oldItem: Definition, newItem: Definition): Boolean {
            return oldItem == newItem
        }
    }
}
