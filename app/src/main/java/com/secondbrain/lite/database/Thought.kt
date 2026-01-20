package com.secondbrain.lite.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "thoughts")
data class Thought(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String = "",
    
    val text: String,
    
    val category: String, // "Decision", "Lesson", or "Reflection"
    
    val date: Long = System.currentTimeMillis(),
    
    val isPinned: Boolean = false
)
