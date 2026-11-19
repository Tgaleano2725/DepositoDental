package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SemanticColors(
    val success: Color = EmeraldSuccess,
    val successContainer: Color = EmeraldContainer,
    val warning: Color = AmberWarning,
    val warningContainer: Color = AmberContainer,
    val danger: Color = CoralError,
    val dangerContainer: Color = CoralContainer,
    val cardElevated: Color = SlateSurfaceElevated,
    val cardHighest: Color = SlateSurfaceHighest,
    val subtleBorder: Color = SlateOutlineSubtle
)

val LocalSemanticColors = staticCompositionLocalOf { SemanticColors() }

private val DepositoDentalDarkColorScheme = darkColorScheme(
    primary = DentalTurquoise,
    onPrimary = ObsidianBackground,
    primaryContainer = DentalTurquoiseContainer,
    onPrimaryContainer = DentalOnTurquoiseContainer,
    secondary = DentalTurquoiseBright,
    onSecondary = ObsidianBackground,
    secondaryContainer = SlateSurfaceHighest,
    onSecondaryContainer = SnowText,
    tertiary = EmeraldSuccess,
    onTertiary = ObsidianBackground,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = SnowText,
    background = ObsidianBackground,
    onBackground = SnowText,
    surface = SlateSurface,
    onSurface = SnowText,
    surfaceVariant = SlateSurfaceElevated,
    onSurfaceVariant = MistText,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = SlateSurface,
    surfaceContainer = SlateSurfaceElevated,
    surfaceContainerHigh = SlateSurfaceHighest,
    outline = SlateOutline,
    outlineVariant = SlateOutlineSubtle,
    error = CoralError,
    onError = SnowText,
    errorContainer = CoralContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

val DepositoDentalShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DepositoDentalTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSemanticColors provides SemanticColors()) {
        MaterialTheme(
            colorScheme = DepositoDentalDarkColorScheme,
            typography = AppTypography,
            shapes = DepositoDentalShapes,
            content = content
        )
    }
}
