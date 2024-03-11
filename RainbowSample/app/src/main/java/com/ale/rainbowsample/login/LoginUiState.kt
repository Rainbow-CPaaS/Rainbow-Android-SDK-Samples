package com.ale.rainbowsample.login

import com.ale.rainbowsample.utils.UiText

data class LoginUiState(
    val login: String? = null,
    val password: String? = null,
    val isLoading: Boolean = false,
    val userMessage: UiText? = null,
    val isUserLoggedIn: Boolean = false
)