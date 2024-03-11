package com.ale.rainbowsample.utils

import android.util.Patterns
import com.ale.rainbowsample.R

class LoginFieldsValidator {

    fun validateLoginFields(email: String, password: String) : UiText? {
        val emailValidation = validateEmail(email)
        return when {
            emailValidation != null -> emailValidation
            password.isEmpty() -> UiText.StringResource(R.string.password_required)
            else -> null
        }
    }

    fun validateEmail(email: String) : UiText? {
        return when {
            email.isEmpty() -> UiText.StringResource(R.string.username_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> UiText.StringResource(R.string.invalid_email_address)
            else -> null
        }
    }

    fun validatePassword(password: String) : UiText? {
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!_%*?&])[A-Za-z\\d@$!_%*?&]{12,}$".toRegex()
        val isMatch = passwordRegex.matches(password)
        return if (!isMatch) UiText.StringResource(R.string.password_rules) else null
    }

    fun validateDigitCode(code: String) : UiText? {
        return if (code.length != 6)
            UiText.StringResource(R.string.please_enter_the_6_digit_code)
        else
            null
    }

    fun validateUserInformation(firstName: String, lastName: String) : UiText? {
        return when {
            firstName.isEmpty() -> UiText.StringResource(R.string.please_enter_a_first_name)
            lastName.isEmpty() -> UiText.StringResource(R.string.please_enter_a_last_name)
            else -> null
        }
    }
}