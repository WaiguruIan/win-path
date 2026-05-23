package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestone_nodes")
data class MilestoneNode(
    @PrimaryKey val id: Int, // Node 1, 2, 3...
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val unlockedAt: Long = System.currentTimeMillis()
)
