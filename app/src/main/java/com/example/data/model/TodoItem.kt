package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateString: String, // format "YYYY-MM-DD" matching chosen calendar days
    val isCompleted: Boolean = false,
    val alertTime: String? = null, // e.g. "09:00" or null
    val alertOffsetMinutes: Int = 0, // By default exact time (0 min before)
    val isAlertEnabled: Boolean = false
)
