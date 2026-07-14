package com.example.noteshare.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shimmer effect modifier for skeleton loading states.
 * Creates a Facebook-style animated gradient sweep across the placeholder.
 */
@Composable
fun shimmerBrush(show: Boolean = true): Brush {
    return if (show) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }
}

/**
 * Skeleton placeholder for a card.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
    )
}

/**
 * Skeleton placeholder for a text line.
 */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    width: Float = 1f,
    height: Dp = 16.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth(width)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
    )
}

/**
 * Skeleton placeholder for a circular avatar.
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
    )
}

/**
 * Skeleton layout mimicking a note card for loading state.
 */
@Composable
fun SkeletonNoteCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonLine(width = 0.6f, height = 20.dp)
        SkeletonLine(width = 1f, height = 14.dp)
        SkeletonLine(width = 0.8f, height = 14.dp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonCircle(size = 24.dp)
            SkeletonLine(width = 0.3f, height = 12.dp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/**
 * Multiple skeleton cards for list loading states.
 */
@Composable
fun SkeletonList(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            SkeletonNoteCard()
        }
    }
}
