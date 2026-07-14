package com.example.noteshare.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteshare.data.model.Event
import com.example.noteshare.data.repository.AuthRepository
import com.example.noteshare.data.repository.EventRepository
import com.example.noteshare.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init { loadEvents() }

    private fun loadEvents() {
        viewModelScope.launch {
            val user = (authRepository.getCurrentUserProfile() as? Result.Success)?.data ?: return@launch
            val pairId = user.pairId ?: return@launch
            _uiState.update { it.copy(pairId = pairId, userId = user.id) }

            eventRepository.getUpcomingEvents(pairId).collect { events ->
                _uiState.update { it.copy(isLoading = false, events = events) }
            }
        }
    }

    fun addEvent(title: String, date: Long, type: String, isRecurring: Boolean) {
        viewModelScope.launch {
            val pid = _uiState.value.pairId ?: return@launch
            val event = Event(
                title = title,
                date = date,
                type = type,
                isRecurring = isRecurring,
                createdBy = _uiState.value.userId
            )
            eventRepository.addEvent(pid, event)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = "Deleting events is not available yet.") }
        }
    }
}

data class CalendarUiState(
    val isLoading: Boolean = true,
    val events: List<Event> = emptyList(),
    val pairId: String? = null,
    val userId: String = "",
    val error: String? = null
)
