package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ChatSurfaceElevated
import com.example.ui.theme.TextPrimaryWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyStateView(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "شمارش روزهای بین دو تاریخ 📅",
        "یک بازی برای گروهم انتخاب کن 🎲",
        "یک دستور غذا انتخاب کنید 📖"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // MindGPT Logo in center
        Image(
            painter = painterResource(id = R.drawable.ic_mindgpt_logo),
            contentDescription = "MindGPT Logo",
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 16.dp)
        )

        // Title: "به چی فکر می‌کنی؟"
        Text(
            text = "به چی فکر می‌کنی؟",
            color = TextPrimaryWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Suggestion items matching Screenshot 11
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ChatSurfaceElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("suggestion_chip"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = suggestion,
                        color = TextPrimaryWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
