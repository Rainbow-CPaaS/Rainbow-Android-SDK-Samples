package com.ale.rainbowsample.search

import androidx.lifecycle.ViewModel
import com.ale.infra.contact.Group
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.rest.listeners.RainbowError
import com.ale.infra.searcher.searchcriteria.SearchCriteria
import com.ale.rainbowsdk.RainbowSdk
import com.ale.rainbowsdk.Search
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading = _searchLoading.asStateFlow()

    private val _searchResult = MutableStateFlow(listOf<ContactSearchKey>())
    val searchResult = _searchResult.asStateFlow()

    private val changeListener = IItemListChangeListener {
        val rosterContacts = mutableListOf<ContactSearchKey>()
        val groups = mutableListOf<ContactSearchKey>()
        val phoneBookContacts = mutableListOf<ContactSearchKey>()
        val companyContacts = mutableListOf<ContactSearchKey>()
        val localContacts = mutableListOf<ContactSearchKey>()
        val rainbowContacts = mutableListOf<ContactSearchKey>()

        RainbowSdk().search().results.copyOfDataList.forEach {
            if (it is IRainbowContact) {
                when {
                    it.isRoster -> rosterContacts.add(ContactSearchKey.RosterContact(it))
                    it.isLocalContact -> localContacts.add(ContactSearchKey.LocalContact(it))
                    it.isPhoneBook -> phoneBookContacts.add(ContactSearchKey.PhoneBookContact(it))
                    it.companyId == RainbowSdk().user().getConnectedUser().companyId -> companyContacts.add(ContactSearchKey.CompanyContact(it))
                    it.isRainbowUser -> rainbowContacts.add(ContactSearchKey.RainbowContact(it))
                }
            } else if (it is Group) {
                groups.add(ContactSearchKey.GroupWithContacts(it))
            }
        }

        val data = mutableListOf<ContactSearchKey>()

        if (rosterContacts.isNotEmpty()) {
            data += ContactSearchKey.Header(ContactSearchSectionKey.ROSTER)
            data += rosterContacts
        }

        if (groups.isNotEmpty()) {
            data += ContactSearchKey.Header(ContactSearchSectionKey.GROUPS)
            data += groups
        }

        if (phoneBookContacts.isNotEmpty()) {
            data += ContactSearchKey.Header(ContactSearchSectionKey.PHONE_BOOK)
            data += phoneBookContacts
        }

        if (companyContacts.isNotEmpty()) {
            data += ContactSearchKey.Header(ContactSearchSectionKey.COMPANY)
            data += companyContacts
        }

        if (localContacts.isNotEmpty()) {
            data += ContactSearchKey.Header(ContactSearchSectionKey.LOCAL)
            data += localContacts
        }

        if (rainbowContacts.isNotEmpty()) {
            data += ContactSearchKey.Header(ContactSearchSectionKey.RAINBOW_CONTACTS)
            data += rainbowContacts
        }

        _searchResult.update { data }
    }

    init {
        RainbowSdk().search().results.registerChangeListener(changeListener)
    }

    override fun onCleared() {
        RainbowSdk().search().results.unregisterChangeListener(changeListener)
    }

    fun search(str: String) {
        if (str.isEmpty()) return

        val searchCriteria = SearchCriteria.Builder()
            .setSearchRainbowContacts(true)
            .setSearchCompanyDirectoryContacts(true)
            .setSearchOtherCompaniesDirectoryContacts(true)
            .setSearchLocalContacts(true)
            .setSearchGroups(true)
            .setSearchPhoneBook(true)
            .build()

        // Search method already delays the query
        RainbowSdk().search().search(str, searchCriteria, object : Search.ISearchListener<List<Any>> {
            override fun onSearchStarted() {
                _searchLoading.update { true }
            }

            override fun onSuccess(data: List<Any>) {
                _searchLoading.update { false }
            }

            override fun onError(error: RainbowError<Unit>) {
                _searchLoading.update { false }
            }
        })
    }
}