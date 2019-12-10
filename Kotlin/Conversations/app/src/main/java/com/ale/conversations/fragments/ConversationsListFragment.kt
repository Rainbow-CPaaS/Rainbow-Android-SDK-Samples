package com.ale.conversations.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.adapter.ConversationsAdapter
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.android.synthetic.main.conversations_list_fragment.*

class ConversationsListFragment : Fragment() {

    private lateinit var activity: StartupActivity
    private val conversations = mutableListOf<IRainbowConversation>()
    private lateinit var conversationsRecycler: RecyclerView
    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var swipeRefresh : SwipeRefreshLayout

    private val changeListener = IItemListChangeListener(::getConversations)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
        conversationsAdapter = ConversationsAdapter(conversations, activity) { conversation: IRainbowConversation -> conversationClicked(conversation) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Register listener for conversations change
        RainbowSdk.instance().conversations().allConversations.registerChangeListener(changeListener)
        return inflater.inflate(R.layout.conversations_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        create_bubble_conversation.drawable.mutate().setTint(Color.WHITE)
        create_bubble_conversation.setOnClickListener{ activity.openBubbleCreationFragment() }

        conversationsRecycler = conversations_recycler
        swipeRefresh = swipe_conversations

        conversationsRecycler.layoutManager = LinearLayoutManager(activity)
        conversationsRecycler.adapter = conversationsAdapter

        swipeRefresh.setOnRefreshListener { getConversations() }
        getConversations()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Do not forget to unregister listener
        RainbowSdk.instance().conversations().allConversations.unregisterChangeListener(changeListener)
    }

    private fun getConversations() {
        conversations.clear()
        conversations.addAll(RainbowSdk.instance().conversations().allConversations.copyOfDataList)

        activity.runOnUiThread {
            conversationsRecycler.adapter?.notifyDataSetChanged()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun conversationClicked(conversation: IRainbowConversation) {
        if (conversation.isRoomType) activity.openBubbleConversationFragment(conversation.room)
        else activity.openOneToOneConversationFragment(conversation)
    }
}