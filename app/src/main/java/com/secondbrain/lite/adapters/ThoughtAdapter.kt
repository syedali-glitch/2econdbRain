package com.secondbrain.lite.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.secondbrain.lite.R
import com.secondbrain.lite.database.Thought
import java.text.SimpleDateFormat
import java.util.*

class ThoughtAdapter(
    private val onThoughtClick: (Thought) -> Unit
) : ListAdapter<Thought, ThoughtAdapter.ThoughtViewHolder>(ThoughtDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThoughtViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_thought_card, parent, false)
        return ThoughtViewHolder(view, onThoughtClick)
    }

    override fun onBindViewHolder(holder: ThoughtViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ThoughtViewHolder(
        itemView: View,
        private val onThoughtClick: (Thought) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val thoughtTextView: TextView = itemView.findViewById(R.id.thoughtTextView)
        private val pinnedIndicator: TextView = itemView.findViewById(R.id.pinnedIndicator)

        fun bind(thought: Thought) {
            // Set category with custom icon
            categoryTextView.text = thought.category
            val categoryIconRes = when (thought.category) {
                "Decision" -> R.drawable.ic_category_decision
                "Lesson" -> R.drawable.ic_category_lesson
                "Reflection" -> R.drawable.ic_category_reflection
                else -> R.drawable.ic_category_decision
            }
            categoryIcon.setImageResource(categoryIconRes)

            // Set date
            dateTextView.text = formatDate(thought.date)

            // Set title (show only if not empty)
            if (thought.title.isNotEmpty()) {
                titleTextView.visibility = View.VISIBLE
                titleTextView.text = thought.title
            } else {
                titleTextView.visibility = View.GONE
            }

            // Set main thought text
            thoughtTextView.text = thought.text

            // Show pinned indicator
            pinnedIndicator.visibility = if (thought.isPinned) View.VISIBLE else View.GONE

            // Click listener
            itemView.setOnClickListener {
                onThoughtClick(thought)
            }
        }

        private fun formatDate(timestamp: Long): String {
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            
            calendar.timeInMillis = timestamp
            val thoughtDate = calendar.timeInMillis

            return when {
                isSameDay(today, thoughtDate) -> itemView.context.getString(R.string.today)
                isSameDay(today - 86400000, thoughtDate) -> itemView.context.getString(R.string.yesterday)
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }

        private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
            val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
            val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
    }

    class ThoughtDiffCallback : DiffUtil.ItemCallback<Thought>() {
        override fun areItemsTheSame(oldItem: Thought, newItem: Thought): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Thought, newItem: Thought): Boolean {
            return oldItem == newItem
        }
    }
}
