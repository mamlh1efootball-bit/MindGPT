package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.MessageEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageOptionsPopup(
    message: MessageEntity,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onSelectText: () -> Unit,
    onEditMessage: () -> Unit,
    onShare: () -> Unit,
    onBranch: () -> Unit,
    onRetry: () -> Unit,
    onSearchWeb: () -> Unit,
    onReadAloud: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = "Today, ${timeFormatter.format(Date(message.timestamp))}"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 310.dp)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1E1E22))
                .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                .padding(vertical = 10.dp)
                .testTag("message_options_popup")
        ) {
            // Timestamp header (Screenshot 1: "Today, 10:48 AM")
            Text(
                text = timeStr,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (message.role == "user") {
                // Screenshot 1 Menu for User message:
                // 1. Copy
                PopupItem(
                    title = "Copy",
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        onCopy()
                        onDismiss()
                    },
                    testTag = "popup_copy"
                )

                // 2. Select text
                PopupItem(
                    title = "Select text",
                    icon = Icons.Default.TextFields,
                    onClick = {
                        onSelectText()
                        onDismiss()
                    },
                    testTag = "popup_select_text"
                )

                // 3. Edit message
                PopupItem(
                    title = "Edit message",
                    icon = Icons.Default.Edit,
                    onClick = {
                        onEditMessage()
                        onDismiss()
                    },
                    testTag = "popup_edit_message"
                )

                // 4. Share prompt
                PopupItem(
                    title = "Share prompt",
                    icon = Icons.Default.Share,
                    onClick = {
                        onShare()
                        onDismiss()
                    },
                    testTag = "popup_share_prompt"
                )
            } else {
                // Assistant message options
                PopupItem(
                    title = "Copy response",
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        onCopy()
                        onDismiss()
                    },
                    testTag = "popup_copy_assistant"
                )

                PopupItem(
                    title = "Select text",
                    icon = Icons.Default.TextFields,
                    onClick = {
                        onSelectText()
                        onDismiss()
                    },
                    testTag = "popup_select_text_assistant"
                )

                PopupItem(
                    title = "Read aloud",
                    icon = Icons.Default.VolumeUp,
                    onClick = {
                        onReadAloud()
                        onDismiss()
                    },
                    testTag = "popup_read_aloud"
                )

                PopupItem(
                    title = "Branch in new chat",
                    icon = Icons.Default.AccountTree,
                    onClick = {
                        onBranch()
                        onDismiss()
                    },
                    testTag = "popup_branch"
                )

                PopupItem(
                    title = "Retry",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        onRetry()
                        onDismiss()
                    },
                    testTag = "popup_retry"
                )

                PopupItem(
                    title = "Search the web",
                    icon = Icons.Default.Language,
                    onClick = {
                        onSearchWeb()
                        onDismiss()
                    },
                    testTag = "popup_search_web"
                )
            }
        }
    }
}

@Composable
private fun PopupItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 11.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = TextPrimaryWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextSecondaryGray,
            modifier = Modifier.size(19.dp)
        )
    }
}
