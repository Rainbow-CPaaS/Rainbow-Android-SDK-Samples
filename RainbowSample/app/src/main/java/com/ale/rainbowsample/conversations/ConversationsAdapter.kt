package com.ale.rainbowsample.conversations

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.infra.manager.IMMessage
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.infra.xmpp.packetextension.room.RoomMultiUserChatEventExtension
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.ConversationAdapterItemBinding
import com.ale.rainbowsample.utils.toShortFormat
import com.ale.rainbowsdk.RainbowSdk

class ConversationsAdapter : ListAdapter<ConversationUiState, RecyclerView.ViewHolder>(ConversationsDiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.conversation_adapter_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.conversation_adapter_item -> ConversationViewHolder(ConversationAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Not handled")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ConversationViewHolder -> holder.bind(getItem(position) as ConversationUiState)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationViewHolder)
            holder.addConversationObserver()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationViewHolder)
            holder.removeConversationObserver()
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

        fun bind(data: ConversationUiState) {
            contact = data.conversation.contact

            displayTitle(data.conversation)
            displaySubtitle(data.conversation)
            displayConversationAvatar(data.conversation)
            displayPresence()
            displayTimeStamp(data.conversation)
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

    private class ConversationsDiffCallBack : DiffUtil.ItemCallback<ConversationUiState>() {
        override fun areItemsTheSame(oldItem: ConversationUiState, newItem: ConversationUiState): Boolean {
            return oldItem.conversation.id == newItem.conversation.id
        }

        override fun areContentsTheSame(oldItem: ConversationUiState, newItem: ConversationUiState): Boolean {
            return oldItem.lastMessage.messageId == newItem.lastMessage.messageId &&
                    oldItem.displayName == newItem.displayName &&
                    oldItem.presence == newItem.presence
        }
    }
}