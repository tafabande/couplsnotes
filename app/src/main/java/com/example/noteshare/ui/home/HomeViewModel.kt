package com.example.noteshare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.Event
import com.example.noteshare.data.model.MoodEntry
import com.example.noteshare.data.model.Note
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.EventRepository
import com.example.noteshare.data.repository.MoodRepository
import com.example.noteshare.data.repository.NoteRepository
import com.example.noteshare.util.DateUtils
import com.example.noteshare.util.NetworkMonitor
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    private var dashboardJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadError = null,
                    notesError = null,
                    moodError = null,
                    eventError = null
                )
            }

            val userResult = authRepository.getCurrentUserProfile()
            val user = (userResult as? Result.Success)?.data
            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = (userResult as? Result.Error)?.message
                            ?: "We could not load your dashboard."
                    )
                }
                return@launch
            }

            val pairId = user.pairId
            _uiState.update {
                it.copy(
                    greeting = DateUtils.getGreeting(),
                    greetingEmoji = DateUtils.getGreetingEmoji(),
                    userName = user.displayName,
                    isPaired = pairId != null,
                    partnerName = if (pairId != null) "Partner" else ""
                )
            }

            val children = mutableListOf<Job>()

            if (pairId != null) {
                children += launch {
                    noteRepository.getLocalNotes(pairId)
                        .catch { _uiState.update { state -> state.copy(notesError = "Failed to load recent notes") } }
                        .collect { notes ->
                            val recent = notes.filterNot { it.isArchived || it.isDeleted }
                                .sortedByDescending { it.updatedAt }
                                .take(5)
                            _uiState.update { state -> state.copy(recentNotes = recent) }
                        }
                }

                children += launch {
                    moodRepository.getLocalMoods(pairId)
                        .catch { _uiState.update { state -> state.copy(moodError = "Failed to load moods") } }
                        .collect { moods ->
                            val myMood = moods.firstOrNull { it.userId == authRepository.currentUserId }
                            val partnerMood = moods.firstOrNull { it.userId != authRepository.currentUserId }
                            _uiState.update {
                                it.copy(myLatestMood = myMood, partnerLatestMood = partnerMood)
                            }
                        }
                }

                children += launch {
                    eventRepository.getUpcomingEvents(pairId)
                        .catch { _uiState.update { state -> state.copy(eventError = "Failed to load events") } }
                        .collect { events ->
                            val upcoming = events.filter { it.date > System.currentTimeMillis() }
                                .sortedBy { it.date }
                                .firstOrNull()
                            _uiState.update { it.copy(nextEvent = upcoming) }
                        }
                }
            }

            children += launch {
                outboxDao.observePendingEventCount().collect { count ->
                    _uiState.update { it.copy(isSyncing = count > 0) }
                }
            }

            children += launch {
                networkMonitor.isOnline.collect { isOnline ->
                    _uiState.update { it.copy(isOnline = isOnline) }
                }
            }

            _uiState.update { it.copy(isPartnerOnline = pairId != null) }
            _uiState.update { it.copy(isLoading = false) }
        }
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
    val eventError: String? = null,
    val loadError: String? = null
)
