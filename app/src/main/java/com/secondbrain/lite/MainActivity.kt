package com.secondbrain.lite

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.secondbrain.lite.adapters.ThoughtAdapter
import com.secondbrain.lite.database.AppDatabase
import com.secondbrain.lite.database.Thought
import com.secondbrain.lite.utils.AdManager
import com.secondbrain.lite.utils.BillingManager
import com.secondbrain.lite.utils.PreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var thoughtsRecyclerView: RecyclerView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var addThoughtFab: FloatingActionButton
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var emptyStateTextView: TextView
    private lateinit var bannerAdContainer: FrameLayout
    
    // Category filter chips
    private lateinit var chipAll: Chip
    private lateinit var chipDecision: Chip
    private lateinit var chipLesson: Chip
    private lateinit var chipReflection: Chip
    
    private var currentFilter: String = "All"
    
    private lateinit var thoughtAdapter: ThoughtAdapter
    private lateinit var database: AppDatabase
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adManager: AdManager
    private lateinit var billingManager: BillingManager
    
    private var bannerAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        preferenceManager = PreferenceManager(this)
        
        // Check if first launch - show onboarding
        if (preferenceManager.isFirstLaunch) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            preferenceManager.isFirstLaunch = false
        }
        
        setContentView(R.layout.activity_main)
        
        // Initialize components
        database = AppDatabase.getDatabase(this)
        adManager = AdManager(this)
        billingManager = BillingManager(this, preferenceManager)
        
        // Initialize views
        thoughtsRecyclerView = findViewById(R.id.thoughtsRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        addThoughtFab = findViewById(R.id.addThoughtFab)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        bannerAdContainer = findViewById(R.id.bannerAdContainer)
        
        // Initialize category chips
        chipAll = findViewById(R.id.chipAll)
        chipDecision = findViewById(R.id.chipDecision)
        chipLesson = findViewById(R.id.chipLesson)
        chipReflection = findViewById(R.id.chipReflection)
        
        // Setup RecyclerView
        thoughtAdapter = ThoughtAdapter { thought ->
            showThoughtActionsBottomSheet(thought)
        }
        thoughtsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = thoughtAdapter
        }
        
        // Setup search
        searchEditText.addTextChangedListener { text ->
            if (text.isNullOrEmpty()) {
                observeAllThoughts()
            } else {
                searchThoughts(text.toString())
            }
        }
        
        // Setup category filter chips with animations
        setupCategoryFilters()
        
        // Setup FAB with animations
        addThoughtFab.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .setDuration(150)
                        .start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start()
                }
            }
            false
        }
        
        addThoughtFab.setOnClickListener {
            startActivity(Intent(this, AddThoughtActivity::class.java))
        }
        
        // Observe thoughts
        observeAllThoughts()
        
        // Setup ads if not removed
        if (!preferenceManager.adsRemoved) {
            setupBannerAd()
            adManager.loadInterstitialAd()
        }
    }

    private fun observeAllThoughts() {
        lifecycleScope.launch {
            database.thoughtDao().getAllThoughts().collectLatest { thoughts ->
                thoughtAdapter.submitList(thoughts)
                
                // Show/hide empty state with animation
                if (thoughts.isEmpty()) {
                    thoughtsRecyclerView.visibility = View.GONE
                    emptyStateContainer.apply {
                        visibility = View.VISIBLE
                        alpha = 0f
                        animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()
                    }
                } else {
                    emptyStateContainer.visibility = View.GONE
                    thoughtsRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun searchThoughts(query: String) {
        lifecycleScope.launch {
            database.thoughtDao().searchThoughts(query).collectLatest { thoughts ->
                thoughtAdapter.submitList(thoughts)
            }
        }
    }

    private fun setupCategoryFilters() {
        chipAll.setOnClickListener {
            currentFilter = "All"
            animateChipSelection(chipAll)
            observeAllThoughts()
        }
        
        chipDecision.setOnClickListener {
            currentFilter = "Decision"
            animateChipSelection(chipDecision)
            observeFilteredThoughts("Decision")
        }
        
        chipLesson.setOnClickListener {
            currentFilter = "Lesson"
            animateChipSelection(chipLesson)
            observeFilteredThoughts("Lesson")
        }
        
        chipReflection.setOnClickListener {
            currentFilter = "Reflection"
            animateChipSelection(chipReflection)
            observeFilteredThoughts("Reflection")
        }
    }
    
    private fun animateChipSelection(selectedChip: Chip) {
        // Scale animation for selected chip
        selectedChip.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction {
                selectedChip.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    private fun observeFilteredThoughts(category: String) {
        lifecycleScope.launch {
            database.thoughtDao().getThoughtsByCategory(category).collectLatest { thoughts ->
                thoughtAdapter.submitList(thoughts)
                
                // Show/hide empty state with animation
                if (thoughts.isEmpty()) {
                    thoughtsRecyclerView.visibility = android.view.View.GONE
                    emptyStateContainer.apply {
                        visibility = android.view.View.VISIBLE
                        alpha = 0f
                        animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start()
                    }
                } else {
                    emptyStateContainer.visibility = android.view.View.GONE
                    thoughtsRecyclerView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    private fun showThoughtActionsBottomSheet(thought: Thought) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_thought_actions, null)
        bottomSheetDialog.setContentView(view)
        
        val viewAction = view.findViewById<View>(R.id.viewThoughtAction)
        val pinAction = view.findViewById<View>(R.id.pinThoughtAction)
        val deleteAction = view.findViewById<View>(R.id.deleteThoughtAction)
        val pinActionText = view.findViewById<TextView>(R.id.pinActionText)
        
        // Set pin/unpin text
        pinActionText.text = if (thought.isPinned) {
            getString(R.string.action_unpin)
        } else {
            getString(R.string.action_pin)
        }
        
        viewAction.setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(this, ViewThoughtActivity::class.java)
            intent.putExtra("thought_id", thought.id)
            startActivity(intent)
        }
        
        pinAction.setOnClickListener {
            bottomSheetDialog.dismiss()
            togglePinThought(thought)
        }
        
        deleteAction.setOnClickListener {
            bottomSheetDialog.dismiss()
            showDeleteConfirmation(thought)
        }
        
        bottomSheetDialog.show()
    }

    private fun togglePinThought(thought: Thought) {
        lifecycleScope.launch {
            if (!thought.isPinned) {
                // Check pin limit
                val pinnedCount = database.thoughtDao().getPinnedCount()
                val maxPins = 5 + preferenceManager.rewardedPinsAvailable
                
                if (pinnedCount >= maxPins) {
                    // Show option to watch rewarded ad
                    if (!preferenceManager.adsRemoved && adManager.isRewardedAdLoaded()) {
                        showWatchAdForPinDialog()
                    } else {
                        Toast.makeText(this@MainActivity, R.string.pin_limit_reached, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
            }
            
            // Toggle pin
            val updatedThought = thought.copy(isPinned = !thought.isPinned)
            database.thoughtDao().update(updatedThought)
            
            // Haptic feedback
            performHapticFeedback()
            
            val message = if (updatedThought.isPinned) {
                getString(R.string.thought_pinned)
            } else {
                getString(R.string.thought_unpinned)
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWatchAdForPinDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.pin_limit_reached))
            .setMessage(getString(R.string.pin_limit_watch_ad))
            .setPositiveButton(getString(R.string.watch_ad)) { _, _ ->
                adManager.showRewardedAd(this) {
                    preferenceManager.rewardedPinsAvailable++
                    Toast.makeText(this, "You can now pin one more thought!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeleteConfirmation(thought: Thought) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                lifecycleScope.launch {
                    database.thoughtDao().delete(thought)
                    performHapticFeedback()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setupBannerAd() {
        bannerAdView = adManager.createBannerAd()
        bannerAdContainer.addView(bannerAdView)
        adManager.loadBannerAd(bannerAdView!!)
    }

    private fun performHapticFeedback() {
        @Suppress("DEPRECATION")
        addThoughtFab.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
    }

    override fun onResume() {
        super.onResume()
        bannerAdView?.resume()
        
        // Preload rewarded ad
        if (!preferenceManager.adsRemoved) {
            adManager.loadRewardedAd()
        }
    }

    override fun onPause() {
        bannerAdView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        billingManager.destroy()
        super.onDestroy()
    }
}
