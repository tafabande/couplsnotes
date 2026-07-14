package com.example.noteshare

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.noteshare.ui.auth.AuthViewModel
import com.example.noteshare.ui.navigation.BottomNavBar
import com.example.noteshare.ui.navigation.BottomNavTab
import com.example.noteshare.ui.navigation.NoteShareNavGraph
import com.example.noteshare.ui.navigation.Screen
import com.example.noteshare.ui.theme.NoteShareTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteShareTheme {
                NoteShareApp()
            }
        }
    }
}

@Composable
fun NoteShareApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that show the bottom nav bar (main tabs only)
    val mainTabRoutes = BottomNavTab.values().map { it.route }
    val showBottomBar = currentRoute in mainTabRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { padding ->
        NoteShareNavGraph(
            navController = navController,
            isLoggedIn = uiState.isLoggedIn,
            isPaired = uiState.isPaired,
            modifier = Modifier.padding(padding)
        )
    }
}
