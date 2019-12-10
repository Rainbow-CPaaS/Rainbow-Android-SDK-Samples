package com.ale.conversations.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.extensions.getPictureForRainbowContact
import com.ale.conversations.extensions.getPictureForRoom
import com.ale.infra.proxy.conversation.IRainbowConversation
import kotlinx.android.synthetic.main.conversations_row.view.*

class ConversationsAdapter(
    private var items: List<IRainbowConversation>,
    private val activity: StartupActivity,
    val clickListener: (IRainbowConversation) -> Unit) : RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    private val pictureCache = mutableMapOf<String, Bitmap>()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return ConversationViewHolder(LayoutInflater.from(activity).inflate(R.layout.conversations_row, parent, false))
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ConversationViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var name: String = ""

        fun bind(conversation: IRainbowConversation) {
            // Check if it's a bubble or a one-to-one conversation
            name = if (conversation.isRoomType) {
                conversation.room.name
            } else {
                conversation.contact.getDisplayName("Unknown")
            }

            if (!pictureCache.containsKey(conversation.jid)) {

                pictureCache[conversation.jid] = if (conversation.isRoomType)
                    conversation.room.getPictureForRoom(activity)
                else
                    conversation.contact.getPictureForRainbowContact(activity)
            }

            itemView.conversation_title.text = name
            itemView.conversation_picture.setImageBitmap(pictureCache[conversation.jid])
            itemView.conversation_message.text = conversation.lastMessage.messageContent
            itemView.setOnClickListener { clickListener(conversation) }
        }
    }
}

