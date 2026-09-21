package com.example.ui.components

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
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.PlusPillText
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun PlusMenuPopup(
    isThinkHarderEnabled: Boolean,
    onCameraClick: () -> Unit,
    onPhotosClick: () -> Unit,
    onFilesClick: () -> Unit,
    onPluginsClick: () -> Unit,
    onToggleThinkHarder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(min = 190.dp, max = 220.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(ChatSurfaceElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
            .padding(vertical = 8.dp)
            .testTag("plus_menu_popup")
    ) {
        PlusItem(
            title = "Camera",
            icon = Icons.Default.PhotoCamera,
            onClick = onCameraClick,
            testTag = "plus_camera"
        )

        PlusItem(
            title = "Photos",
            icon = Icons.Default.PhotoLibrary,
            onClick = onPhotosClick,
            testTag = "plus_photos"
        )

        PlusItem(
            title = "Files",
            icon = Icons.Default.Description,
            onClick = onFilesClick,
            testTag = "plus_files"
        )

        PlusItem(
            title = "Plugins",
            icon = Icons.Default.AlternateEmail,
            onClick = onPluginsClick,
            testTag = "plus_plugins"
        )

        // "Think harder" with active state indicator (Screenshot 12)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleThinkHarder() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("plus_think_harder"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Think harder",
                    tint = if (isThinkHarderEnabled) PlusPillText else TextSecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Think harder",
                    color = if (isThinkHarderEnabled) PlusPillText else TextPrimaryWhite,
                    fontSize = 14.sp,
                    fontWeight = if (isThinkHarderEnabled) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (isThinkHarderEnabled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = PlusPillText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlusItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextSecondaryGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = TextPrimaryWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
