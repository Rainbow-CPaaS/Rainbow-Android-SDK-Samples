package com.ale.conversations.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.adapter.ContactsAdapter
import com.ale.conversations.extensions.toast
import com.ale.infra.contact.Contact
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.infra.http.adapter.concurrent.RainbowServiceException
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.proxy.users.IUserProxy
import com.ale.listener.IRainbowContactsSearchListener
import com.ale.listener.IRainbowSentInvitationListener
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.android.synthetic.main.contacts_fragment.*


class ContactsFragment : Fragment(), IRainbowContact.IContactListener {

    private lateinit var activity: StartupActivity
    private val contacts = mutableListOf<IRainbowContact>()
    private lateinit var contactsRecycler: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var swipeRefresh : SwipeRefreshLayout

    // Listener on contacts
    private val changeListener = IItemListChangeListener(::getContacts)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
        contactsAdapter = ContactsAdapter(contacts, activity, ::addOrRemoveContactToRoster, ::contactClicked)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Register listener for contacts list changed
        RainbowSdk.instance().contacts().rainbowContacts.registerChangeListener(changeListener)

        return inflater.inflate(R.layout.contacts_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsRecycler = contacts_recycler
        swipeRefresh = swipe_contacts

        contactsRecycler.layoutManager = LinearLayoutManager(activity)
        contactsRecycler.adapter = contactsAdapter

        swipeRefresh.setOnRefreshListener { getContacts() }

        edit_search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // When length is >= 3 we are searching users
                if (s.length >= 3) {
                    RainbowSdk.instance().contacts().searchByName(s.toString(), object: IRainbowContactsSearchListener {
                        // Unregister all listeners to contacts before clearing the list
                        override fun searchStarted() {
                            unregisterListeners()
                        }

                        override fun searchError(p0: RainbowServiceException?) {
                            activity.toast("Error while searching contact")
                        }

                        // Method called when search has finished
                        override fun searchFinished(contactsFounded: MutableList<Contact>) {
                            contacts.clear()
                            if (contactsFounded.isNotEmpty())
                                contacts.addAll(contactsFounded as MutableList<IRainbowContact>)

                            activity.runOnUiThread { contactsAdapter.notifyDataSetChanged() }
                        }
                    })
                } else
                    getContacts()
            }
        })
        getContacts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterListeners()
        RainbowSdk.instance().contacts().rainbowContacts.unregisterChangeListener(changeListener)
    }

    private fun addOrRemoveContactToRoster(contact: IRainbowContact, add: Boolean) = when (add) {
        // Remove contact from roster
        false -> RainbowSdk.instance().contacts().removeContactFromRoster(contact.id, object : IUserProxy.IContactRemovedFromRosterListener {
            override fun onSuccess() {
                activity.runOnUiThread { contactsAdapter.notifyDataSetChanged() }
            }

            override fun onFailure(p0: RainbowServiceException?) {
                activity.toast("Error adding contact to roster")
            }
        })
        // Add contact to roster (send an invitation)
        else -> RainbowSdk.instance().contacts().addRainbowContactToRoster(contact, object : IRainbowSentInvitationListener {
            override fun onInvitationError() {
                activity.toast("Error sending invitation to contact")
            }

            override fun onInvitationSentError(rbServiceException: RainbowServiceException) {
                activity.toast("Error sending invitation to contact")
            }

            override fun onInvitationSentSuccess() {
                activity.toast("Invitation sent")
                activity.runOnUiThread { contactsAdapter.notifyDataSetChanged() }
            }
        })
    }

    private fun getContacts() {
        unregisterListeners()
        contacts.clear()
        contacts.addAll(RainbowSdk.instance().contacts().rainbowContacts.copyOfDataList)
        registerListeners()

        activity.runOnUiThread {
            contactsRecycler.adapter?.notifyDataSetChanged()
            swipeRefresh.isRefreshing = false
        }
    }

    // Unregister listener to all contacts
    private fun unregisterListeners() {
        for (contact in contacts) {
            contact.unregisterChangeListener(this)
        }
    }

    // Register listener for all contacts
    private fun registerListeners() {
        for (contact in contacts) {
            contact.registerChangeListener(this)
        }
    }

    // Open conversation when clicking on contact row
    private fun contactClicked(contact: IRainbowContact) {
        activity.openOneToOneConversationFragment(RainbowSdk.instance().conversations().getConversationFromContact(contact.jid))
    }

    // IContactListener - Method called when company has been changed for a contact
    override fun onCompanyChanged(contact: String?) {
        activity.runOnUiThread { contactsAdapter.notifyDataSetChanged() }
    }

    // IContactListener - Method called when presence has been changed for a contact
    override fun onPresenceChanged(p0: IRainbowContact?, p1: RainbowPresence?) {
        activity.runOnUiThread { contactsAdapter.notifyDataSetChanged() }
    }

    // IContactListener -
    override fun onActionInProgress(actionInProgress: Boolean) {
    }

    // IContactListener - Method called when contact has been updated
    override fun contactUpdated(contact: IRainbowContact?) {
    }
}