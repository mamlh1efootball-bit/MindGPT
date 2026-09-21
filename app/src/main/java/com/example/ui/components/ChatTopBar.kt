package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.ChatSurfaceHighlight
import com.example.ui.theme.PlusPillBg
import com.example.ui.theme.PlusPillText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite

@Composable
fun ChatTopBar(
    hasActiveChat: Boolean,
    isPlayingAudio: Boolean,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onGetPlusClick: () -> Unit,
    onStopAudioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatOledBlack)
            .statusBarsPadding()
    ) {
        // Audio Player Bar when TTS is active (Screenshots 7 & 8)
        AnimatedVisibility(
            visible = isPlayingAudio,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ChatSurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onStopAudioClick,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("audio_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close audio",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Playing sound wave",
                        tint = PlusPillText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "00:00",
                        color = TextPrimaryWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ChatSurfaceHighlight)
                        .clickable { onStopAudioClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause audio",
                        tint = TextPrimaryWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Main Navigation Top Bar (Screenshots 1, 3, 5, 11)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Button: Either Pill [ ⋮ | ✎ ] or Single chat button
            if (hasActiveChat) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOptionsClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("chat_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Chat options",
                            tint = TextPrimaryWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(BorderSubtle)
                    )

                    IconButton(
                        onClick = onNewChatClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("chat_edit_new_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New chat",
                            tint = TextPrimaryWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .clickable { onNewChatClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New chat",
                        tint = TextPrimaryWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Right side buttons: "Get Plus ✨" and Hamburger Menu Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "Get Plus ✨" pill button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PlusPillBg)
                        .clickable { onGetPlusClick() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("get_plus_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Get Plus",
                        color = PlusPillText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Plus Sparkle",
                        tint = PlusPillText,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Hamburger Circle Button (Open Drawer) - Two horizontal lines
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, CircleShape)
                        .clickable { onMenuClick() }
                        .testTag("drawer_menu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Two parallel bars icon matching ChatGPT screenshot
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(2.dp)
                                .background(TextPrimaryWhite, RoundedCornerShape(1.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(2.dp)
                                .background(TextPrimaryWhite, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}
