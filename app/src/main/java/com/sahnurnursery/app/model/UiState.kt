package com.sahnurnursery.app.model

/**
 * Sealed interface representing the global UI State for asynchronous / database operations.
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val actionLabel: String? = null,
        val onRetry: (() -> Unit)? = null
    ) : UiState<Nothing>
}

/**
 * Sealed interface representing database operation outcomes.
 */
sealed interface DatabaseResult<out T> {
    data class Success<out T>(val data: T, val message: String? = null) : DatabaseResult<T>
    data class Error(
        val userMessage: String,
        val throwable: Throwable? = null
    ) : DatabaseResult<Nothing>
}

/**
 * Sealed hierarchy for user feedback messages (Errors, Success, Warnings, Info).
 */
sealed class UiFeedback {
    abstract val id: Long
    abstract val message: String

    data class Error(
        override val id: Long = System.currentTimeMillis(),
        override val message: String,
        val cause: Throwable? = null,
        val actionLabel: String? = "Retry",
        val retryAction: (() -> Unit)? = null
    ) : UiFeedback()

    data class Success(
        override val id: Long = System.currentTimeMillis(),
        override val message: String
    ) : UiFeedback()

    data class Warning(
        override val id: Long = System.currentTimeMillis(),
        override val message: String
    ) : UiFeedback()

    data class Info(
        override val id: Long = System.currentTimeMillis(),
        override val message: String
    ) : UiFeedback()
}
