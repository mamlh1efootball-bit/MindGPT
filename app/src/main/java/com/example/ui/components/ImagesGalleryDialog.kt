package com.example.ui.components

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentActionBlue
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatOledBlack
import com.example.ui.theme.ChatSurfaceCard
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.PlusPillText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGray

data class ImagePromptCard(
    val title: String,
    val prompt: String,
    val category: String
)

@Composable
fun ImagesGalleryDialog(
    onDismiss: () -> Unit,
    onSelectPrompt: (String) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }

    val presetImages = remember {
        listOf(
            ImagePromptCard("بک‌گراند یوتیوب", "طراحی یک بنر و پس‌زمینه حرفه‌ای یوتیوب با تم سایبرپانک نئونی", "کانال و ویدیو"),
            ImagePromptCard("پوستر بنر اختصاصی", "پوستر تبلیغاتی مینیمال با تایپوگرافی مدرن و گرادیانت شیک", "گرافیک"),
            ImagePromptCard("طراحی لوگو جدید", "طراحی لوگوی مینیمال، انتزاعی و هوشمند برای برند هوش مصنوعی", "برندینگ"),
            ImagePromptCard("تصویرسازی دیجیتال", "نقاشی دیجیتال پرتره هنری سورئال با نورپردازی سینمایی", "هنری"),
            ImagePromptCard("طراحی رابط کاربری", "مفاهیم مدرن رابط کاربری اپلیکیشن موبایل با طراحی شیشه‌ای Glassmorphism", "UI/UX"),
            ImagePromptCard("طراحی کاور موزیک", "کاور آلبوم موسیقی با تلفیق کهکشان و مناظر رویاگونه", "موسیقی")
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatOledBlack)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("images_gallery_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Images",
                            tint = PlusPillText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "استودیو خلاق تصاویر MindGPT",
                            color = TextPrimaryWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ChatSurfaceElevated)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prompt creation input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (promptInput.isEmpty()) {
                            Text(
                                text = "توصیف تصویر مورد نظر را بنویسید...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            textStyle = TextStyle(
                                color = TextPrimaryWhite,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(AccentActionBlue),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentActionBlue)
                            .clickable {
                                if (promptInput.isNotBlank()) {
                                    onSelectPrompt("تصویرسازی حرفه‌ای: $promptInput")
                                    onDismiss()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Create Image",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "الگوها و ایده‌های تصویرسازی:",
                    color = TextSecondaryGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(presetImages) { card ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(ChatSurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .clickable {
                                    onSelectPrompt(card.prompt)
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ChatSurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PlusPillText,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = card.title,
                                color = TextPrimaryWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = card.category,
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
