package com.example.noteshare.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.noteshare.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPairing: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "title_alpha"
    )

    // Navigate after delay
    LaunchedEffect(Unit) {
        delay(2000)
        when {
            uiState.isLoggedIn && uiState.isPaired -> onNavigateToHome()
            uiState.isLoggedIn -> onNavigateToPairing()
            else -> onNavigateToWelcome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary, SecondaryDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Heart emoji with heartbeat animation
            Text(
                text = "💕",
                fontSize = 72.sp,
                modifier = Modifier
                    .scale(heartScale)
                    .padding(bottom = 24.dp)
            )

            // App name
            Text(
                text = "NoteShare",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share moments together",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )
        }
    }
}
