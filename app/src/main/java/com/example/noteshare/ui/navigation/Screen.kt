package com.example.noteshare.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * All navigation routes in the app.
 */
sealed class Screen(val route: String) {
    // Auth flow
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Pairing flow
    data object InvitePartner : Screen("invite_partner")
    data object EnterInviteCode : Screen("enter_invite_code")
    data object WaitingForPartner : Screen("waiting_for_partner")
    data object PairSuccess : Screen("pair_success")

    // Main tabs
    data object Home : Screen("home")
    data object Notes : Screen("notes")
    data object Timeline : Screen("timeline")
    data object Settings : Screen("settings")

    // Detail screens
    data object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String = "new") = "note_editor/$noteId"
    }
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    data object Mood : Screen("mood")
    data object Calendar : Screen("calendar")
    data object Memories : Screen("memories")
    data object Profile : Screen("profile")
    data object RelationshipSettings : Screen("relationship_settings")
}

/**
 * Bottom navigation tab items.
 */
enum class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = Screen.Home.route,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    NOTES(
        route = Screen.Notes.route,
        label = "Notes",
        selectedIcon = Icons.Filled.StickyNote2,
        unselectedIcon = Icons.Outlined.StickyNote2
    ),
    TIMELINE(
        route = Screen.Timeline.route,
        label = "Timeline",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline
    ),
    SETTINGS(
        route = Screen.Settings.route,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}
