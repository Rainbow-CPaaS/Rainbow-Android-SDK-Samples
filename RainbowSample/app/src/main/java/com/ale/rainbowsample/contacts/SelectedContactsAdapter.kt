package com.ale.rainbowsample.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ale.infra.contact.IRainbowContact
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.ContactSelectionRowBinding

class SelectedContactsAdapter(
    val onItemClick: ((IRainbowContact) -> Unit)? = null,
) : androidx.recyclerview.widget.ListAdapter<ContactUiState, RecyclerView.ViewHolder>(ContactsDiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return R.layout.contact_selection_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.contact_selection_row -> SelectedContactViewHolder(ContactSelectionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Not handled")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SelectedContactViewHolder -> holder.bind(getItem(position) as ContactUiState)
        }
    }

    internal inner class SelectedContactViewHolder(private val binding: ContactSelectionRowBinding) : RecyclerView.ViewHolder(binding.root) {

        val context: Context
            get() = binding.root.context

        fun bind(data: ContactUiState) {
            onItemClick?.let { binding.layout.setOnClickListener { it(data.contact) } }
            binding.name.text = data.contact.getDisplayName(context.getString(R.string.unknown))
            binding.avatar.displayContact(data.contact)
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