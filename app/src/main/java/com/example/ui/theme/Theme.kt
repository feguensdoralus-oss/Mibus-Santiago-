package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BoldDarkPrimary,
    onPrimary = Color(0xFF482300),
    primaryContainer = BoldDarkPrimaryContainer,
    onPrimaryContainer = BoldDarkOnPrimaryContainer,
    secondary = Color(0xFFFFB4AB),
    onSecondary = Color(0xFF690005),
    secondaryContainer = Color(0xFF93000A),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFE5BFA8),
    background = BoldDarkBackground,
    surface = BoldDarkSurface,
    surfaceVariant = BoldDarkSurfaceWarm,
    onBackground = BoldDarkTextPrimary,
    onSurface = BoldDarkTextPrimary,
    onSurfaceVariant = BoldDarkTextSecondary,
    outline = Color(0xFF55443A)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoldPrimary,
    onPrimary = Color.White,
    primaryContainer = BoldPrimaryContainer,
    onPrimaryContainer = BoldOnPrimaryContainer,
    secondary = RedPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = BoldTextSecondary,
    background = BoldBackground,
    surface = BoldSurface,
    surfaceVariant = BoldSurfaceWarm,
    onBackground = BoldTextPrimary,
    onSurface = BoldTextPrimary,
    onSurfaceVariant = BoldTextSecondary,
    outline = BoldBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

