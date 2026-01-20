package com.secondbrain.lite

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.secondbrain.lite.database.AppDatabase
import com.secondbrain.lite.database.Thought
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ViewThoughtActivity : AppCompatActivity() {

    private lateinit var categoryDot: View
    private lateinit var categoryTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var thoughtTextView: TextView
    private lateinit var editButton: MaterialButton
    
    private lateinit var database: AppDatabase
    private var thoughtId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_thought)
        
        // Initialize
        database = AppDatabase.getDatabase(this)
        thoughtId = intent.getLongExtra("thought_id", -1)
        
        if (thoughtId == -1L) {
            finish()
            return
        }
        
        // Initialize views
        categoryDot = findViewById(R.id.categoryDot)
        categoryTextView = findViewById(R.id.categoryTextView)
        dateTextView = findViewById(R.id.dateTextView)
        titleTextView = findViewById(R.id.titleTextView)
        thoughtTextView = findViewById(R.id.thoughtTextView)
        editButton = findViewById(R.id.editButton)
        
        // Load thought
        loadThought()
        
        // Edit button
        editButton.setOnClickListener {
            val intent = Intent(this, AddThoughtActivity::class.java)
            intent.putExtra("thought_id", thoughtId)
            startActivity(intent)
        }
    }

    private fun loadThought() {
        lifecycleScope.launch {
            val thought = database.thoughtDao().getThoughtById(thoughtId)
            thought?.let {
                displayThought(it)
            } ?: run {
                finish()
            }
        }
    }

    private fun displayThought(thought: Thought) {
        // Set category
        categoryTextView.text = thought.category
        val categoryColor = when (thought.category) {
            "Decision" -> R.color.category_decision
            "Lesson" -> R.color.category_lesson
            "Reflection" -> R.color.category_reflection
            else -> R.color.primary_soft_blue
        }
        (categoryDot.background as? GradientDrawable)?.setColor(
            ContextCompat.getColor(this, categoryColor)
        )
        
        // Set date
        dateTextView.text = formatDate(thought.date)
        
        // Set title
        if (thought.title.isNotEmpty()) {
            titleTextView.visibility = View.VISIBLE
            titleTextView.text = thought.title
        } else {
            titleTextView.visibility = View.GONE
        }
        
        // Set main text
        thoughtTextView.text = thought.text
    }

    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        calendar.timeInMillis = timestamp
        val thoughtDate = calendar.timeInMillis

        return when {
            isSameDay(today, thoughtDate) -> getString(R.string.today)
            isSameDay(today - 86400000, thoughtDate) -> getString(R.string.yesterday)
            else -> {
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
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

    override fun onResume() {
        super.onResume()
        // Reload thought in case it was edited
        loadThought()
    }
}
