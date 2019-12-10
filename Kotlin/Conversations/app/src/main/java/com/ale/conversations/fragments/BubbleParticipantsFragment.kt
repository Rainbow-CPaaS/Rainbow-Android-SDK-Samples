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
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.manager.room.Room
import com.ale.infra.manager.room.RoomParticipant
import com.ale.infra.manager.room.RoomStatus
import com.ale.infra.proxy.room.IRoomProxy
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.android.synthetic.main.bubble_participants.*

class BubbleParticipantsFragment(private val room: Room) : Fragment() {

    private lateinit var activity: StartupActivity
    private lateinit var moderatorsAdapter: ContactsAdapter
    private lateinit var membersAdapter: ContactsAdapter
    private lateinit var invitedAdapter: ContactsAdapter
    private lateinit var moderatorsRecycler: RecyclerView
    private lateinit var membersRecycler: RecyclerView
    private lateinit var invitedRecycler: RecyclerView
    private val moderators = mutableListOf<IRainbowContact>()
    private val members = mutableListOf<IRainbowContact>()
    private val invited = mutableListOf<IRainbowContact>()
    
    // Listener on bubble change (contact accept to join bubble...)
    private val bubbleChangeListener = object : Room.RoomListener {
        override fun roomUpdated(bubble: Room?) {
            initializeParticipants()
            activity.runOnUiThread {
                moderatorsAdapter.notifyDataSetChanged()
                membersAdapter.notifyDataSetChanged()
                invitedAdapter.notifyDataSetChanged()
            }
        }

        override fun conferenceUpdated(bubble: Room?) {
            activity.toast("Conference updated")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
        initializeParticipants()
        moderatorsAdapter = ContactsAdapter(moderators, activity)
        membersAdapter = ContactsAdapter(members, activity)
        invitedAdapter = ContactsAdapter(invited, activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Register listener
        room.registerChangeListener(bubbleChangeListener)
        return inflater.inflate(R.layout.bubble_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moderatorsRecycler = recyclerview_moderators
        membersRecycler = recyclerview_members
        invitedRecycler = recyclerview_invited

        moderatorsRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = moderatorsAdapter
        }

        membersRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = membersAdapter
        }

        invitedRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = invitedAdapter
        }

        if (isModerator()) {
            if (!room.isRoomArchived)
                btn_archive.visibility = View.VISIBLE

            btn_delete.visibility = View.VISIBLE

            btn_archive.setOnClickListener { archiveBubble() }
            btn_delete.setOnClickListener { deleteBubble() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't forget to unregister listener
        room.unregisterChangeListener(bubbleChangeListener)
    }

    private fun initializeParticipants() {
        moderators.clear()
        members.clear()
        invited.clear()
        moderators.addAll(room.participants.copyOfDataList.filter { it.isModerator }.map { it.contact })
        members.addAll(room.participants.copyOfDataList.filter { !it.isModerator && it.status == RoomStatus.ACCEPTED }.map { it.contact })
        invited.addAll(room.participants.copyOfDataList.filter { it.status == RoomStatus.INVITED }.map { it.contact })
    }

    // Display archive and delete button if connected user is moderator of bubble
    private fun isModerator() : Boolean {
        return room.participants.copyOfDataList.any {
            it.isModerator && it.contact.id == RainbowSdk.instance().myProfile().connectedUser.id
        }
    }

    private fun deleteBubble() {
        RainbowSdk.instance().bubbles().deleteBubble(room, object : IRoomProxy.IDeleteRoomListener {
            override fun onRoomDeletedSuccess() {
                activity.toast("Bubble deleted")
                activity.openSliderFragment()
            }

            override fun onRoomDeletedFailed() {
                activity.toast("Failed to delete bubble")
            }
        })
    }

    // A bubble archived is still readable but users can no longer write messages
    private fun archiveBubble() {
        RainbowSdk.instance().bubbles().archiveBubble(room, object : IRoomProxy.IChangeUserRoomDataListener {
            override fun onChangeUserRoomDataSuccess(roomParticipant: RoomParticipant) {
                activity.toast("Bubble archived")
                activity.runOnUiThread {
                    btn_archive.visibility = View.INVISIBLE
                }
            }

            override fun onChangeUserRoomDataFailed() {
                activity.toast("Failed to archive bubble")
            }
        })
    }
}