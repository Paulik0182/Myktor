package com.nayya.uicomponents

sealed interface BottomTextState {
    data class Description(
        val descriptionText: String?,
        val descriptionIcon: Int = 0,
        val showDescriptionText: Boolean = true,
        val showDescriptionIcon: Boolean = true,
    ) : BottomTextState

    data class Error(
        val errorText: String?,
        val errorIcon: Int = 0,
        val showErrorText: Boolean = true,
        val showErrorIcon: Boolean = true,
    ) : BottomTextState

    object Empty : BottomTextState
}
