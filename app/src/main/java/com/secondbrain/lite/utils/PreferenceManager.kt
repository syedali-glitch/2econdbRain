package com.secondbrain.lite.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("SecondBrainPrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_ADS_REMOVED = "ads_removed"
        private const val KEY_THOUGHT_SAVE_COUNT = "thought_save_count"
        private const val KEY_REWARDED_PINS_AVAILABLE = "rewarded_pins_available"
    }
    
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    var adsRemoved: Boolean
        get() = prefs.getBoolean(KEY_ADS_REMOVED, false)
        set(value) = prefs.edit().putBoolean(KEY_ADS_REMOVED, value).apply()
    
    var thoughtSaveCount: Int
        get() = prefs.getInt(KEY_THOUGHT_SAVE_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_THOUGHT_SAVE_COUNT, value).apply()
    
    var rewardedPinsAvailable: Int
        get() = prefs.getInt(KEY_REWARDED_PINS_AVAILABLE, 0)
        set(value) = prefs.edit().putInt(KEY_REWARDED_PINS_AVAILABLE, value).apply()
    
    fun incrementThoughtSaveCount() {
        thoughtSaveCount++
    }
}
