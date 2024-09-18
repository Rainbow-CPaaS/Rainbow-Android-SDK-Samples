package com.ale.rainbowsample.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ale.infra.contact.Group
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.rest.listeners.RainbowError
import com.ale.infra.rest.listeners.RainbowListener
import com.ale.rainbowsample.contacts.mapToContactsUiState
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {

    private val rosterChangeListener = IItemListChangeListener { refreshContactList() }

    private val _name = MutableStateFlow("")
    private val _description = MutableStateFlow("")

    private val _contacts = MutableStateFlow<List<IRainbowContact>>(emptyList())
    val contacts = _contacts.map { it.mapToContactsUiState() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedContacts = MutableStateFlow<List<IRainbowContact>>(emptyList())
    val selectedContacts = _selectedContacts.map { it.mapToContactsUiState() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isButtonEnabled = combine(_name, _selectedContacts) { name, contacts ->
        name.isNotBlank() && contacts.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _apiResult = MutableSharedFlow<Result<Any>>()
    val apiResult = _apiResult.asSharedFlow()

    init {
        RainbowSdk().contacts().roster.registerChangeListener(rosterChangeListener)
        refreshContactList()
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun selectContact(contact: IRainbowContact) {
        _selectedContacts.value += contact
        refreshContactList()
    }

    fun unselectContact(contact: IRainbowContact) {
        _selectedContacts.value -= contact
        refreshContactList()
    }

    fun createGroup() {
        RainbowSdk().groups().createGroup(_name.value, _description.value, object : RainbowListener<Group, Unit> {
            override fun onSuccess(data: Group) {
                if (_selectedContacts.value.isNotEmpty()) {
                    RainbowSdk().groups().addContactsToGroup(data.id, _selectedContacts.value.mapNotNull { it.id }, object : RainbowListener<Unit, List<RainbowError<Unit>>> {
                        override fun onSuccess(data: Unit) {
                            viewModelScope.launch { _apiResult.emit(Result.success(Unit)) }
                        }

                        override fun onError(error: RainbowError<List<RainbowError<Unit>>>) {
                            viewModelScope.launch { _apiResult.emit(Result.failure(Throwable("Error while adding participants"))) }
                        }
                    })
                } else {
                    viewModelScope.launch { _apiResult.emit(Result.success(Unit)) }
                }
            }

            override fun onError(error: RainbowError<Unit>) {
                viewModelScope.launch { _apiResult.emit(Result.failure(Throwable("Error while creating group"))) }
            }
        })
    }

    private fun refreshContactList() {
        val roster = RainbowSdk().contacts().roster.copyOfDataList
        _contacts.update {
            roster.filter { it !in _selectedContacts.value }
        }
    }
}