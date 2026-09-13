package com.sulkmqul.corvelox.ui.theme

import android.app.Activity
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

val ChicLightColorScheme = lightColorScheme(
    primary = Color(0xFF4A4E51),       // チャコールグレー
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF8C7B6B),     // トープ
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF4F1EA),    // ★ ここを真っ白ではなく、温かみのあるエクリュに変更
    onBackground = Color(0xFF2C2A29),    // 背景に合わせて文字色も少しマイルドな黒に
    surface = Color(0xFFFAF9F6),       // カードの色は背景より少しだけ明るい色（ほぼ白）にして立体感を出す
    onSurface = Color(0xFF2C2A29),
    surfaceVariant = Color(0xFFE5E2DA),
    error = Color(0xFFBA1A1A)
)

@Composable
fun CorveloxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChicLightColorScheme,
        typography = Typography,
        content = content
    )
}