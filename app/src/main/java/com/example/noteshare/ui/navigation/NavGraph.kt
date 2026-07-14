package com.example.noteshare.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.noteshare.ui.auth.*
import com.example.noteshare.ui.calendar.CalendarScreen
import com.example.noteshare.ui.home.HomeScreen
import com.example.noteshare.ui.memories.MemoriesScreen
import com.example.noteshare.ui.mood.MoodScreen
import com.example.noteshare.ui.notes.NoteDetailScreen
import com.example.noteshare.ui.notes.NoteEditorScreen
import com.example.noteshare.ui.notes.NotesScreen
import com.example.noteshare.ui.pairing.*
import com.example.noteshare.ui.settings.ProfileScreen
import com.example.noteshare.ui.settings.RelationshipSettingsScreen
import com.example.noteshare.ui.settings.SettingsScreen
import com.example.noteshare.ui.timeline.TimelineScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteShareNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    isPaired: Boolean,
    modifier: Modifier = Modifier
) {
    val startDestination = when {
        !isLoggedIn -> Screen.Splash.route
        !isPaired -> Screen.InvitePartner.route
        else -> Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { 100 },
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { -100 },
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { -100 },
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { 100 },
                animationSpec = tween(300)
            )
        }
    ) {
        // ═══════════════════════════════════
        // Auth Flow
        // ═══════════════════════════════════
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToPairing = {
                    navController.navigate(Screen.InvitePartner.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.InvitePartner.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════
        // Pairing Flow
        // ═══════════════════════════════════
        composable(Screen.InvitePartner.route) {
            InvitePartnerScreen(
                onNavigateToEnterCode = { navController.navigate(Screen.EnterInviteCode.route) },
                onNavigateToWaiting = { navController.navigate(Screen.WaitingForPartner.route) },
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.InvitePartner.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EnterInviteCode.route) {
            EnterInviteCodeScreen(
                onPairSuccess = {
                    navController.navigate(Screen.PairSuccess.route) {
                        popUpTo(Screen.InvitePartner.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.WaitingForPartner.route) {
            WaitingScreen(
                onPartnerJoined = {
                    navController.navigate(Screen.PairSuccess.route) {
                        popUpTo(Screen.InvitePartner.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PairSuccess.route) {
            PairSuccessScreen(
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PairSuccess.route) { inclusive = true }
                    }
                }
            )
        }

        // ═══════════════════════════════════
        // Main Tabs
        // ═══════════════════════════════════
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNoteEditor = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                },
                onNavigateToMood = { navController.navigate(Screen.Mood.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToMemories = { navController.navigate(Screen.Memories.route) },
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateToEditor = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId))
                },
                onNavigateToDetail = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        composable(Screen.Timeline.route) {
            TimelineScreen(
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToRelationship = { navController.navigate(Screen.RelationshipSettings.route) },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ═══════════════════════════════════
        // Detail Screens
        // ═══════════════════════════════════
        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
            NoteEditorScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { id ->
                    navController.navigate(Screen.NoteEditor.createRoute(id))
                }
            )
        }

        composable(Screen.Mood.route) {
            MoodScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Memories.route) {
            MemoriesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RelationshipSettings.route) {
            RelationshipSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
