package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.MessageEntity
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.IconMuted
import com.example.ui.theme.PlusPillText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import com.example.ui.theme.UserBubbleBlue

@Composable
fun MessageItem(
    message: MessageEntity,
    onMenuClick: () -> Unit,
    onPlayTts: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isThinkingExpanded by remember { mutableStateOf(false) }

    if (message.role == "user") {
        // User Message Bubble (Screenshot 1: blue pill bubble)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Attached image if present
            if (!message.imageUri.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "User uploaded image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            if (message.content.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(UserBubbleBlue)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("user_message_bubble")
                ) {
                    Text(
                        text = message.content,
                        color = TextPrimaryWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    } else {
        // Assistant Message (Screenshots 1, 5: clean text on black, action buttons row)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Web search indicator if applicable
            if (message.isWebSearch) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Web Search",
                        tint = PlusPillText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Searched the web",
                        color = PlusPillText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Thinking block if "Think harder" was used
            if (!message.thinkingContent.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { isThinkingExpanded = !isThinkingExpanded }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Thought process",
                                tint = TextSecondaryGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مراحل تفکر عمیق (Reasoning Process)",
                                color = TextSecondaryGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = if (isThinkingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle reasoning",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = isThinkingExpanded) {
                        Text(
                            text = message.thinkingContent,
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Assistant Response Text
            Text(
                text = message.content,
                color = TextPrimaryWhite,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistant_message_content")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row matching Screenshots 1, 5:
            // [3-dots] [Share] [Speaker] [Thumbs down] [Thumbs up] [Copy]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 3-dots Menu button
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("msg_action_menu")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Message options",
                        tint = IconMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, message.content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری پاسخ"))
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("msg_action_share")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = IconMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Speaker (Read aloud / TTS) button (Screenshots 1, 7, 8)
                IconButton(
                    onClick = onPlayTts,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("msg_action_speaker")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen",
                        tint = IconMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Thumbs Down button
                IconButton(
                    onClick = onToggleDislike,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("msg_action_dislike")
                ) {
                    Icon(
                        imageVector = if (message.isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = if (message.isDisliked) AccentActionBlue else IconMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Thumbs Up button
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("msg_action_like")
                ) {
                    Icon(
                        imageVector = if (message.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (message.isLiked) AccentActionBlue else IconMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("MindGPT", message.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("msg_action_copy")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy text",
                        tint = IconMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
