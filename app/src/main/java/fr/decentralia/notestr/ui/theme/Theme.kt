package fr.decentralia.notestr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

@Composable
fun NotestrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val nostrPurple = Color(0xFF8549D3)
    val scheme = if (darkTheme) darkColorScheme(
        primary = nostrPurple, onPrimary = Color.White,
        secondary = Color(0xFFB993EB), onSecondary = Color(0xFF21152D),
        tertiary = Color(0xFFD6B7F5),
        background = Color(0xFF17141D), onBackground = Color(0xFFF1ECF7),
        surface = Color(0xFF241F2E), onSurface = Color(0xFFF1ECF7),
        surfaceVariant = Color(0xFF302A39), onSurfaceVariant = Color(0xFFD1C6DE),
        outline = Color(0xFF6C5A7D)
    ) else lightColorScheme(
        primary = nostrPurple, onPrimary = Color.White,
        secondary = Color(0xFF6535A5), onSecondary = Color.White,
        tertiary = Color(0xFF8549D3),
        background = Color(0xFFEEE9F2), onBackground = Color(0xFF211A28),
        surface = Color(0xFFF8F5FA), onSurface = Color(0xFF211A28),
        surfaceVariant = Color(0xFFE8DFF0), onSurfaceVariant = Color(0xFF4E4359),
        outline = Color(0xFFC9BDD6)
    )
    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}

