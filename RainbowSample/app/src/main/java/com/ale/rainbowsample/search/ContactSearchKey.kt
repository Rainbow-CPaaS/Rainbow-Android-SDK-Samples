package com.ale.rainbowsample.search

import com.ale.infra.contact.Group
import com.ale.infra.contact.IRainbowContact

sealed class ContactSearchKey {
    data class RosterContact(val contact: IRainbowContact) : ContactSearchKey()
    data class GroupWithContacts(val group: Group) : ContactSearchKey()
    data class CompanyContact(val contact: IRainbowContact) : ContactSearchKey()
    data class LocalContact(val contact: IRainbowContact) : ContactSearchKey()
    data class PhoneBookContact(val contact: IRainbowContact) : ContactSearchKey()
    data class RainbowContact(val contact: IRainbowContact) : ContactSearchKey()
    data class Header(val sectionKey: ContactSearchSectionKey) : ContactSearchKey()
}

enum class ContactSearchSectionKey {
    ROSTER,
    GROUPS,
    COMPANY,
    LOCAL,
    PHONE_BOOK,
    RAINBOW_CONTACTS
}