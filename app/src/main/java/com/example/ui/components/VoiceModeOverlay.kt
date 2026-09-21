package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VoiceState
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.AccentBlueGlow
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.PlusPillText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun VoiceModeOverlay(
    voiceState: VoiceState,
    transcript: String,
    onClose: () -> Unit,
    onSendVoicePrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChatOledBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("voice_mode_overlay")
    ) {
        // Top Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ChatSurfaceElevated)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .clickable { onClose() }
                    .testTag("voice_close_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close voice mode",
                    tint = TextPrimaryWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "حالت صوتی هوشمند",
                color = TextSecondaryGray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        // Center Pulsing Voice Orb
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(if (voiceState != VoiceState.IDLE) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentBlueGlow,
                                AccentActionBlue,
                                Color(0xFF0F172A)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Sound Orb",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            val statusText = when (voiceState) {
                VoiceState.LISTENING -> "در حال شنیدن صدای شما..."
                VoiceState.THINKING -> "در حال پردازش پاسخ MindGPT..."
                VoiceState.SPEAKING -> "در حال پخش پاسخ صوتی..."
                VoiceState.IDLE -> "آماده برای گفتگو"
            }

            Text(
                text = statusText,
                color = PlusPillText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (transcript.isNotEmpty()) {
                Text(
                    text = transcript,
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    lineHeight = 20.sp
                )
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color(0xFF450A0A) else ChatSurfaceElevated)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .clickable { isMuted = !isMuted }
                    .testTag("voice_mute_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Toggle Mute",
                    tint = TextPrimaryWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Quick Persian Voice Prompts
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(ChatSurfaceElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .clickable {
                        onSendVoicePrompt("سلام! امروز چه خبر؟")
                    }
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("voice_quick_prompt"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "سلام! امروز چه خبر؟ 💬",
                    color = TextPrimaryWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
