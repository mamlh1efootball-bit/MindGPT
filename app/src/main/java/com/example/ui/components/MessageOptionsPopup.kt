package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    onBranch: () -> Unit,
    onRetry: () -> Unit,
    onSearchWeb: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = "Today, ${timeFormatter.format(Date(message.timestamp))}"

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(min = 260.dp, max = 320.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(ChatSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                .padding(vertical = 12.dp)
                .testTag("message_options_popup")
        ) {
            // Timestamp header (Screenshot 6: "Today, 9:28 AM")
            Text(
                text = timeStr,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // "Branch in new chat"
            PopupItem(
                title = "Branch in new chat",
                icon = Icons.Default.AccountTree,
                onClick = onBranch,
                testTag = "popup_branch"
            )

            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // "Retry"
            PopupItem(
                title = "Retry",
                icon = Icons.Default.Refresh,
                onClick = onRetry,
                testTag = "popup_retry"
            )

            // "Search the web"
            PopupItem(
                title = "Search the web",
                icon = Icons.Default.Language,
                onClick = onSearchWeb,
                testTag = "popup_search_web"
            )
        }
    }
}

@Composable
private fun PopupItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = TextPrimaryWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextSecondaryGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
