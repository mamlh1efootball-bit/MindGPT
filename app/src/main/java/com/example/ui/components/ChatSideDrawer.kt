package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatEntity
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceCard
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.ChatSurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun ChatSideDrawer(
    chats: List<ChatEntity>,
    activeChatId: String?,
    onSelectChat: (ChatEntity) -> Unit,
    onNewChatClick: () -> Unit,
    onImagesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(ChatOledBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 12.dp)
            .testTag("chat_side_drawer")
    ) {
        // Header: Search Icon + "MindGPT" (Screenshot 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPrimaryWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "MindGPT",
                color = TextPrimaryWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fixed navigation sections matching Screenshot 10:
        // Images, Library, Projects, Scheduled, Plugins
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            DrawerSectionItem(
                title = "Images",
                icon = Icons.Default.Collections,
                onClick = onImagesClick,
                testTag = "drawer_images"
            )
            DrawerSectionItem(
                title = "Library",
                icon = Icons.Default.LocalLibrary,
                onClick = {},
                testTag = "drawer_library"
            )
            DrawerSectionItem(
                title = "Projects",
                icon = Icons.Default.Folder,
                onClick = {},
                testTag = "drawer_projects"
            )
            DrawerSectionItem(
                title = "Scheduled",
                icon = Icons.Default.AccessTime,
                onClick = {},
                testTag = "drawer_scheduled"
            )
            DrawerSectionItem(
                title = "Plugins",
                icon = Icons.Default.Extension,
                onClick = {},
                testTag = "drawer_plugins"
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        HorizontalDivider(
            color = BorderSubtle,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Chat History List (Screenshot 10)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                val isSelected = chat.id == activeChatId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ChatSurfaceHighlight else Color.Transparent)
                        .clickable { onSelectChat(chat) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("drawer_chat_item_${chat.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.title,
                        color = if (isSelected) TextPrimaryWhite else TextSecondaryGray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HorizontalDivider(
            color = BorderSubtle,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Bottom User Row + Blue "Chat ✏️" Button (Screenshot 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // User Avatar & Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSettingsClick() }
                    .padding(4.dp)
                    .testTag("drawer_user_profile")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ChatSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = TextPrimaryWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondaryGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Blue "Chat ✏️" New Chat button (Screenshot 10)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentActionBlue)
                    .clickable { onNewChatClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("drawer_new_chat_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chat",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New chat",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerSectionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextSecondaryGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = TextPrimaryWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
