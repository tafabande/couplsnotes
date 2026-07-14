package com.example.noteshare.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Thoughtful empty state component with emoji, message, and optional action button.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    // Gentle breathing animation on the emoji
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_breathe"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size((48 * scale).dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(0.7f)
                .padding(bottom = 24.dp)
        )

        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Pre-configured empty states for common scenarios.
 */
object EmptyStates {
    @Composable
    fun NoNotes(onCreateNote: () -> Unit) {
        EmptyState(
            icon = Icons.Default.Edit,
            title = "Your first note is waiting",
            subtitle = "Start sharing thoughts, ideas, and moments with your partner",
            actionLabel = "Write a Note",
            onAction = onCreateNote
        )
    }

    @Composable
    fun NoMemories(onAddMemory: () -> Unit) {
        EmptyState(
            icon = Icons.Default.PhotoCamera,
            title = "No memories yet",
            subtitle = "Capture your special moments together",
            actionLabel = "Add Memory",
            onAction = onAddMemory
        )
    }

    @Composable
    fun NoEvents(onAddEvent: () -> Unit) {
        EmptyState(
            icon = Icons.Default.DateRange,
            title = "No upcoming events",
            subtitle = "Add birthdays, anniversaries, and dates to never forget",
            actionLabel = "Add Event",
            onAction = onAddEvent
        )
    }

    @Composable
    fun NoTimeline() {
        EmptyState(
            icon = Icons.Default.Star,
            title = "Your story starts here",
            subtitle = "Notes, moods, and memories will appear in your shared timeline"
        )
    }

    @Composable
    fun NoMoods(onCheckIn: () -> Unit) {
        EmptyState(
            icon = Icons.Default.Mood,
            title = "How are you feeling?",
            subtitle = "Share your mood with your partner",
            actionLabel = "Check In",
            onAction = onCheckIn
        )
    }

    @Composable
    fun NoPartner(onInvite: () -> Unit) {
        EmptyState(
            icon = Icons.Default.Favorite,
            title = "Better together",
            subtitle = "Invite your partner to start sharing notes and memories",
            actionLabel = "Invite Partner",
            onAction = onInvite
        )
    }
}
