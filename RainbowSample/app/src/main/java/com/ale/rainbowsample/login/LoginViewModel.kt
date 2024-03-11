package com.ale.rainbowsample.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ale.infra.rest.listeners.RainbowError
import com.ale.rainbowsample.R
import com.ale.rainbowsample.utils.LoginFieldsValidator
import com.ale.rainbowsample.utils.UiText
import com.ale.rainbowsdk.Connection
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel(), Connection.IConnectionListener {

    private val host = "openrainbow.net"
    private val loginValidator = LoginFieldsValidator()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val storedLogin = RainbowSdk().user().userLoginInCache
        val storedPassword = RainbowSdk().user().userPasswordInCache

        RainbowSdk().connection().registerConnectionListener(this)

        _uiState.update { currentUiState ->
            currentUiState.copy(
                login = storedLogin,
                password = storedPassword,
                isUserLoggedIn = RainbowSdk().connection().state.isAtLeastAuthenticated()
            )
        }

        if (storedLogin.isNotEmpty() && storedPassword.isNotEmpty()) {
            signIn(storedLogin, storedPassword)
        }
    }

    override fun onCleared() {
        super.onCleared()
        RainbowSdk().connection().unregisterConnectionListener(this)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            loginValidator.validateLoginFields(email, password)?.let { uiText ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(userMessage = uiText)
                }
                return@launch
            }

            RainbowSdk().connection().signIn(email, password, host, object : Connection.ISignInListener {
                override fun onSignInFailed(errorCode: Connection.ErrorCode, error: RainbowError<Unit>) {
                    val errorMessage = when (errorCode) {
                        Connection.ErrorCode.CONNECTION_WRONG_LOGIN_OR_PWD -> UiText.StringResource(R.string.incorrect_username_or_password)
                        else -> UiText.StringResource(R.string.an_error_occurred)
                    }

                    _uiState.update { currentUiState ->
                        currentUiState.copy(isLoading = false, userMessage = errorMessage)
                    }
                }

                override fun onSignInSucceeded() {
                    _uiState.update { currentUiState ->
                        currentUiState.copy(isLoading = false)
                    }
                }
            })
        }
    }

    fun userMessageShown() {
        _uiState.update { currentUiState ->
            currentUiState.copy(userMessage = null)
        }
    }

    override fun onStateChanged(newState: Connection.ConnectionState) {
        val isLoading = newState == Connection.ConnectionState.AUTHENTICATING

        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = isLoading,
                isUserLoggedIn = newState.isAtLeastAuthenticated()
            )
        }
    }
}

