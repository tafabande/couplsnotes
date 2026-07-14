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

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import com.example.noteshare.util.NetworkMonitor
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteShareTheme {
                NoteShareAppContent()
            }
        }
    }
}

@Composable
fun NoteShareAppContent() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isOnline.collectAsState(initial = true)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that show the bottom nav bar (main tabs only)
    val mainTabRoutes = BottomNavTab.values().map { it.route }
    val showBottomBar = currentRoute in mainTabRoutes

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = !isConnected,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You are currently offline",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BottomNavBar(navController = navController)
                }
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
