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
import com.ale.conversations.adapter.MessagesAdapter
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.manager.IMMessage
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.listener.IRainbowImListener
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.android.synthetic.main.conversation_fragment.*

class ConversationFragment(private val conversation: IRainbowConversation) : Fragment(), IRainbowImListener {

    companion object {
        const val NB_MESSAGE_TO_RETRIEVE = 50
    }

    private lateinit var activity: StartupActivity
    private val messages = mutableListOf<IMMessage>()
    private lateinit var messagesRecycler: RecyclerView
    private lateinit var swipeRefresh : SwipeRefreshLayout
    private var scrollToBottom = true
    private lateinit var messagesAdapter: MessagesAdapter

    private val changeListener = IItemListChangeListener(::getMessages)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
        messagesAdapter = MessagesAdapter(messages, activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (!conversation.isRoomType) {
            activity.supportActionBar?.show()
            activity.supportActionBar?.title = conversation.contact.getDisplayName("Unknown")
        }

        // Register listener for messages changed in conversation
        // Do not register listener with lambda (memory leaks)
        // conversation.messages.registerListener { changeListener }
        conversation.messages.registerChangeListener(changeListener)

        // Register IRainbowImListener
        RainbowSdk.instance().im().registerListener(this)

        return inflater.inflate(R.layout.conversation_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messagesRecycler = messages_recycler
        swipeRefresh = swipe_messages

        messagesRecycler.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = messagesAdapter
        }

        swipeRefresh.setOnRefreshListener { retrieveMoreMessages() }

        send_button.setOnClickListener { sendMessage() }

        edit_text.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                // When edit text is not empty, send isTyping state
                if (s.isNotEmpty())
                    RainbowSdk.instance().im().sendIsTyping(conversation, true)
                else
                    RainbowSdk.instance().im().sendIsTyping(conversation, false)
            }
        })

        if (conversation.isRoomType && conversation.room.isRoomArchived) {
            archived_bubble.visibility = View.VISIBLE
        }

        retrieveMessages()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Do not forget to unregister listeners to avoid memory leaks
        conversation.messages.unregisterChangeListener(changeListener)
        RainbowSdk.instance().im().unregisterListener(this)
    }

    private fun retrieveMoreMessages() {
        scrollToBottom = false
        RainbowSdk.instance().im().getMoreMessagesFromConversation(conversation, NB_MESSAGE_TO_RETRIEVE)
    }

    private fun retrieveMessages() = RainbowSdk.instance().im().getMessagesFromConversation(conversation, NB_MESSAGE_TO_RETRIEVE)

    private fun getMessages() {
        messages.clear()
        messages.addAll(conversation.messages.copyOfDataList)

        activity.runOnUiThread {
            messagesRecycler.adapter?.notifyDataSetChanged()
            if (scrollToBottom)
                messagesRecycler.scrollToPosition(messagesAdapter.itemCount - 1)
            swipeRefresh.isRefreshing = false
            scrollToBottom = true
        }
    }

    private fun sendMessage() {
        // Send message to conversation
        RainbowSdk.instance().im().sendMessageToConversation(conversation, edit_text.text.toString())
        edit_text.setText("")
    }

    // This method is called when your message has been sent
    override fun onImSent(conversationId: String?, message: IMMessage?) {
    }

    // This method is called when a new message has been received
    override fun onImReceived(conversationId: String?, message: IMMessage?) {
        if (!conversation.isRoomType) {
            RainbowSdk.instance().im().markMessageFromConversationAsRead(conversation, message)
            activity.runOnUiThread { messagesAdapter.notifyDataSetChanged() }
        }
    }

    // This method is called after getMessages, It's better to use IItemListChangeListener
    override fun onMessagesListUpdated(p0: Int, p1: String?, p2: MutableList<IMMessage>?) {
    }

    // This method is called when the contact send a typing state
    override fun isTypingState(contact: IRainbowContact?, isTyping: Boolean, bubbleID: String?) {
        if (isTyping) {
            activity.runOnUiThread {
                typing_text.text = getString(R.string.typing, contact?.getDisplayName("Unknown"))
                typing_text.visibility = View.VISIBLE
            }
        } else {
            activity.runOnUiThread {
                typing_text.visibility = View.GONE
            }
        }
    }

    // This method is called after getMoreMessageFromConversation, It's better to use IItemListChangeListener
    override fun onMoreMessagesListUpdated(p0: Int, p1: String?, p2: MutableList<IMMessage>?) {
    }
}