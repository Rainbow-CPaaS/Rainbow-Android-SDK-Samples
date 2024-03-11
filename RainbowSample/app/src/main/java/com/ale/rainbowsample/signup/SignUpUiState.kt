package com.ale.rainbowsample.signup

import com.ale.rainbowsample.utils.UiText

data class SignUpUiState(
    val login: String? = null,
    val password: String? = null,
    val isLoading: Boolean = false,
    val userMessage: UiText? = null,
    val registrationState: RegistrationState = RegistrationState.EMAIL
)

enum class RegistrationState {
    EMAIL, CODE, PROFILE, REGISTRATION_SUCCEED
}
