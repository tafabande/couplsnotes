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
            Tag("date", Icons.Default.Favorite, Tertiary),
            Tag("gift", Icons.Default.CardGiftcard, Primary),
            Tag("health", Icons.Default.FitnessCenter, Success),
            Tag("shopping", Icons.Default.ShoppingCart, Warning),
            Tag("thoughts", Icons.Default.Lightbulb, Secondary),
            Tag("recipes", Icons.Default.MenuBook, TertiaryDark),
            Tag("passwords", Icons.Default.Lock, PrimaryDark)
        )

        fun fromName(name: String): Tag {
            return DEFAULTS.find { it.name == name }
                ?: Tag(name, Icons.Default.Label, TagOther)
        }
    }
}
