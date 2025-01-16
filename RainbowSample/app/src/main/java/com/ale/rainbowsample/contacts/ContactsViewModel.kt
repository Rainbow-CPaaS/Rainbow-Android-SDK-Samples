package com.ale.rainbowsample.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.rest.listeners.onFailure
import com.ale.infra.rest.listeners.onSuccess
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val rosterChangeListener = IItemListChangeListener { onContactsChanged() }

    private val _uiState = MutableStateFlow(
        ContactsUiState(
            contacts = emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _eventSharedFlow = MutableSharedFlow<Event>()
    val eventSharedFlow = _eventSharedFlow.asSharedFlow()

    init {
        RainbowSdk().contacts().roster.registerChangeListener(rosterChangeListener)
        onContactsChanged()
    }

    override fun onCleared() {
        RainbowSdk().contacts().roster.unregisterChangeListener(rosterChangeListener)
    }

    private fun onContactsChanged() {
        val contacts = RainbowSdk().contacts().roster.copyOfDataList

        _uiState.update {
            it.copy(
                contacts = contacts.mapToContactsUiState()
            )
        }
    }

    fun inviteContactToJoinRoster(contact: IRainbowContact) {
        viewModelScope.launch {
            if (RainbowSdk().invitations().isContactInvited(contact)) {
                _eventSharedFlow.emit(Event.PENDING_INVITATION)
            } else {
                RainbowSdk().invitations().addContactToNetwork(contact)
                    .onSuccess {
                        _eventSharedFlow.emit(Event.INVITATION_SEND_SUCCESS)
                    }
                    .onFailure {
                        _eventSharedFlow.emit(Event.INVITATION_SEND_FAILURE)
                    }
            }
        }
    }

    fun removeContactFromRoster(contact: IRainbowContact) {
        viewModelScope.launch {
            RainbowSdk().invitations().removeContactFromNetwork(contact)
                .onSuccess { _eventSharedFlow.emit(Event.REMOVE_SUCCESS) }
                .onFailure { _eventSharedFlow.emit(Event.REMOVE_FAILURE) }
        }
    }

    enum class Event {
        INVITATION_SEND_SUCCESS, INVITATION_SEND_FAILURE, REMOVE_SUCCESS, REMOVE_FAILURE,PENDING_INVITATION
    }
}