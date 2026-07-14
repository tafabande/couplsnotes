package com.example.noteshare.data.model

import com.example.noteshare.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Represents a tag that can be applied to notes.
 */
data class Tag(
    val name: String,
    val icon: ImageVector,
    val color: Color
) {
    companion object {
        val DEFAULTS = listOf(
            Tag("travel", Icons.Default.Flight, TagTravel),
            Tag("food", Icons.Default.Restaurant, TagFood),
            Tag("school", Icons.Default.School, TagSchool),
            Tag("work", Icons.Default.Work, TagWork),
            Tag("family", Icons.Default.FamilyRestroom, TagFamily),
            Tag("date", Icons.Default.Favorite, Color(0xFFE8677D)),
            Tag("gift", Icons.Default.CardGiftcard, Color(0xFF9B7FC9)),
            Tag("health", Icons.Default.FitnessCenter, Color(0xFF4CAF79)),
            Tag("shopping", Icons.Default.ShoppingCart, Color(0xFFE8A94D)),
            Tag("thoughts", Icons.Default.Lightbulb, Color(0xFF7C7585)),
            Tag("recipes", Icons.Default.MenuBook, Color(0xFFE8A94D)),
            Tag("passwords", Icons.Default.Lock, Color(0xFF5B8DEF))
        )

        fun fromName(name: String): Tag {
            return DEFAULTS.find { it.name == name }
                ?: Tag(name, Icons.Default.Label, TagOther)
        }
    }
}
