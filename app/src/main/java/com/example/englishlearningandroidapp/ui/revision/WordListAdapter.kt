package com.example.englishlearningandroidapp.ui.revision

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishlearningandroidapp.R
import com.example.englishlearningandroidapp.data.database.Word

/**
 * Adapter for displaying word list in the navigation dialog
 */
class WordListAdapter(
    private val words: List<Word>,
    private val onWordClick: (Int) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordNumberTextView: TextView = itemView.findViewById(R.id.wordNumberTextView)
        val chineseTranslationTextView: TextView = itemView.findViewById(R.id.chineseTranslationTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onWordClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word_list, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.wordNumberTextView.text = "${position + 1}."
        holder.chineseTranslationTextView.text = word.chineseTranslation
    }

    override fun getItemCount(): Int = words.size
}

