package com.example.airteldatamonitor.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airteldatamonitor.data.model.RouterSmsMessage
import com.example.airteldatamonitor.data.model.SmsCenterState
import com.example.airteldatamonitor.data.model.UiState
import com.example.airteldatamonitor.data.model.UssdResponse
import com.example.airteldatamonitor.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SmsFilter {
    ALL,
    UNREAD,
    AIRTEL_OFFICIAL
}

data class UssdSessionState(
    val isDialing: Boolean = false,
    val lastDialedCode: String = "",
    val latestResponse: String? = null,
    val needReply: Boolean = false,
    val history: List<Pair<String, String>> = emptyList() // Pair(Code, Response)
)

class SmsCenterViewModel(
    private val repository: RouterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SmsCenterState>>(UiState.Loading)
    val uiState: StateFlow<UiState<SmsCenterState>> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SmsFilter.ALL)
    val selectedFilter: StateFlow<SmsFilter> = _selectedFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    private val _ussdSession = MutableStateFlow(UssdSessionState())
    val ussdSession: StateFlow<UssdSessionState> = _ussdSession.asStateFlow()

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = repository.fetchSmsMessages(folder = 0)
            _uiState.value = res
            _isRefreshing.value = false
        }
    }

    fun setFilter(filter: SmsFilter) {
        _selectedFilter.value = filter
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            val res = repository.markSmsAsRead(id)
            if (res.isSuccess) {
                // Update local state reactively
                val curr = _uiState.value
                if (curr is UiState.Success) {
                    val updated = curr.data.messages.map {
                        if (it.id == id || id == "READ ALL") it.copy(isRead = true) else it
                    }
                    _uiState.value = UiState.Success(
                        curr.data.copy(
                            messages = updated,
                            unreadCount = if (id == "READ ALL") 0 else maxOf(0, curr.data.unreadCount - 1)
                        )
                    )
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _actionFeedback.value = "Marking all as read..."
            val res = repository.markSmsAsRead("READ ALL")
            if (res.isSuccess) {
                _actionFeedback.value = "✓ All messages marked as read"
                markAsRead("READ ALL")
            } else {
                _actionFeedback.value = "✗ Failed to mark all as read"
            }
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            _actionFeedback.value = "Deleting message..."
            val res = repository.deleteSms(id)
            if (res.isSuccess) {
                _actionFeedback.value = "✓ Message deleted"
                val curr = _uiState.value
                if (curr is UiState.Success) {
                    val deletedMsg = curr.data.messages.find { it.id == id }
                    val updated = curr.data.messages.filter { it.id != id }
                    _uiState.value = UiState.Success(
                        curr.data.copy(
                            messages = updated,
                            totalCount = maxOf(0, curr.data.totalCount - 1),
                            unreadCount = if (deletedMsg?.isRead == false) maxOf(0, curr.data.unreadCount - 1) else curr.data.unreadCount
                        )
                    )
                }
            } else {
                _actionFeedback.value = "✗ Failed to delete message"
            }
        }
    }

    fun sendSms(phoneNo: String, content: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _actionFeedback.value = "Sending SMS from router SIM..."
            val res = repository.sendSms(phoneNo, content)
            if (res.isSuccess) {
                _actionFeedback.value = "✓ SMS transmitted to $phoneNo"
                onComplete(true)
            } else {
                val err = res.exceptionOrNull()?.message ?: "Failed to send SMS"
                _actionFeedback.value = "✗ $err"
                onComplete(false)
            }
        }
    }

    fun dialUssd(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _ussdSession.value = _ussdSession.value.copy(
                isDialing = true,
                lastDialedCode = trimmed,
                latestResponse = null
            )
            val res = repository.sendUssd(trimmed)
            if (res.isSuccess) {
                val resp = res.getOrThrow()
                val updatedHistory = listOf(trimmed to resp.message) + _ussdSession.value.history
                _ussdSession.value = _ussdSession.value.copy(
                    isDialing = false,
                    latestResponse = resp.message,
                    needReply = resp.needReply,
                    history = updatedHistory.take(10)
                )
            } else {
                val err = res.exceptionOrNull()?.message ?: "USSD request timed out"
                _ussdSession.value = _ussdSession.value.copy(
                    isDialing = false,
                    latestResponse = "Error: $err"
                )
            }
        }
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }
}
