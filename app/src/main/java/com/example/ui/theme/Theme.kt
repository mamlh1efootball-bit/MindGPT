package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentActionBlue,
    secondary = AccentBlueGlow,
    tertiary = PlusPillText,
    background = ChatOledBlack,
    surface = ChatSurface,
    surfaceVariant = ChatSurfaceElevated,
    onPrimary = TextPrimaryWhite,
    onSecondary = TextPrimaryWhite,
    onTertiary = TextPrimaryWhite,
    onBackground = TextPrimaryWhite,
    onSurface = TextPrimaryWhite,
    onSurfaceVariant = TextSecondaryGray,
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    CompositionLocalProvider(
      LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = VazirFontFamily)
    ) {
      content()
    }
  }
}
