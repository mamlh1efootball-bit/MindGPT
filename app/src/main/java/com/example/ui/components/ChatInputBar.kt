package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceCard
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun ChatInputBar(
    inputText: String,
    attachedImageUri: Uri?,
    isGenerating: Boolean,
    isThinkHarderEnabled: Boolean,
    showPlusMenu: Boolean,
    placeholderText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit,
    onMicClick: () -> Unit,
    onPlusClick: () -> Unit,
    onRemoveImage: () -> Unit,
    onCameraClick: () -> Unit,
    onPhotosClick: () -> Unit,
    onFilesClick: () -> Unit,
    onPluginsClick: () -> Unit,
    onToggleThinkHarder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatOledBlack)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Plus Popup Menu (Screenshot 12)
        if (showPlusMenu) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                PlusMenuPopup(
                    isThinkHarderEnabled = isThinkHarderEnabled,
                    onCameraClick = onCameraClick,
                    onPhotosClick = onPhotosClick,
                    onFilesClick = onFilesClick,
                    onPluginsClick = onPluginsClick,
                    onToggleThinkHarder = onToggleThinkHarder,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Attached image preview if any
        if (attachedImageUri != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(68.dp)
            ) {
                AsyncImage(
                    model = attachedImageUri,
                    contentDescription = "Attached image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { onRemoveImage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove attached image",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // The Pill input bar (Screenshots 1, 2, 4, 11)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(ChatSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action Button: Voice Call (Phone icon) or Send (ArrowUp) or Stop
            val hasContent = inputText.trim().isNotEmpty() || attachedImageUri != null

            if (isGenerating) {
                // Stop Generation button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AccentActionBlue)
                        .clickable { /* Handled in VM */ }
                        .testTag("input_stop_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (hasContent) {
                // Send Message Button (Arrow Up)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AccentActionBlue)
                        .clickable { onSend() }
                        .testTag("input_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Phone Call Voice Mode Button (Screenshot 1, 4, 11)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AccentActionBlue)
                        .clickable { onVoiceClick() }
                        .testTag("input_voice_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call Mode",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Mic Icon Button for dictation (Screenshots 1, 2, 11)
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("input_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Dictation",
                    tint = TextSecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Text Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = placeholderText,
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    textStyle = TextStyle(
                        color = TextPrimaryWhite,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(AccentActionBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_text_input")
                )
            }

            // Right "+" Plus Button (Opens plus menu)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (showPlusMenu) ChatSurfaceCard else Color.Transparent)
                    .clickable { onPlusClick() }
                    .testTag("input_plus_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add attachment or options",
                    tint = TextPrimaryWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
