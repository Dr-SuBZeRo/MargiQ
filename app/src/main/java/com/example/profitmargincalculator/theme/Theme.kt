package com.example.profitmargincalculator.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.example.profitmargincalculator.data.preferences.ThemePreference

private val DarkColorScheme = darkColorScheme(
    primary = FinanceNavyPrimaryDark,
    onPrimary = FinanceNavyOnPrimaryDark,
    primaryContainer = FinancePrimaryContainerDark,
    onPrimaryContainer = FinanceOnPrimaryContainerDark,
    secondary = FinanceSecondaryDark,
    onSecondary = FinanceOnSecondaryDark,
    secondaryContainer = FinanceSecondaryContainerDark,
    onSecondaryContainer = FinanceOnSecondaryContainerDark,
    tertiary = FinanceTertiaryDark,
    onTertiary = FinanceOnTertiaryDark,
    tertiaryContainer = FinanceTertiaryContainerDark,
    onTertiaryContainer = FinanceOnTertiaryContainerDark,
    background = FinanceBackgroundDark,
    onBackground = FinanceOnBackgroundDark,
    surface = FinanceSurfaceDark,
    onSurface = FinanceOnSurfaceDark,
    surfaceVariant = FinanceSurfaceVariantDark,
    onSurfaceVariant = FinanceOnSurfaceVariantDark,
    outline = FinanceOutlineDark,
    outlineVariant = FinanceOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = FinanceNavyPrimaryLight,
    onPrimary = FinanceNavyOnPrimaryLight,
    primaryContainer = FinancePrimaryContainerLight,
    onPrimaryContainer = FinanceOnPrimaryContainerLight,
    secondary = FinanceSecondaryLight,
    onSecondary = FinanceOnSecondaryLight,
    secondaryContainer = FinanceSecondaryContainerLight,
    onSecondaryContainer = FinanceOnSecondaryContainerLight,
    tertiary = FinanceTertiaryLight,
    onTertiary = FinanceOnTertiaryLight,
    tertiaryContainer = FinanceTertiaryContainerLight,
    onTertiaryContainer = FinanceOnTertiaryContainerLight,
    background = FinanceBackgroundLight,
    onBackground = FinanceOnBackgroundLight,
    surface = FinanceSurfaceLight,
    onSurface = FinanceOnSurfaceLight,
    surfaceVariant = FinanceSurfaceVariantLight,
    onSurfaceVariant = FinanceOnSurfaceVariantLight,
    outline = FinanceOutlineLight,
    outlineVariant = FinanceOutlineVariantLight
)

object FinanceTheme {
    val colors: FinancialColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinancialColors.current
}

@Composable
fun ProfitMarginCalculatorTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        ThemePreference.SYSTEM -> systemInDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val financialColors = if (isDark) DarkFinancialColors else LightFinancialColors

    CompositionLocalProvider(LocalFinancialColors provides financialColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
