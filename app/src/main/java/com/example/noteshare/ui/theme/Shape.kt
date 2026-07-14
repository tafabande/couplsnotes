package com.example.noteshare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NoteShareShapes = Shapes(
    // Buttons, chips, small elements
    extraSmall = RoundedCornerShape(8.dp),
    // Text fields, small cards
    small = RoundedCornerShape(12.dp),
    // Cards, dialogs
    medium = RoundedCornerShape(16.dp),
    // Bottom sheets, large containers
    large = RoundedCornerShape(24.dp),
    // Full-screen modals
    extraLarge = RoundedCornerShape(32.dp)
)
