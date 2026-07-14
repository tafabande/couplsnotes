package com.example.noteshare.ui.auth

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.noteshare.ui.theme.BackgroundLight
import com.example.noteshare.ui.theme.Primary
import com.example.noteshare.ui.theme.Secondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPairing: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val titleAlpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(900), label = "title_alpha")

    LaunchedEffect(Unit) {
        delay(1400)
        when {
            uiState.isLoggedIn && uiState.isPaired -> onNavigateToHome()
            uiState.isLoggedIn -> onNavigateToPairing()
            else -> onNavigateToWelcome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundLight, MaterialTheme.colorScheme.surfaceVariant))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .scale(pulse)
                    .background(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraLarge)
            )
            Text(
                text = "NoteShare",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(titleAlpha)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A calm shared place for notes and check-ins.",
                fontSize = 16.sp,
                color = Secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )
        }
    }
}
