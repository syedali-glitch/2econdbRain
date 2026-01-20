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
    private lateinit var decisionRadio: RadioButton
    private lateinit var lessonRadio: RadioButton
    private lateinit var reflectionRadio: RadioButton
    private lateinit var saveButton: MaterialButton
    
    private lateinit var database: AppDatabase
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adManager: AdManager
    
    private var editingThoughtId: Long? = null

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
        decisionRadio = findViewById(R.id.decisionRadio)
        lessonRadio = findViewById(R.id.lessonRadio)
        reflectionRadio = findViewById(R.id.reflectionRadio)
        saveButton = findViewById(R.id.saveButton)
        
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

    private fun loadThoughtForEditing(thoughtId: Long) {
        lifecycleScope.launch {
            val thought = database.thoughtDao().getThoughtById(thoughtId)
            thought?.let {
                titleEditText.setText(it.title)
                thoughtEditText.setText(it.text)
                
                when (it.category) {
                    "Decision" -> decisionRadio.isChecked = true
                    "Lesson" -> lessonRadio.isChecked = true
                    "Reflection" -> reflectionRadio.isChecked = true
                }
            }
        }
    }

    private fun saveThought() {
        val title = titleEditText.text?.toString()?.trim() ?: ""
        val text = thoughtEditText.text?.toString()?.trim() ?: ""
        
        // Validate
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.thought_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get category
        val category = when {
            decisionRadio.isChecked -> "Decision"
            lessonRadio.isChecked -> "Lesson"
            reflectionRadio.isChecked -> "Reflection"
            else -> "Decision"
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
