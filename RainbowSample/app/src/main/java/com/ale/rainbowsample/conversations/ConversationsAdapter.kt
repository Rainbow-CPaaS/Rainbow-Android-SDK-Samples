package com.ale.rainbowsample.conversations

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.infra.manager.IMMessage
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.infra.xmpp.packetextension.room.RoomMultiUserChatEventExtension
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.ConversationAdapterItemBinding
import com.ale.rainbowsample.databinding.FavoriteListAdapterItemBinding
import com.ale.rainbowsample.utils.HorizontalMarginItemDecoration
import com.ale.rainbowsample.utils.toShortFormat
import com.ale.rainbowsdk.RainbowSdk
import com.ale.rainbowx.conferencerecyclerview.removeAllItemDecorations
import kotlin.math.roundToInt

class ConversationsAdapter : ListAdapter<ConversationsAdapter.ConversationsAdapterItemType, RecyclerView.ViewHolder>(ConversationsDiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ConversationsAdapterItemType.FavoritesItem -> R.layout.favorite_list_adapter_item
            is ConversationsAdapterItemType.ConversationItem -> R.layout.conversation_adapter_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.conversation_adapter_item -> ConversationViewHolder(ConversationAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.favorite_list_adapter_item -> FavoritesViewHolder(FavoriteListAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Not handled")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ConversationViewHolder -> holder.bind(getItem(position) as ConversationsAdapterItemType.ConversationItem)
            is FavoritesViewHolder -> holder.bind(getItem(position) as ConversationsAdapterItemType.FavoritesItem)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationViewHolder)
            holder.addConversationObserver()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationViewHolder)
            holder.removeConversationObserver()

        if (holder is FavoritesViewHolder)
            holder.unregisterChildObservers()
    }

    internal inner class ConversationViewHolder(private val binding: ConversationAdapterItemBinding) : RecyclerView.ViewHolder(binding.root), IRainbowContact.IContactListener {

        val context: Context
            get() = binding.root.context

        private var contact: IRainbowContact? = null
        private val uiHandler = Handler(Looper.getMainLooper())

        fun addConversationObserver() {
            contact?.registerChangeListener(this)
        }

        fun removeConversationObserver() {
            contact?.unregisterChangeListener(this)
        }

        fun bind(data: ConversationsAdapterItemType.ConversationItem) {
            contact = data.conversation.conversation.contact

            displayTitle(data.conversation.conversation)
            displaySubtitle(data.conversation.conversation)
            displayConversationAvatar(data.conversation.conversation)
            displayPresence()
            displayTimeStamp(data.conversation.conversation)
        }

        private fun displayTimeStamp(conversation: IRainbowConversation) {
            binding.timestamp.text = conversation.lastMessage.messageDate.toShortFormat()
        }

        private fun displayTitle(conversation: IRainbowConversation) {
            binding.title.text = conversation.getDisplayName(context.getString(R.string.unknown))
        }

        private fun displaySubtitle(conversation: IRainbowConversation) {
            val lastMessage = conversation.lastMessage
            var lastContent = lastMessage.messageContent ?: ""
            val lastContact = RainbowSdk().contacts().getContactFromJid(lastMessage.contactJid)
            val lastContactName = lastContact?.getDisplayName(context.getString(R.string.unknown)) ?: ""

            binding.subtitle.text = ""

            if (lastMessage.isRoomEventType) {
                displayRoomEventTypeSubtitle(lastMessage, lastContactName, RainbowSdk().contacts().isLoggedInUser(lastContact))
                return
            }

            if (lastMessage.isWebRtcEventType) {
                displayWebRtcEventTypeSubtitle(conversation, lastMessage)
                return
            }

            if (lastContent.isEmpty() || lastContactName.isEmpty()) return

            // Remove prefix "/code" when Rainbow client is sending code message
            lastContent = lastContent.removePrefix("/code")

            when {
                conversation.isChatType -> {
                    if (RainbowSdk().contacts().isLoggedInUser(lastContact)) {
                        binding.subtitle.text = context.getString(R.string.last_message_sent, lastContent)
                    } else {
                        binding.subtitle.text = lastContent
                    }
                }

                conversation.isRoomType -> {
                    if (RainbowSdk().contacts().isLoggedInUser(lastContact)) {
                        binding.subtitle.text = context.getString(R.string.last_message_sent, lastContent)
                    } else {
                        binding.subtitle.text = context.getString(R.string.last_message_received, lastContactName, lastContent)
                    }
                }
            }
        }

        private fun displayWebRtcEventTypeSubtitle(conversation: IRainbowConversation, lastMessage: IMMessage) {
            val conversationContactName = conversation.contact?.getDisplayName(context.getString(R.string.unknown)) ?: ""

            val eventText = when {
                lastMessage.isAnsweredCall && lastMessage.isMsgSent -> context.getString(R.string.you_called, conversationContactName)
                lastMessage.isAnsweredCall && !lastMessage.isMsgSent -> context.getString(R.string.called_you, conversationContactName)
                lastMessage.isMsgSent -> context.getString(R.string.outgoing_call)
                lastMessage.isForwardedMessage -> context.getString(R.string.incoming_call_forwarded)
                else -> context.getString(R.string.missed_call)
            }

            binding.subtitle.text = eventText
        }

        private fun displayRoomEventTypeSubtitle(lastMessage: IMMessage, lastContactName: String, isUserMe: Boolean) {
            val eventText = when (lastMessage.roomEventType) {
                RoomMultiUserChatEventExtension.RoomEventType.CONFERENCEADD -> {
                    if (isUserMe)
                        context.getString(R.string.you_have_started_a_conference)
                    else
                        context.getString(R.string.has_started_a_conference, lastContactName)
                }

                RoomMultiUserChatEventExtension.RoomEventType.CONFERENCEREMOVED -> {
                    if (isUserMe)
                        context.getString(R.string.you_have_ended_the_conference)
                    else
                        context.getString(R.string.has_ended_the_conference, lastContactName)
                }

                else -> ""
            }

            binding.subtitle.text = eventText
        }

        private fun displayConversationAvatar(conversation: IRainbowConversation) {
            when {
                conversation.isRoomType -> {
                    val room = conversation.room ?: return
                    binding.avatar.displayRoom(room)
                }

                conversation.isChatType -> {
                    val contact = conversation.contact ?: return
                    binding.avatar.displayContact(contact)
                }

                else -> binding.avatar.resetImageView()
            }
        }

        private fun displayPresence() {
            binding.avatar.displayPresence(contact)
        }

        override fun contactUpdated(updatedContact: IRainbowContact) {

        }

        override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
            uiHandler.post {
                displayPresence()
            }
        }
    }

    internal inner class FavoritesViewHolder(private val binding: FavoriteListAdapterItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val recyclerView: RecyclerView
            get() = binding.favoritesRecyclerview

        private var childFavoritesAdapter: FavoritesAdapter = FavoritesAdapter()

        fun bind(data: ConversationsAdapterItemType.FavoritesItem) {
            binding.favoritesRecyclerview.adapter = childFavoritesAdapter
            binding.favoritesRecyclerview.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)

            val divider = HorizontalMarginItemDecoration().apply {
                dividerThickness = binding.root.resources.getDimension(R.dimen.small_horizontal_margin).roundToInt()
                isLastItemDecorated = false
            }

            binding.favoritesRecyclerview.removeAllItemDecorations()
            binding.favoritesRecyclerview.addItemDecoration(divider)

            childFavoritesAdapter.submitList(data.favorites)
        }

        fun unregisterChildObservers() {
            binding.favoritesRecyclerview.adapter = null
        }
    }

    private class ConversationsDiffCallBack : DiffUtil.ItemCallback<ConversationsAdapterItemType>() {
        override fun areItemsTheSame(oldItem: ConversationsAdapterItemType, newItem: ConversationsAdapterItemType): Boolean {
            if (oldItem is ConversationsAdapterItemType.ConversationItem && newItem is ConversationsAdapterItemType.ConversationItem) {
                return oldItem.conversation.conversation.id == newItem.conversation.conversation.id
            }

            if (oldItem is ConversationsAdapterItemType.FavoritesItem && newItem is ConversationsAdapterItemType.FavoritesItem) {
                return oldItem.favorites == newItem.favorites
            }

            return false
        }

        override fun areContentsTheSame(oldItem: ConversationsAdapterItemType, newItem: ConversationsAdapterItemType): Boolean {
            if (oldItem is ConversationsAdapterItemType.ConversationItem && newItem is ConversationsAdapterItemType.ConversationItem) {
                return oldItem.conversation.lastMessage.messageId == newItem.conversation.lastMessage.messageId &&
                        oldItem.conversation.displayName == newItem.conversation.displayName &&
                        oldItem.conversation.presence == newItem.conversation.presence
            }

            return false
        }
    }

    sealed class ConversationsAdapterItemType {
        data class FavoritesItem(val favorites: List<FavoriteUiState>) : ConversationsAdapterItemType()
        data class ConversationItem(val conversation: ConversationUiState) : ConversationsAdapterItemType()
    }
}