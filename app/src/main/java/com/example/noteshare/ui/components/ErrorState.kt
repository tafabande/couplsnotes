package com.example.noteshare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Error state component with icon, message, and retry action.
 */
@Composable
fun ErrorState(
    icon: ImageVector = Icons.Default.ErrorOutline,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String = "Try Again",
    onRetry: (() -> Unit)? = null
) {
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
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.error
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
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (onRetry != null) {
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionLabel)
            }
        }
    }
}

/**
 * Pre-configured error states for common error scenarios.
 */
object ErrorStates {
    @Composable
    fun NoInternet(onRetry: () -> Unit) {
        ErrorState(
            icon = Icons.Default.WifiOff,
            title = "You're offline",
            subtitle = "Changes will sync when you reconnect",
            actionLabel = "Retry",
            onRetry = onRetry
        )
    }

    @Composable
    fun UploadFailed(onRetry: () -> Unit) {
        ErrorState(
            icon = Icons.Default.CloudOff,
            title = "Upload failed",
            subtitle = "We couldn't upload your content. Please try again.",
            actionLabel = "Retry",
            onRetry = onRetry
        )
    }

    @Composable
    fun InviteExpired(onNewInvite: () -> Unit) {
        ErrorState(
            icon = Icons.Default.ErrorOutline,
            title = "Invite expired",
            subtitle = "This invite has expired. Ask your partner for a new one.",
            actionLabel = "New Invite",
            onRetry = onNewInvite
        )
    }

    @Composable
    fun ServerError(onRetry: () -> Unit) {
        ErrorState(
            icon = Icons.Default.ErrorOutline,
            title = "Something went wrong",
            subtitle = "We're having trouble connecting. Please try again later.",
            actionLabel = "Try Again",
            onRetry = onRetry
        )
    }

    @Composable
    fun GenericError(message: String, onRetry: (() -> Unit)? = null) {
        ErrorState(
            icon = Icons.Default.ErrorOutline,
            title = "Oops!",
            subtitle = message,
            onRetry = onRetry
        )
    }
}
