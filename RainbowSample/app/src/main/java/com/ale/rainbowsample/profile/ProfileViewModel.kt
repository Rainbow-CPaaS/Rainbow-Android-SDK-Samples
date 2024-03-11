package com.ale.rainbowsample.profile

import androidx.lifecycle.ViewModel
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.infra.rest.listeners.RainbowError
import com.ale.infra.rest.listeners.RainbowListener
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class ProfileViewModel: ViewModel(), IRainbowContact.IContactListener {

    private val _uiState = MutableStateFlow(ProfileUiState(
        firstName = RainbowSdk().user().getConnectedUser().firstName,
        lastName = RainbowSdk().user().getConnectedUser().lastName,
        nickName = RainbowSdk().user().getConnectedUser().nickName,
        company = RainbowSdk().user().getConnectedUser().companyName,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        RainbowSdk().user().getConnectedUser().registerChangeListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        RainbowSdk().user().getConnectedUser().unregisterChangeListener(this)
    }

    override fun contactUpdated(updatedContact: IRainbowContact) {
        _uiState.update {
            it.copy(
                lastAvatarUpdate = RainbowSdk().user().getConnectedUser().lastAvatarUpdateDate
            )
        }
    }

    override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
        _uiState.update {
            it.copy(
                userPresence = presence
            )
        }
    }

    fun uploadAvatar(file: File) {
        _uiState.update { it.copy(isLoading = true) }

        RainbowSdk().user().updatePhoto(file, object : RainbowListener<Unit, Unit> {
            override fun onError(error: RainbowError<Unit>) {
                _uiState.update { it.copy(isLoading = false) }
            }

            override fun onSuccess(data: Unit) {
                _uiState.update { it.copy(isLoading = false) }

            }
        })
    }

    fun deleteAvatar() {
        _uiState.update { it.copy(isLoading = true) }

        RainbowSdk().user().deletePhoto(object : RainbowListener<Unit, Unit> {
            override fun onError(error: RainbowError<Unit>) {
                _uiState.update { it.copy(isLoading = false) }
            }

            override fun onSuccess(data: Unit) {
                _uiState.update { it.copy(isLoading = false) }

            }
        })
    }
}