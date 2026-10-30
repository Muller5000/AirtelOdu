package com.example.airteldatamonitor.data.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
}

enum class RouterConnectionStatus(val label: String) {
    CONNECTED("Connected (Wi-Fi)"),
    CONNECTING("Connecting to Router..."),
    DISCONNECTED("Disconnected"),
    AUTH_FAILED("Authentication Failed"),
    UNREACHABLE("Router Unreachable")
}
