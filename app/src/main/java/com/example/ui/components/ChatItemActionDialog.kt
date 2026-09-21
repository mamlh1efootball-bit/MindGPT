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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.ChatEntity
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun ChatItemActionDialog(
    chat: ChatEntity,
    onDismiss: () -> Unit,
    onPinToggle: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1E1E22))
                .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                .padding(vertical = 12.dp)
                .testTag("chat_item_action_dialog")
        ) {
            // Chat Title Header
            Text(
                text = chat.title,
                color = TextPrimaryWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Pin / Unpin
            ChatDialogRow(
                title = if (chat.isPinned) "برداشتن سنجاق (Unpin)" else "سنجاق کردن گفتگو (Pin)",
                icon = if (chat.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                iconTint = if (chat.isPinned) AccentActionBlue else TextSecondaryGray,
                onClick = {
                    onPinToggle()
                    onDismiss()
                }
            )

            // Rename
            ChatDialogRow(
                title = "تغییر نام (Rename)",
                icon = Icons.Default.DriveFileRenameOutline,
                onClick = {
                    onRenameClick()
                    onDismiss()
                }
            )

            // Share
            ChatDialogRow(
                title = "اشتراک‌گذاری (Share)",
                icon = Icons.Default.Share,
                onClick = {
                    onShareClick()
                    onDismiss()
                }
            )

            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Delete
            ChatDialogRow(
                title = "حذف گفتگو (Delete)",
                icon = Icons.Default.Delete,
                iconTint = Color(0xFFEF4444),
                textColor = Color(0xFFEF4444),
                onClick = {
                    onDeleteClick()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun RenameChatDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(20.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1E22))
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("rename_chat_dialog")
        ) {
            Text(
                text = "تغییر نام گفتگو",
                color = TextPrimaryWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان جدید", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentActionBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimaryWhite,
                    unfocusedTextColor = TextPrimaryWhite
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = TextSecondaryGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(title.trim())
                        }
                    }
                ) {
                    Text("ذخیره", color = AccentActionBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ChatDialogRow(
    title: String,
    icon: ImageVector,
    iconTint: Color = TextSecondaryGray,
    textColor: Color = TextPrimaryWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
