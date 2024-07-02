package com.ale.rainbowsample.contacts

import com.ale.infra.contact.IRainbowContact

data class ContactsUiState(
    val contacts: List<ContactUiState>
)

data class ContactUiState(
    val contact: IRainbowContact,
)

fun List<IRainbowContact>.mapToContactsUiState(): List<ContactUiState> {
    return this.map {
        ContactUiState(
            contact = it,
        )
    }
}