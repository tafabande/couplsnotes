package com.example.noteshare.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.noteshare.data.model.Event
import com.example.noteshare.data.model.MoodEntry
import com.example.noteshare.data.model.Note
import com.example.noteshare.ui.components.EmptyStates
import com.example.noteshare.ui.components.SectionErrorCard
import com.example.noteshare.ui.components.SkeletonList
import com.example.noteshare.ui.components.rememberVaultNavigationInterceptor
import com.example.noteshare.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNoteEditor: (String) -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToMemories: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val handleNoteClick = rememberVaultNavigationInterceptor(onNavigateToDetail = onNavigateToNoteDetail)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToNoteEditor("new") }) {
                Icon(Icons.Default.Edit, contentDescription = "New note")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                SkeletonList(count = 4, modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            if (!uiState.isOnline) {
                item { OfflineBanner() }
            }

            item {
                HeroHeader(
                    greeting = uiState.greeting,
                    emoji = uiState.greetingEmoji,
                    userName = uiState.userName,
                    isSyncing = uiState.isSyncing,
                    partnerName = uiState.partnerName,
                    isPartnerOnline = uiState.isPartnerOnline
                )
            }

            item {
                QuickActionsRow(
                    onMoodClick = onNavigateToMood,
                    onCalendarClick = onNavigateToCalendar,
                    onMemoriesClick = onNavigateToMemories,
                    onNewNoteClick = { onNavigateToNoteEditor("new") }
                )
            }

            if (uiState.loadError != null) {
                item {
                    SectionErrorCard(message = uiState.loadError!!, onRetry = { viewModel.refresh() })
                }
            }

            item {
                TodaySummaryCard(
                    nextEvent = uiState.nextEvent,
                    myMood = uiState.myLatestMood,
                    partnerMood = uiState.partnerLatestMood,
                    eventError = uiState.eventError,
                    moodError = uiState.moodError,
                    onCheckIn = onNavigateToMood,
                    onViewCalendar = onNavigateToCalendar,
                    onRetry = { viewModel.refresh() }
                )
            }

            item {
                SectionHeader(
                    title = "Recent notes",
                    subtitle = if (uiState.notesError == null) "${uiState.recentNotes.size} items" else "Error"
                )
            }

            if (uiState.notesError != null) {
                item {
                    SectionErrorCard(message = uiState.notesError!!, onRetry = { viewModel.refresh() })
                }
            } else if (uiState.recentNotes.isEmpty()) {
                item {
                    EmptyStates.NoNotes(onCreateNote = { onNavigateToNoteEditor("new") })
                }
            } else {
                items(uiState.recentNotes, key = { it.id }) { note ->
                    CompactNoteCard(note = note, onClick = { handleNoteClick(note) })
                }
            }
        }
    }
}

@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
        Spacer(modifier = Modifier.width(8.dp))
        Text("You're offline. Cached data is showing.", color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun HeroHeader(
    greeting: String,
    emoji: String,
    userName: String,
    isSyncing: Boolean,
    partnerName: String,
    isPartnerOnline: Boolean
) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "$emoji $greeting", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = userName.ifBlank { "there" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isSyncing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Syncing changes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            if (partnerName.isNotBlank()) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (isPartnerOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = partnerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (isPartnerOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onMoodClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onMemoriesClick: () -> Unit,
    onNewNoteClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { QuickActionChip(Icons.Default.Edit, "New note", onNewNoteClick) }
        item { QuickActionChip(Icons.Default.Mood, "Mood", onMoodClick) }
        item { QuickActionChip(Icons.Default.DateRange, "Calendar", onCalendarClick) }
        item { QuickActionChip(Icons.Default.Favorite, "Memories", onMemoriesClick) }
    }
}

@Composable
private fun TodaySummaryCard(
    nextEvent: Event?,
    myMood: MoodEntry?,
    partnerMood: MoodEntry?,
    eventError: String?,
    moodError: String?,
    onCheckIn: () -> Unit,
    onViewCalendar: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onRetry) { Text("Refresh") }
            }
            if (eventError != null) {
                SectionErrorCard(message = eventError, onRetry = onRetry, modifier = Modifier.padding(0.dp))
            } else if (nextEvent != null) {
                EventCountdownCard(event = nextEvent)
            } else {
                Text("No upcoming events.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (moodError != null) {
                SectionErrorCard(message = moodError, onRetry = onRetry, modifier = Modifier.padding(0.dp))
            } else {
                MoodSummaryCard(myMood = myMood, partnerMood = partnerMood, onCheckIn = onCheckIn)
            }
            TextButton(onClick = onViewCalendar) { Text("Open calendar") }
        }
    }
}

@Composable
private fun EventCountdownCard(event: Event) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), shape = MaterialTheme.shapes.large) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${event.typeEmoji} ${event.title}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(text = DateUtils.formatDate(event.date), color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
            }
            Text(text = DateUtils.getCountdownText(event.date), color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MoodSummaryCard(
    myMood: MoodEntry?,
    partnerMood: MoodEntry?,
    onCheckIn: () -> Unit
) {
    Card(
        onClick = onCheckIn,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            MoodCell(label = "You", emoji = myMood?.emoji)
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            MoodCell(label = "Partner", emoji = partnerMood?.emoji)
        }
    }
}

@Composable
private fun MoodCell(label: String, emoji: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 28.sp)
        } else {
            Text(text = "Check in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Bottom) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CompactNoteCard(note: Note, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (note.isPinned) {
                    Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                if (note.isVault) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vault note", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = note.authorName.ifBlank { "You" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(text = DateUtils.formatTimestamp(note.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
