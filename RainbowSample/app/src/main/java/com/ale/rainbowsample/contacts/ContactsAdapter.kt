package com.ale.rainbowsample.contacts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.contact.RainbowPresence
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.ContactAdapterItemBinding
import com.ale.rainbowsample.utils.presenceAsString

class ContactsAdapter : androidx.recyclerview.widget.ListAdapter<ContactUiState, RecyclerView.ViewHolder>(ContactsDiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.contact_adapter_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.contact_adapter_item -> ContactViewHolder(ContactAdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Not handled")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContactViewHolder -> holder.bind(getItem(position) as ContactUiState)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ContactViewHolder)
            holder.addContactObserver()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ContactViewHolder)
            holder.removeContactObserver()
    }

    internal inner class ContactViewHolder(private val binding: ContactAdapterItemBinding) : RecyclerView.ViewHolder(binding.root), IRainbowContact.IContactListener {

        val context: Context
            get() = binding.root.context

        private lateinit var contact: IRainbowContact
        private val uiHandler = Handler(Looper.getMainLooper())

        fun bind(data: ContactUiState) {
            contact = data.contact
            updateLayout()
        }

        private fun updateLayout() {
            binding.title.text = contact.getDisplayName(context.getString(R.string.unknown))
            binding.avatar.displayContact(contact)
            binding.avatar.displayPresence(contact)
            binding.subtitle.text = contact.presenceAsString(context)
        }

        override fun contactUpdated(updatedContact: IRainbowContact) {
            contact = updatedContact
            uiHandler.post { updateLayout() }
        }

        override fun onPresenceChanged(contact: IRainbowContact, presence: RainbowPresence?) {
            uiHandler.post { updateLayout() }
        }

        fun addContactObserver() {
            contact.registerChangeListener(this)
        }

        fun removeContactObserver() {
            contact.unregisterChangeListener(this)
        }
    }

    private class ContactsDiffCallBack : DiffUtil.ItemCallback<ContactUiState>() {
        override fun areItemsTheSame(oldItem: ContactUiState, newItem: ContactUiState): Boolean {
            return oldItem.contact.uniqueId == newItem.contact.uniqueId
        }

        override fun areContentsTheSame(oldItem: ContactUiState, newItem: ContactUiState): Boolean {
            return oldItem.contact.getDisplayName("") == newItem.contact.getDisplayName("")
        }
    }
}