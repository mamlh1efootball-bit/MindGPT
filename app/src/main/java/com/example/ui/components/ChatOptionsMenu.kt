package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.RedDestructive
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatOptionsMenu(
    chat: ChatEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onPin: () -> Unit,
    onAddToProject: () -> Unit,
    onUploadedFiles: () -> Unit,
    onFindInChat: () -> Unit,
    onAddToHome: () -> Unit,
    onArchive: () -> Unit,
    onOpenApiKey: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChatSurfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(BorderSubtle, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: Chat Title
            Text(
                text = chat.title,
                color = TextPrimaryWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("chat_menu_title")
            )

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            MenuItem(
                icon = Icons.Default.Share,
                title = "اشتراک‌گذاری (Share)",
                onClick = onShare,
                testTag = "menu_share"
            )

            MenuItem(
                icon = Icons.Default.PushPin,
                title = if (chat.isPinned) "برداشتن سنجاق (Unpin)" else "سنجاق کردن (Pin)",
                onClick = onPin,
                testTag = "menu_pin"
            )

            MenuItem(
                icon = Icons.Default.CreateNewFolder,
                title = "افزودن به پروژه (Add to project)",
                onClick = onAddToProject,
                testTag = "menu_add_to_project"
            )

            MenuItem(
                icon = Icons.Default.AttachFile,
                title = "فایل‌های آپلود شده (Uploaded files)",
                onClick = onUploadedFiles,
                testTag = "menu_uploaded_files"
            )

            MenuItem(
                icon = Icons.Default.Search,
                title = "جستجو در گفتگو (Find in chat)",
                onClick = onFindInChat,
                testTag = "menu_find_in_chat"
            )

            MenuItem(
                icon = Icons.Default.Home,
                title = "افزودن به صفحه اصلی (Add to home)",
                onClick = onAddToHome,
                testTag = "menu_add_to_home"
            )

            MenuItem(
                icon = Icons.Default.Archive,
                title = if (chat.isArchived) "خروج از بایگانی (Unarchive)" else "بایگانی (Archive)",
                onClick = onArchive,
                testTag = "menu_archive"
            )

            MenuItem(
                icon = Icons.Default.AttachFile,
                title = "تنظیمات API (Google AI Studio)",
                onClick = onOpenApiKey,
                testTag = "menu_api_settings"
            )

            HorizontalDivider(
                color = BorderSubtle,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            MenuItem(
                icon = Icons.Default.Delete,
                title = "حذف (Delete)",
                onClick = onDelete,
                textColor = RedDestructive,
                iconTint = RedDestructive,
                testTag = "menu_delete"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = TextPrimaryWhite,
    iconTint: Color = TextSecondaryGray,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
