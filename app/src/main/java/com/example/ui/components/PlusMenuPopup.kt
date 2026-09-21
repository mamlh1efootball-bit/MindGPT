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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TravelExplore
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
    isDeepResearchEnabled: Boolean,
    onCameraClick: () -> Unit,
    onPhotosClick: () -> Unit,
    onCreateImageClick: () -> Unit,
    onDesignClick: () -> Unit,
    onToggleThink: () -> Unit,
    onToggleDeepResearch: () -> Unit,
    onStudyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(min = 210.dp, max = 240.dp)
            .shadow(20.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E1E22))
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp)
            .testTag("plus_menu_popup")
    ) {
        // Core Plugins matching user screenshot:
        // 1. ایجاد تصویر (Create image)
        PlusItem(
            title = "ایجاد تصویر",
            icon = Icons.Default.Image,
            iconTint = Color(0xFF60A5FA),
            onClick = onCreateImageClick,
            testTag = "plugin_create_image"
        )

        // 2. طراحی (Design)
        PlusItem(
            title = "طراحی",
            icon = Icons.Default.Brush,
            iconTint = Color(0xFFF472B6),
            onClick = onDesignClick,
            testTag = "plugin_design"
        )

        // 3. فکر کن (Think)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleThink() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("plugin_think"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Think",
                    tint = if (isThinkHarderEnabled) Color(0xFF38BDF8) else TextSecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "فکر کن",
                    color = if (isThinkHarderEnabled) Color(0xFF38BDF8) else TextPrimaryWhite,
                    fontSize = 14.sp,
                    fontWeight = if (isThinkHarderEnabled) FontWeight.Bold else FontWeight.Medium
                )
            }
            if (isThinkHarderEnabled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 4. پژوهش عمیق (Deep research)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleDeepResearch() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("plugin_deep_research"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = "Deep research",
                    tint = if (isDeepResearchEnabled) Color(0xFF34D399) else TextSecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "پژوهش عمیق",
                    color = if (isDeepResearchEnabled) Color(0xFF34D399) else TextPrimaryWhite,
                    fontSize = 14.sp,
                    fontWeight = if (isDeepResearchEnabled) FontWeight.Bold else FontWeight.Medium
                )
            }
            if (isDeepResearchEnabled) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 5. مطالعه (Study)
        PlusItem(
            title = "مطالعه",
            icon = Icons.Default.MenuBook,
            iconTint = Color(0xFFFBBF24),
            onClick = onStudyClick,
            testTag = "plugin_study"
        )

        HorizontalDivider(
            color = BorderSubtle,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Photo Gallery
        PlusItem(
            title = "عکس‌ها",
            icon = Icons.Default.PhotoLibrary,
            iconTint = TextSecondaryGray,
            onClick = onPhotosClick,
            testTag = "plus_photos"
        )

        // Camera
        PlusItem(
            title = "دوربین",
            icon = Icons.Default.PhotoCamera,
            iconTint = TextSecondaryGray,
            onClick = onCameraClick,
            testTag = "plus_camera"
        )
    }
}

@Composable
private fun PlusItem(
    title: String,
    icon: ImageVector,
    iconTint: Color = TextSecondaryGray,
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
            tint = iconTint,
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
