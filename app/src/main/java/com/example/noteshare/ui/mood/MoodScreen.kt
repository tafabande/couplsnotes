package com.example.noteshare.ui.mood

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.noteshare.ui.components.EmptyStates
import com.example.noteshare.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: MoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMood by remember { mutableIntStateOf(0) }
    var moodNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mood check-in", fontWeight = FontWeight.SemiBold)
                        Text("Keep it quick and honest", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("How are you feeling?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(
                                1 to "😢",
                                2 to "😕",
                                3 to "😐",
                                4 to "😊",
                                5 to "🥰"
                            ).forEach { (level, emoji) ->
                                MoodEmoji(
                                    level = level,
                                    emoji = emoji,
                                    isSelected = selectedMood == level,
                                    onClick = { selectedMood = level }
                                )
                            }
                        }

                        if (selectedMood > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = moodNote,
                                onValueChange = { moodNote = it },
                                placeholder = { Text("Add a note (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.submitMood(selectedMood, moodNote)
                                    selectedMood = 0
                                    moodNote = ""
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save check-in")
                            }
                        }

                        if (uiState.justSubmitted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mood recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            item {
                Text("Recent moods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            if (uiState.moods.isEmpty()) {
                item { EmptyStates.NoMoods(onCheckIn = {}) }
            } else {
                items(uiState.moods.take(20)) { mood ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = mood.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = mood.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                mood.note?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(text = DateUtils.formatTimestamp(mood.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodEmoji(
    level: Int,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "mood_scale"
    )

    val rotation = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            when (level) {
                1, 2 -> {
                    rotation.animateTo(12f, animationSpec = tween(100))
                    rotation.animateTo(-12f, animationSpec = tween(100))
                    rotation.animateTo(0f, animationSpec = tween(100))
                }
                4, 5 -> {
                    offsetY.animateTo(-16f, animationSpec = tween(150))
                    offsetY.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = emoji,
            fontSize = 36.sp,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation.value)
                .padding(bottom = offsetY.value.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.size(6.dp)
            ) {}
        }
    }
}
