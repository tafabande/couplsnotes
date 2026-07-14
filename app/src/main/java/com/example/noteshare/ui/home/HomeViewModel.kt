package com.example.noteshare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.*
import com.example.noteshare.data.repository.*
import com.example.noteshare.util.DateUtils
import com.example.noteshare.util.NetworkMonitor
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val moodRepository: MoodRepository,
    private val eventRepository: EventRepository,
    private val outboxDao: com.example.noteshare.data.local.db.dao.OutboxDao,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = authRepository.currentUserId ?: return@launch
            val userResult = authRepository.getCurrentUserProfile()
            val user = (userResult as? Result.Success)?.data ?: return@launch
            val pairId = user.pairId

            _uiState.update {
                it.copy(
                    greeting = DateUtils.getGreeting(),
                    greetingEmoji = DateUtils.getGreetingEmoji(),
                    userName = user.displayName,
                    isPaired = pairId != null
                )
            }

            if (pairId != null) {
                // Load recent notes from Local SSOT
                launch {
                    _uiState.update { it.copy(notesError = null) }
                    noteRepository.getLocalNotes(pairId)
                        .catch { _uiState.update { it.copy(notesError = "Failed to load recent notes") } }
                        .collect { notes ->
                            val recent = notes.filter { !it.isArchived && !it.isDeleted }
                                .sortedByDescending { it.updatedAt }
                                .take(5)
                            _uiState.update { it.copy(recentNotes = recent) }
                        }
                }

                // Load moods from Local SSOT
                launch {
                    _uiState.update { it.copy(moodError = null) }
                    moodRepository.getLocalMoods(pairId)
                        .catch { _uiState.update { it.copy(moodError = "Failed to load moods") } }
                        .collect { moods ->
                            val myMood = moods.firstOrNull { it.userId == userId }
                            val partnerMood = moods.firstOrNull { it.userId != userId }
                            _uiState.update {
                                it.copy(myLatestMood = myMood, partnerLatestMood = partnerMood)
                            }
                        }
                }

                // Load next event from Local SSOT
                launch {
                    _uiState.update { it.copy(eventError = null) }
                    eventRepository.getUpcomingEvents(pairId)
                        .catch { _uiState.update { it.copy(eventError = "Failed to load events") } }
                        .collect { events ->
                            val upcoming = events.filter { it.date > System.currentTimeMillis() }
                                .sortedBy { it.date }
                                .firstOrNull()
                            _uiState.update { it.copy(nextEvent = upcoming) }
                        }
                }
                
                // TODO: Firebase RTDB Presence Heartbeat
                // For now, we mock the partner being online to show the UI
                _uiState.update { it.copy(isPartnerOnline = true, partnerName = "Partner") }

                // Observe Sync State
                launch {
                    outboxDao.observePendingEventCount().collect { count ->
                        _uiState.update { it.copy(isSyncing = count > 0) }
                    }
                }
                
                // Observe Network Status
                launch {
                    networkMonitor.isOnline.collect { isOnline ->
                        _uiState.update { it.copy(isOnline = isOnline) }
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val greetingEmoji: String = "",
    val userName: String = "",
    val isPaired: Boolean = false,
    val recentNotes: List<Note> = emptyList(),
    val myLatestMood: MoodEntry? = null,
    val partnerLatestMood: MoodEntry? = null,
    val nextEvent: Event? = null,
    val isPartnerOnline: Boolean = false,
    val partnerName: String = "",
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val notesError: String? = null,
    val moodError: String? = null,
    val eventError: String? = null
)
