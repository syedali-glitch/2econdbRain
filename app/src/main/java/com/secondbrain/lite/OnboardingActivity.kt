package com.secondbrain.lite

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    
    private val onboardingPages = listOf(
        OnboardingPage(
            R.string.onboarding_title_1,
            R.string.onboarding_desc_1,
            R.color.category_decision
        ),
        OnboardingPage(
            R.string.onboarding_title_2,
            R.string.onboarding_desc_2,
            R.color.category_lesson
        ),
        OnboardingPage(
            R.string.onboarding_title_3,
            R.string.onboarding_desc_3,
            R.color.category_reflection
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = OnboardingAdapter()
        
        // Disable user swipe (optional - user can swipe or use buttons)
        // viewPager.isUserInputEnabled = false
    }

    private fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    inner class OnboardingAdapter : RecyclerView.Adapter<OnboardingViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.onboarding_page, parent, false)
            return OnboardingViewHolder(view)
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(onboardingPages[position], position)
        }

        override fun getItemCount(): Int = onboardingPages.size
    }

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val iconView: View = itemView.findViewById(R.id.iconView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val indicatorLayout: LinearLayout = itemView.findViewById(R.id.indicatorLayout)
        private val skipButton: MaterialButton = itemView.findViewById(R.id.skipButton)
        private val nextButton: MaterialButton = itemView.findViewById(R.id.nextButton)

        fun bind(page: OnboardingPage, position: Int) {
            // Set icon color
            (iconView.background as? GradientDrawable)?.setColor(
                ContextCompat.getColor(itemView.context, page.iconColor)
            )
            
            // Set text
            titleTextView.setText(page.title)
            descriptionTextView.setText(page.description)
            
            // Setup indicators
            setupIndicators(position)
            
            // Setup buttons
            skipButton.setOnClickListener {
                finishOnboarding()
            }
            
            nextButton.text = if (position == onboardingPages.size - 1) {
                getString(R.string.get_started)
            } else {
                getString(R.string.next)
            }
            
            nextButton.setOnClickListener {
                if (position < onboardingPages.size - 1) {
                    viewPager.currentItem = position + 1
                } else {
                    finishOnboarding()
                }
            }
        }

        private fun setupIndicators(currentPosition: Int) {
            indicatorLayout.removeAllViews()
            
            for (i in 0 until onboardingPages.size) {
                val indicator = View(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        if (i == currentPosition) 24 else 12,
                        12
                    ).also { params ->
                        params.setMargins(8, 0, 8, 0)
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f
                        setColor(
                            ContextCompat.getColor(
                                itemView.context,
                                if (i == currentPosition) R.color.primary_soft_blue else R.color.text_secondary
                            )
                        )
                    }
                }
                indicatorLayout.addView(indicator)
            }
        }
    }

    data class OnboardingPage(
        val title: Int,
        val description: Int,
        val iconColor: Int
    )
}
