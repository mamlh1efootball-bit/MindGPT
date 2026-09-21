package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
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
import com.example.ui.theme.BorderGlassLuminous
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.PlusPillText
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun VoiceModeOverlay(
    voiceState: VoiceState,
    transcript: String,
    onClose: () -> Unit,
    onStartSpeechRecognition: () -> Unit,
    onSendVoicePrompt: (String) -> Unit,
    onEditInChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var recognizedText by remember(transcript) { mutableStateOf(transcript) }

    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
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
        // Top Bar
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
                text = "گفتگوی صوتی MindGPT",
                color = TextPrimaryWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing Orb
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) pulseScale else 1f)
                    .shadow(30.dp, CircleShape, spotColor = AccentBlueGlow)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentBlueGlow,
                                AccentActionBlue,
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(2.dp, BorderGlassLuminous, CircleShape)
                    .clickable { onStartSpeechRecognition() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Voice Orb",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            val statusText = when {
                voiceState == VoiceState.LISTENING -> "در حال شنیدن صدای شما... صحبت کنید"
                voiceState == VoiceState.THINKING -> "در حال پردازش پاسخ در سرور هوش مصنوعی..."
                voiceState == VoiceState.SPEAKING -> "در حال پخش پاسخ صوتی..."
                recognizedText.isNotBlank() -> "متن ویس آماده ارسال است"
                else -> "روی دکمه میکروفون بزنید و صحبت کنید"
            }

            Text(
                text = statusText,
                color = PlusPillText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transcribed Text Display (Frosted Glass Card)
            AnimatedVisibility(
                visible = recognizedText.isNotBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xD9252834),
                                    Color(0xBF1C1E26)
                                )
                            )
                        )
                        .border(1.dp, BorderGlassLuminous, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "متن تبدیل‌شده از صدای شما:",
                        color = TextSecondaryGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "« $recognizedText »",
                        color = TextPrimaryWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: [ارسال پیام به MindGPT] and [ویرایش در چت]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Edit in chat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ChatSurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable {
                                    onEditInChat(recognizedText)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TextSecondaryGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ویرایش متن",
                                    color = TextPrimaryWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Send button with ArrowUp
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentActionBlue)
                                .clickable {
                                    onSendVoicePrompt(recognizedText)
                                }
                                .padding(vertical = 12.dp)
                                .testTag("voice_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ارسال پیام",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Controls: Microphone Trigger
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Re-record Microphone Button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (voiceState == VoiceState.LISTENING) Color(0xFFEF4444) else AccentActionBlue)
                    .border(2.dp, BorderGlassLuminous, CircleShape)
                    .clickable { onStartSpeechRecognition() }
                    .testTag("voice_record_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record Speech",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
