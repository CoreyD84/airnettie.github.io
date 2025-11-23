package com.airnettie.mobile.tabs.theme

// 👇 All your imports go here
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Your custom theme tokens
import com.airnettie.mobile.ui.theme.Pink40
import com.airnettie.mobile.ui.theme.Pink80
import com.airnettie.mobile.ui.theme.Purple40
import com.airnettie.mobile.ui.theme.Purple80
import com.airnettie.mobile.ui.theme.PurpleGrey40
import com.airnettie.mobile.ui.theme.PurpleGrey80
import com.airnettie.mobile.ui.theme.Typography

// Semantic colors you exposed in Color.kt
import com.airnettie.mobile.ui.theme.White
import com.airnettie.mobile.ui.theme.Black
import com.airnettie.mobile.ui.theme.GrayDark
import com.airnettie.mobile.ui.theme.ErrorRed

// 👇 Then paste your DarkColorScheme, LightColorScheme, and MommaMobileTheme here
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = GrayDark,
    surface = GrayDark,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    error = ErrorRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    error = ErrorRed,
    onError = White
)

@Composable
fun MommaMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}