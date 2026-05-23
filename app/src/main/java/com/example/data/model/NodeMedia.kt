package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "node_media")
data class NodeMedia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: Int,
    val type: MediaType,
    val content: String, // Text description or file path
    val timestamp: Long = System.currentTimeMillis()
)
