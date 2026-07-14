package com.example.noteshare.data.model

import com.example.noteshare.ui.theme.*
import androidx.compose.ui.graphics.Color

/**
 * Represents a tag that can be applied to notes.
 */
data class Tag(
    val name: String,
    val emoji: String,
    val color: Color
) {
    companion object {
        val DEFAULTS = listOf(
            Tag("travel", "✈️", TagTravel),
            Tag("food", "🍕", TagFood),
            Tag("school", "📚", TagSchool),
            Tag("work", "💼", TagWork),
            Tag("family", "👨‍👩‍👧‍👦", TagFamily),
            Tag("date", "🌹", Color(0xFFE8677D)),
            Tag("gift", "🎁", Color(0xFF9B7FC9)),
            Tag("health", "💪", Color(0xFF4CAF79)),
            Tag("shopping", "🛒", Color(0xFFE8A94D)),
            Tag("thoughts", "💭", Color(0xFF7C7585)),
            Tag("recipes", "👨‍🍳", Color(0xFFE8A94D)),
            Tag("passwords", "🔒", Color(0xFF5B8DEF))
        )

        fun fromName(name: String): Tag {
            return DEFAULTS.find { it.name == name }
                ?: Tag(name, "🏷️", TagOther)
        }
    }
}
