package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val thinkingContent: String? = null,
    val isWebSearch: Boolean = false
)
