package com.example.noteshare.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════
// NoteShare Color Palette — Warm & Romantic
// ═══════════════════════════════════════════

// Primary — Soft Coral Rose
val Primary = Color(0xFFE8677D)
val PrimaryLight = Color(0xFFF2929F)
val PrimaryDark = Color(0xFFCC4E63)
val PrimaryContainer = Color(0xFFFFE0E6)
val OnPrimaryContainer = Color(0xFF3F0018)

// Secondary — Soft Lavender
val Secondary = Color(0xFF9B7FC9)
val SecondaryLight = Color(0xFFBFA5E0)
val SecondaryDark = Color(0xFF7A5DAF)
val SecondaryContainer = Color(0xFFEDE3FF)
val OnSecondaryContainer = Color(0xFF1E0A3C)

// Tertiary — Warm Gold
val Tertiary = Color(0xFFE8A94D)
val TertiaryLight = Color(0xFFFFCB7A)
val TertiaryDark = Color(0xFFC48C30)
val TertiaryContainer = Color(0xFFFFEDD3)
val OnTertiaryContainer = Color(0xFF2C1800)

// Background & Surface — Light Mode
val BackgroundLight = Color(0xFFFFF9F5)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF7F0EC)
val OnBackgroundLight = Color(0xFF1C1B1F)
val OnSurfaceLight = Color(0xFF1C1B1F)
val OnSurfaceVariantLight = Color(0xFF4A4458)
val OutlineLight = Color(0xFF7C7585)

// Background & Surface — Dark Mode
val BackgroundDark = Color(0xFF1A1025)
val SurfaceDark = Color(0xFF231830)
val SurfaceVariantDark = Color(0xFF2E2240)
val OnBackgroundDark = Color(0xFFE8E0EC)
val OnSurfaceDark = Color(0xFFE8E0EC)
val OnSurfaceVariantDark = Color(0xFFCAC2D3)
val OutlineDark = Color(0xFF948C9D)

// Status Colors
val Success = Color(0xFF4CAF79)
val SuccessContainer = Color(0xFFDAF5E4)
val Warning = Color(0xFFE8A94D)
val WarningContainer = Color(0xFFFFF3E0)
val Error = Color(0xFFD94A5A)
val ErrorContainer = Color(0xFFFFDADD)
val Info = Color(0xFF5B8DEF)
val InfoContainer = Color(0xFFDCE8FF)

// Mood Colors (gradient from sad to very happy)
val MoodVerySad = Color(0xFF7E8FA6)
val MoodSad = Color(0xFFA0B0C8)
val MoodNeutral = Color(0xFFE8C87A)
val MoodHappy = Color(0xFFF2929F)
val MoodVeryHappy = Color(0xFFE8677D)

// Tag Colors
val TagTravel = Color(0xFF5B8DEF)
val TagFood = Color(0xFFE8A94D)
val TagSchool = Color(0xFF9B7FC9)
val TagWork = Color(0xFF4CAF79)
val TagFamily = Color(0xFFE8677D)
val TagOther = Color(0xFF7C7585)

// Gradient helpers
val GradientPrimary = listOf(Primary, PrimaryLight)
val GradientSecondary = listOf(Secondary, SecondaryLight)
val GradientWarm = listOf(Color(0xFFE8677D), Color(0xFFE8A94D))
val GradientSunset = listOf(Color(0xFFE8677D), Color(0xFF9B7FC9))
val GradientDarkOverlay = listOf(Color(0x00000000), Color(0x99000000))
