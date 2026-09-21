package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun EditMessageDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onSaveAndSend: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(initialText) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatOledBlack)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .testTag("edit_message_screen")
        ) {
            // Top Bar matching Screenshot 2: [X]  Edit message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel edit",
                        tint = TextPrimaryWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Edit message",
                    color = TextPrimaryWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Warning Notice card matching Screenshot 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E22))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextSecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Editing this message will restart the conversation from here.",
                    color = TextSecondaryGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            // Text Editing Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    textStyle = TextStyle(
                        color = TextPrimaryWhite,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(AccentActionBlue),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("edit_message_input")
                )
            }

            // Bottom Action Bar with Send Button (Screenshot 2: Blue circular arrow up button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (editedText.isNotBlank()) AccentActionBlue else ChatSurfaceElevated)
                        .clickable(enabled = editedText.isNotBlank()) {
                            onSaveAndSend(editedText.trim())
                        }
                        .testTag("edit_message_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send edited message",
                        tint = if (editedText.isNotBlank()) Color.White else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
