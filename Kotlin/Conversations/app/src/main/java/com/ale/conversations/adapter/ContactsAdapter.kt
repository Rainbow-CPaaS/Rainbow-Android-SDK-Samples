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
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence.*
import kotlinx.android.synthetic.main.contacts_row.view.*

class ContactsAdapter(private var items: List<IRainbowContact>,
                      private val activity: StartupActivity,
                      val clickRosterListener: ((IRainbowContact, Boolean) -> Unit)? = null,
                      val clickListener: ((IRainbowContact) -> Unit)? = null) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    private val contactsPictureCache = mutableMapOf<String, Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(activity).inflate(R.layout.contacts_row, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        if (position < items.size)
            holder.bind(items[position])
    }

    inner class ContactViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: IRainbowContact) {
            val name = contact.getDisplayName("Unknown")

            if (!contactsPictureCache.containsKey(contact.jid))
                contactsPictureCache[contact.jid] = contact.getPictureForRainbowContact(activity)

            itemView.contact_avatar.setImageBitmap(contactsPictureCache[contact.jid])
            itemView.contact_display_name.text = name
            itemView.contact_enterprise_name.text = contact.companyName

            val color = when (contact.presence) {
                AWAY, MANUAL_AWAY -> Color.YELLOW
                MOBILE_ONLINE -> Color.BLUE
                ONLINE -> Color.GREEN
                XA, OFFLINE, UNSUBSCRIBED -> Color.GRAY
                else -> Color.RED
            }

            itemView.contact_presence.setBackgroundColor(color)
            itemView.setOnClickListener { clickListener?.invoke(contact) }
            itemView.add_to_roster.setOnClickListener { clickRosterListener?.invoke(contact, true) }
            itemView.remove_from_roster.setOnClickListener { clickRosterListener?.invoke(contact, false) }

            if (clickRosterListener != null) {
                if (contact.presence == UNSUBSCRIBED) {
                    itemView.add_to_roster.visibility = View.VISIBLE
                    itemView.remove_from_roster.visibility = View.GONE

                } else {
                    itemView.remove_from_roster.visibility = View.VISIBLE
                    itemView.add_to_roster.visibility = View.GONE
                }
            }
        }
    }
}