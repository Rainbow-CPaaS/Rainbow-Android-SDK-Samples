package com.ale.rainbowsample.contacts

import androidx.lifecycle.ViewModel
import com.ale.infra.list.IItemListChangeListener
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ContactsViewModel : ViewModel() {

    private val rosterChangeListener = IItemListChangeListener { onContactsChanged() }

    private val _uiState = MutableStateFlow(
        ContactsUiState(
            contacts = emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

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
}