package com.ale.rainbowsample.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ale.infra.contact.Contact
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.rest.listeners.RainbowError
import com.ale.infra.rest.listeners.RainbowListener
import com.ale.infra.rest.user.SelfRegisterBody
import com.ale.rainbowsample.R
import com.ale.rainbowsample.utils.LoginFieldsValidator
import com.ale.rainbowsample.utils.UiText
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val loginValidator = LoginFieldsValidator()

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    var email: String? = null
    var password: String? = null
    private var code: String? = null

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            loginValidator.validateEmail(email)?.let { uiText ->
                _uiState.update { state -> state.copy(userMessage = uiText) }
                return@launch
            }

            loginValidator.validatePassword(password)?.let { uiText ->
                _uiState.update { state -> state.copy(userMessage = uiText) }
                return@launch
            }

            _uiState.update { state -> state.copy(isLoading = true) }

            this@SignUpViewModel.email = email
            this@SignUpViewModel.password = password

            RainbowSdk().user().selfRegisterByEmail(email, lang = null, listener = object : RainbowListener<Unit, Unit> {
                override fun onError(error: RainbowError<Unit>) {
                    _uiState.update { state -> state.copy(isLoading = false, userMessage = UiText.StringResource(R.string.an_error_occurred)) }
                }

                override fun onSuccess(data: Unit) {
                    _uiState.update { state -> state.copy(registrationState = RegistrationState.CODE, isLoading = false) }
                }
            })
        }
    }

    fun registerWithCode(code: String) {
        viewModelScope.launch {
            loginValidator.validateDigitCode(code)?.let { uiText ->
                _uiState.update { state -> state.copy(userMessage = uiText) }
                return@launch
            }

            this@SignUpViewModel.code = code
            _uiState.update { state -> state.copy(registrationState = RegistrationState.PROFILE) }
        }
    }

    fun register(firstName: String, lastName: String) {
        viewModelScope.launch {
            loginValidator.validateUserInformation(firstName, lastName)?.let { uiText ->
                _uiState.update { state -> state.copy(userMessage = uiText) }
                return@launch
            }

            _uiState.update { state -> state.copy(isLoading = true) }

            val selfRegisterBody = SelfRegisterBody.Builder()
                .temporaryToken(code)
                .loginEmail(email)
                .password(password)
                .build()

            RainbowSdk().user().selfRegisterUser(selfRegisterBody, object : RainbowListener<IRainbowContact, Unit> {
                override fun onError(error: RainbowError<Unit>) {
                    _uiState.update { state -> state.copy(isLoading = false, userMessage = UiText.StringResource(R.string.an_error_occurred)) }
                }

                override fun onSuccess(data: IRainbowContact) {
                    _uiState.update { state -> state.copy(registrationState = RegistrationState.REGISTRATION_SUCCEED) }
                }
            })
        }
    }

    fun userMessageShown() {
        _uiState.update { currentUiState ->
            currentUiState.copy(userMessage = null)
        }
    }
}