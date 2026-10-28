package com.margiq

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object CalculatorDestination : NavKey

@Serializable
data object CompareDestination : NavKey

@Serializable
data object HistoryDestination : NavKey

@Serializable
data object SettingsDestination : NavKey
