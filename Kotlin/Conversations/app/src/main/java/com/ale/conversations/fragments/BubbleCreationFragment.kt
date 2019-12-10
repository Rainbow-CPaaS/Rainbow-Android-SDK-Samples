package com.ale.conversations.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.adapter.ContactsAdapter
import com.ale.conversations.extensions.toast
import com.ale.infra.contact.Contact
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.manager.room.Room
import com.ale.infra.proxy.room.IRoomProxy
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.android.synthetic.main.bubble_creation_fragment.*
import kotlinx.android.synthetic.main.contacts_fragment.contacts_recycler

class BubbleCreationFragment : Fragment() {

    private lateinit var activity: StartupActivity
    private val contacts = mutableListOf<IRainbowContact>()
    private lateinit var contactsRecycler: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private val participants = mutableListOf<IRainbowContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
        contactsAdapter = ContactsAdapter(contacts, activity, clickListener = ::addContactToBubble)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity.supportActionBar?.show()
        activity.supportActionBar?.title = "Create bubble conversation"

        return inflater.inflate(R.layout.bubble_creation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsRecycler = contacts_recycler
        contactsRecycler.layoutManager = LinearLayoutManager(activity)
        contactsRecycler.adapter = contactsAdapter
        text_contacts.text = getString(R.string.choose_contacts, participants.size)

        create_bubble_button.setOnClickListener { createBubble() }

        getContacts()

    }

    private fun createBubble() {
        val name : String = edit_bubble_name.text.toString()
        val description = edit_bubble_description.text.toString()
        val notifications = input_notifications.isChecked

        if (name.isEmpty()) {
            activity.toast("Please, fill bubble name")
            return
        }

        // Create a new bubble
        RainbowSdk.instance().bubbles().createBubble(name, description, notifications, object: IRoomProxy.IRoomCreationListener {
            override fun onCreationSuccess(room: Room) {
                // If contacts has been selected, add to bubble as participant (send invitation)
                if (participants.isNotEmpty()) {
                    RainbowSdk.instance().bubbles().addParticipantsToBubble(room, participants, object : IRoomProxy.IAddParticipantsListener {
                        // There is a limit of 5000 participants in bubble
                        override fun onMaxParticipantsReached() {
                            activity.toast("Max participants reached")
                            activity.openBubbleConversationFragment(room, true)
                        }

                        override fun onAddParticipantsSuccess() {
                            activity.openBubbleConversationFragment(room, true)
                        }

                        override fun onAddParticipantFailed(p0: Contact?) {
                            activity.toast("Error when adding contact in bubble")
                            activity.openBubbleConversationFragment(room, true)
                        }
                    })
                } else
                    activity.openBubbleConversationFragment(room, true)
            }

            override fun onCreationFailed(error: IRoomProxy.RoomCreationError?) {
                activity.toast("Error when creating bubble")
            }
        })

    }

    private fun getContacts() {
        contacts.clear()
        contacts.addAll(RainbowSdk.instance().contacts().rainbowContacts.copyOfDataList)
        activity.runOnUiThread {
            contactsRecycler.adapter?.notifyDataSetChanged()
        }
    }

    private fun addContactToBubble(contact: IRainbowContact) {
        participants.add(contact)
        contacts.remove(contact)
        activity.runOnUiThread {
            contactsAdapter.notifyDataSetChanged()
            text_contacts.text = getString(R.string.choose_contacts, participants.size)
        }
    }
}