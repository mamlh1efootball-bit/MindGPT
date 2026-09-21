package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "چت جدید",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val folder: String = ""
)
