package com.ale.conversations.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.extensions.getPictureForRainbowContact
import com.ale.conversations.extensions.prettyFormat
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.manager.IMMessage
import com.ale.infra.manager.IMMessage.DeliveryState
import com.ale.rainbowsdk.RainbowSdk
import kotlinx.android.synthetic.main.message_row_other.view.*

class MessagesAdapter(
    private var items: List<IMMessage>,
    private val activity: StartupActivity) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private val contactsCache = mutableMapOf<String, IRainbowContact>()
    private val contactsPictureCache = mutableMapOf<String, Bitmap>()

    companion object {
        private const val TYPE_ME = 0 // Message has been sent by connected user
        private const val TYPE_OTHER = 1 // Message has been sent by an other contact
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder = when(viewType) {
        TYPE_ME -> MessageViewHolder(LayoutInflater.from(activity).inflate(R.layout.message_row_me, parent, false))
        TYPE_OTHER -> MessageViewHolder(LayoutInflater.from(activity).inflate(R.layout.message_row_other, parent, false))
        else -> throw IllegalArgumentException()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        if (position < items.size) {
            if (!contactsCache.containsKey(items[position].contactJid))
                contactsCache[items[position].contactJid] = RainbowSdk.instance().contacts().getContactFromJabberId(items[position].contactJid)
            holder.bind(items[position])
        }
    }

    override fun getItemViewType(position: Int) = if (items[position].isMsgSent) TYPE_ME else TYPE_OTHER

    inner class MessageViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: IMMessage) {
            itemView.message_content.text = message.messageContent
            itemView.message_time.text = message.messageDate.prettyFormat()

            // Display a check mark depending of message delivery state
            // For more information check [DELIVERY STATE AVAILABLE FOR MESSAGES]
            // in https://hub.openrainbow.com/#/documentation/doc/sdk/android/guides/Make_an_IM_conversation
            when (message.deliveryState) {
                DeliveryState.SENT -> itemView.message_state.setColorFilter(Color.argb(255, 156, 156, 156))
                DeliveryState.READ, DeliveryState.SENT_CLIENT_READ -> itemView.message_state.setColorFilter(Color.argb(255, 255, 255, 255))
                else -> itemView.message_state.setColorFilter(Color.argb(0, 0, 0, 0))
            }

            if (itemViewType == TYPE_OTHER) {

                val contact = contactsCache[message.contactJid]

                if (contact != null) {

                    if (!contactsPictureCache.containsKey(contact.jid))
                        contactsPictureCache[contact.jid] = contact.getPictureForRainbowContact(activity)

                    itemView.contact_avatar_message.visibility = View.VISIBLE
                    itemView.contact_avatar_message.setImageBitmap(contactsPictureCache[message.contactJid])
                }
            }
        }
    }

}