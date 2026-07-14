package com.example.noteshare.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.*
import com.example.noteshare.data.repository.*
import com.example.noteshare.util.NetworkMonitor
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val moodRepository: MoodRepository,
    private val eventRepository: EventRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init { loadTimeline() }

    private fun loadTimeline() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = (authRepository.getCurrentUserProfile() as? Result.Success)?.data ?: return@launch
            val pairId = user.pairId ?: return@launch

            // Combine notes, moods, and events into a unified timeline using Room as SSOT
            _uiState.update { it.copy(timelineError = null) }
            combine(
                noteRepository.getLocalNotes(pairId),
                moodRepository.getLocalMoods(pairId),
                eventRepository.getUpcomingEvents(pairId)
            ) { notes, moods, events ->
                val items = mutableListOf<TimelineItem>()

                notes.filter { !it.isDeleted }.forEach { note ->
                    items.add(TimelineItem.NoteItem(note))
                }
                moods.forEach { mood ->
                    items.add(TimelineItem.MoodItem(mood))
                }
                events.forEach { event ->
                    items.add(TimelineItem.EventItem(event))
                }

                items.sortedByDescending { it.timestamp }
            }
            .catch { _uiState.update { it.copy(timelineError = "Failed to load timeline") } }
            .collect { items ->
                _uiState.update { it.copy(isLoading = false, items = items) }
            }
        }

        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
        }
    }
}

data class TimelineUiState(
    val isLoading: Boolean = false,
    val items: List<TimelineItem> = emptyList(),
    val isOnline: Boolean = true,
    val timelineError: String? = null
)

sealed class TimelineItem {
    abstract val timestamp: Long

    data class NoteItem(val note: Note) : TimelineItem() {
        override val timestamp: Long = note.createdAt
    }
    data class MoodItem(val mood: MoodEntry) : TimelineItem() {
        override val timestamp: Long = mood.createdAt
    }
    data class EventItem(val event: Event) : TimelineItem() {
        override val timestamp: Long = event.createdAt
    }
}
