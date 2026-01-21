package com.secondbrain.lite

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.secondbrain.lite.database.AppDatabase
import com.secondbrain.lite.database.Thought
import com.secondbrain.lite.utils.AdManager
import com.secondbrain.lite.utils.PreferenceManager
import kotlinx.coroutines.launch

class AddThoughtActivity : AppCompatActivity() {

    private lateinit var headerTextView: TextView
    private lateinit var titleEditText: TextInputEditText
    private lateinit var thoughtEditText: TextInputEditText
    private lateinit var categoryAutoComplete: android.widget.AutoCompleteTextView
    private lateinit var saveButton: MaterialButton
    
    private lateinit var database: AppDatabase
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adManager: AdManager
    
    private var editingThoughtId: Long? = null
    
    // Categories and their colors
    private val categories = listOf("Decision", "Lesson", "Reflection")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_thought)
        
        // Initialize
        database = AppDatabase.getDatabase(this)
        preferenceManager = PreferenceManager(this)
        adManager = AdManager(this)
        
        // Initialize views
        headerTextView = findViewById(R.id.headerTextView)
        titleEditText = findViewById(R.id.titleEditText)
        thoughtEditText = findViewById(R.id.thoughtEditText)
        categoryAutoComplete = findViewById(R.id.categoryAutoComplete)
        saveButton = findViewById(R.id.saveButton)
        
        // Setup dropdown
        setupCategoryDropdown()
        
        // Check if editing existing thought
        editingThoughtId = intent.getLongExtra("thought_id", -1).takeIf { it != -1L }
        if (editingThoughtId != null) {
            headerTextView.text = getString(R.string.edit_thought_title)
            loadThoughtForEditing(editingThoughtId!!)
        }
        
        // Save button click
        saveButton.setOnClickListener {
            saveThought()
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = object : android.widget.ArrayAdapter<String>(this, R.layout.item_category_dropdown, categories) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return createItemView(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                return createItemView(position, convertView, parent)
            }

            private fun createItemView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = convertView ?: android.view.LayoutInflater.from(context).inflate(R.layout.item_category_dropdown, parent, false)
                
                val item = getItem(position) ?: return view
                val categoryName = view.findViewById<TextView>(R.id.categoryName)
                val categoryDot = view.findViewById<android.widget.ImageView>(R.id.categoryDot)
                
                categoryName.text = item
                
                // Set dot color
                val colorRes = when (item) {
                    "Decision" -> R.color.category_decision
                    "Lesson" -> R.color.category_lesson
                    "Reflection" -> R.color.category_reflection
                    else -> R.color.category_decision
                }
                categoryDot.imageTintList = androidx.core.content.ContextCompat.getColorStateList(context, colorRes)
                
                return view
            }
        }
        
        categoryAutoComplete.setAdapter(adapter)
        
        // Default selection logic handled by setting text and updating indicator
        categoryAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categories[position]
            updateCategoryIndicator(selectedCategory)
        }
        
        // Initial Indicator
        updateCategoryIndicator("Decision")
    }

    private fun updateCategoryIndicator(category: String) {
        val colorRes = when (category) {
            "Decision" -> R.color.category_decision
            "Lesson" -> R.color.category_lesson
            "Reflection" -> R.color.category_reflection
            else -> R.color.category_decision
        }
        
        val color = androidx.core.content.ContextCompat.getColor(this, colorRes)
        
        // Add a colored dot drawable to the left of the text in the AutoCompleteTextView
        val dotDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.circle_shape)?.mutate()
        dotDrawable?.setTint(color)
        dotDrawable?.setBounds(0, 0, 32, 32) // Size of the dot
        
        categoryAutoComplete.setCompoundDrawablesRelative(dotDrawable, null, null, null)
        categoryAutoComplete.compoundDrawablePadding = 16
    }

    private fun loadThoughtForEditing(thoughtId: Long) {
        lifecycleScope.launch {
            val thought = database.thoughtDao().getThoughtById(thoughtId)
            thought?.let {
                titleEditText.setText(it.title)
                thoughtEditText.setText(it.text)
                
                // Set category
                categoryAutoComplete.setText(it.category, false)
                updateCategoryIndicator(it.category)
            }
        }
    }

    private fun saveThought() {
        val title = titleEditText.text?.toString()?.trim() ?: ""
        val text = thoughtEditText.text?.toString()?.trim() ?: ""
        val category = categoryAutoComplete.text.toString()
        
        // Validate
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.thought_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save thought
        lifecycleScope.launch {
            val thought = if (editingThoughtId != null) {
                // Update existing thought
                val existing = database.thoughtDao().getThoughtById(editingThoughtId!!)
                existing?.copy(
                    title = title,
                    text = text,
                    category = category
                ) ?: return@launch
            } else {
                // Create new thought
                Thought(
                    title = title,
                    text = text,
                    category = category,
                    date = System.currentTimeMillis()
                )
            }
            
            database.thoughtDao().insert(thought)
            
            // Haptic feedback
            performHapticFeedback()
            
            // Show toast
            Toast.makeText(this@AddThoughtActivity, R.string.thought_saved, Toast.LENGTH_SHORT).show()
            
            // If adding new thought (not editing), increment save count and check for interstitial
            if (editingThoughtId == null) {
                preferenceManager.incrementThoughtSaveCount()
                
                if (!preferenceManager.adsRemoved && preferenceManager.thoughtSaveCount % 5 == 0) {
                    // Show interstitial ad after 5 saves
                    adManager.showInterstitialAd(this@AddThoughtActivity) {
                        finish()
                    }
                } else {
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    private fun performHapticFeedback() {
        @Suppress("DEPRECATION")
        saveButton.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
    }
}
