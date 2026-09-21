package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.GeminiClient
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceCard
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.ChatSurfaceHighlight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

@Composable
fun ApiKeyDialog(
    currentKey: String,
    selectedModel: String,
    onDismiss: () -> Unit,
    onSave: (key: String, model: String) -> Unit,
    onClearKey: () -> Unit
) {
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf(currentKey) }
    var currentSelectedModel by remember { mutableStateOf(selectedModel) }
    val scrollState = rememberScrollState()

    val isConfigured = keyInput.trim().isNotEmpty() && !keyInput.startsWith("MY_")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(ChatSurfaceElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .verticalScroll(scrollState)
                .testTag("api_key_dialog")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentActionBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Logo",
                            tint = AccentActionBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google AI Studio",
                            color = TextPrimaryWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تنظیمات مستقیم هوش مصنوعی گوگل",
                            color = TextSecondaryGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ChatSurfaceCard)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isConfigured) Color(0xFF064E3B).copy(alpha = 0.6f) else Color(0xFF78350F).copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (isConfigured) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isConfigured) Color(0xFF34D399) else Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isConfigured) "متصل به Google AI Studio (فعال)" else "کلید اختصاصی وارد نشده است",
                        color = TextPrimaryWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isConfigured)
                            "درخواست‌ها مستقیماً با مدل انتخابی به سرورهای گوگل ارسال می‌شوند."
                        else
                            "برای فعال‌سازی کامل و پاسخ‌های واقعی هوش مصنوعی، کلید API خود را وارد کنید.",
                        color = TextSecondaryGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // API Key Input Section
            Text(
                text = "کلید اختصاصی Gemini API (Google AI Studio):",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChatSurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (keyInput.isEmpty()) {
                    Text(
                        text = "AIzaSy...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        textStyle = TextStyle(
                            color = TextPrimaryWhite,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(AccentActionBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("api_key_input")
                    )

                    // Paste Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChatSurfaceElevated)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                if (!clipText.isNullOrBlank()) {
                                    keyInput = clipText
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = AccentActionBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "جاگذاری",
                                color = AccentActionBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "دریافت رایگان کلید از: aistudio.google.com/apikey",
                color = AccentActionBlue,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Model Selection
            Text(
                text = "انتخاب مدل هوش مصنوعی (Google Models):",
                color = TextPrimaryWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            ModelOptionItem(
                title = "Gemini 2.5 Flash (پیشنهادی گوگل)",
                subtitle = "سریع‌ترین مدل چندوجهی، پاسخ‌های فوری و بهینه برای متن و تصویر",
                modelKey = GeminiClient.MODEL_FLASH_25,
                isSelected = currentSelectedModel == GeminiClient.MODEL_FLASH_25,
                onSelect = { currentSelectedModel = GeminiClient.MODEL_FLASH_25 }
            )

            Spacer(modifier = Modifier.height(6.dp))

            ModelOptionItem(
                title = "Gemini 2.5 Pro (تفکر و استدلال)",
                subtitle = "قدرتمندترین مدل تحلیلی برای تفکر عمیق و حل مسائل پیچیده",
                modelKey = GeminiClient.MODEL_PRO_25,
                isSelected = currentSelectedModel == GeminiClient.MODEL_PRO_25,
                onSelect = { currentSelectedModel = GeminiClient.MODEL_PRO_25 }
            )

            Spacer(modifier = Modifier.height(6.dp))

            ModelOptionItem(
                title = "Gemini Flash Latest",
                subtitle = "جدیدترین انتشار مدل‌های فلش با بالاترین نرخ پاسخگویی",
                modelKey = GeminiClient.MODEL_FLASH_LATEST,
                isSelected = currentSelectedModel == GeminiClient.MODEL_FLASH_LATEST,
                onSelect = { currentSelectedModel = GeminiClient.MODEL_FLASH_LATEST }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save and Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isConfigured) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChatSurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                keyInput = ""
                                onClearKey()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "حذف کلید",
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentActionBlue)
                        .clickable {
                            onSave(keyInput, currentSelectedModel)
                        }
                        .padding(vertical = 12.dp)
                        .testTag("save_api_key_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ذخیره و اتصال",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelOptionItem(
    title: String,
    subtitle: String,
    modelKey: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ChatSurfaceHighlight else ChatSurfaceCard)
            .border(
                1.dp,
                if (isSelected) AccentActionBlue else BorderSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) AccentActionBlue else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimaryWhite,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextSecondaryGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
