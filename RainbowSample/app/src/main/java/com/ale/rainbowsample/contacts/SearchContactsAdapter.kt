package com.ale.rainbowsample.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ale.rainbowsample.R
import com.ale.rainbowsample.databinding.SearchAdapterHeaderItemBinding
import com.ale.rainbowsample.databinding.SearchContactCompanyItemBinding
import com.ale.rainbowsample.databinding.SearchContactGroupItemBinding
import com.ale.rainbowsample.databinding.SearchContactLocalItemBinding
import com.ale.rainbowsample.databinding.SearchContactPhonebookItemBinding
import com.ale.rainbowsample.databinding.SearchContactRainbowItemBinding
import com.ale.rainbowsample.databinding.SearchContactRosterItemBinding
import com.ale.rainbowsample.search.ContactSearchKey
import com.ale.rainbowsample.search.ContactSearchSectionKey
import com.ale.rainbowsample.utils.presenceAsString

class SearchContactsAdapter : ListAdapter<ContactSearchKey, RecyclerView.ViewHolder>(SearchContactsDiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is ContactSearchKey.Header -> R.layout.search_adapter_header_item
            is ContactSearchKey.RosterContact ->  R.layout.search_contact_roster_item
            is ContactSearchKey.GroupWithContacts ->  R.layout.search_contact_group_item
            is ContactSearchKey.CompanyContact -> R.layout.search_contact_company_item
            is ContactSearchKey.LocalContact -> R.layout.search_contact_local_item
            is ContactSearchKey.PhoneBookContact -> R.layout.search_contact_phonebook_item
            is ContactSearchKey.RainbowContact -> R.layout.search_contact_rainbow_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.search_adapter_header_item -> SearchHeaderViewHolder(SearchAdapterHeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.search_contact_roster_item -> SearchRosterContactViewHolder(SearchContactRosterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.search_contact_group_item -> SearchGroupContactViewHolder(SearchContactGroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.search_contact_company_item -> SearchCompanyContactViewHolder(SearchContactCompanyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.search_contact_local_item -> SearchLocalContactViewHolder(SearchContactLocalItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.search_contact_phonebook_item -> SearchPhoneBookContactViewHolder(SearchContactPhonebookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            R.layout.search_contact_rainbow_item -> SearchContactViewHolder(SearchContactRainbowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException("Not handled")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchHeaderViewHolder -> holder.bind(getItem(position) as ContactSearchKey.Header)
            is SearchRosterContactViewHolder -> holder.bind(getItem(position) as ContactSearchKey.RosterContact)
            is SearchGroupContactViewHolder -> holder.bind(getItem(position) as ContactSearchKey.GroupWithContacts)
            is SearchCompanyContactViewHolder -> holder.bind(getItem(position) as ContactSearchKey.CompanyContact)
            is SearchLocalContactViewHolder -> holder.bind(getItem(position) as ContactSearchKey.LocalContact)
            is SearchPhoneBookContactViewHolder -> holder.bind(getItem(position) as ContactSearchKey.PhoneBookContact)
            is SearchContactViewHolder -> holder.bind(getItem(position) as ContactSearchKey.RainbowContact)
        }
    }


    private class SearchContactsDiffCallBack : DiffUtil.ItemCallback<ContactSearchKey>() {
        override fun areItemsTheSame(oldItem: ContactSearchKey, newItem: ContactSearchKey): Boolean {
            return when {
                newItem is ContactSearchKey.RainbowContact && oldItem is ContactSearchKey.RainbowContact -> { newItem.contact.id == oldItem.contact.id }
                newItem is ContactSearchKey.RosterContact && oldItem is ContactSearchKey.RosterContact -> { newItem.contact.id == oldItem.contact.id }
                newItem is ContactSearchKey.CompanyContact && oldItem is ContactSearchKey.CompanyContact -> { newItem.contact.id == oldItem.contact.id }
                newItem is ContactSearchKey.LocalContact && oldItem is ContactSearchKey.LocalContact -> { newItem.contact.uniqueId == oldItem.contact.uniqueId }
                newItem is ContactSearchKey.PhoneBookContact && oldItem is ContactSearchKey.PhoneBookContact -> { newItem.contact.uniqueId == oldItem.contact.uniqueId }
                newItem is ContactSearchKey.GroupWithContacts && oldItem is ContactSearchKey.GroupWithContacts -> { newItem.group.id == oldItem.group.id }
                newItem is ContactSearchKey.Header && oldItem is ContactSearchKey.Header -> { newItem.sectionKey == oldItem.sectionKey }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ContactSearchKey, newItem: ContactSearchKey): Boolean {
            return when {
                newItem is ContactSearchKey.RainbowContact && oldItem is ContactSearchKey.RainbowContact -> { newItem.contact.getDisplayName("") == oldItem.contact.getDisplayName("") }
                newItem is ContactSearchKey.RosterContact && oldItem is ContactSearchKey.RosterContact -> { newItem.contact.getDisplayName("") == oldItem.contact.getDisplayName("") }
                newItem is ContactSearchKey.CompanyContact && oldItem is ContactSearchKey.CompanyContact -> { newItem.contact.getDisplayName("") == oldItem.contact.getDisplayName("") }
                newItem is ContactSearchKey.LocalContact && oldItem is ContactSearchKey.LocalContact -> { newItem.contact.getDisplayName("") == oldItem.contact.getDisplayName("") }
                newItem is ContactSearchKey.PhoneBookContact && oldItem is ContactSearchKey.PhoneBookContact -> { newItem.contact.getDisplayName("") == oldItem.contact.getDisplayName("") }
                newItem is ContactSearchKey.GroupWithContacts && oldItem is ContactSearchKey.GroupWithContacts -> { newItem.group.getDisplayName("") == oldItem.group.getDisplayName("")}
                newItem is ContactSearchKey.Header && oldItem is ContactSearchKey.Header -> { newItem.sectionKey == oldItem.sectionKey }
                else -> false
            }
        }
    }

    internal inner class SearchHeaderViewHolder(private val binding: SearchAdapterHeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.Header) {
            binding.headerTitle.text = when(data.sectionKey) {
                ContactSearchSectionKey.ROSTER -> context.getString(R.string.network_contact_title)
                ContactSearchSectionKey.GROUPS -> context.getString(R.string.my_contact_groups)
                ContactSearchSectionKey.COMPANY -> context.getString(R.string.my_company_contacts)
                ContactSearchSectionKey.LOCAL -> context.getString(R.string.phone_contacts)
                ContactSearchSectionKey.PHONE_BOOK -> context.getString(R.string.phone_book_contacts)
                ContactSearchSectionKey.RAINBOW_CONTACTS -> context.getString(R.string.rainbow_contacts)
            }
        }
    }

    internal inner class SearchRosterContactViewHolder(private val binding: SearchContactRosterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.RosterContact) {
            binding.title.text = data.contact.getDisplayName(context.getString(R.string.unknown))
            binding.presence.text = data.contact.presenceAsString(context)
            binding.avatar.displayContact(data.contact)
            binding.avatar.displayPresence(data.contact)
        }
    }

    internal inner class SearchGroupContactViewHolder(private val binding: SearchContactGroupItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.GroupWithContacts) {
            binding.title.text = data.group.getDisplayName(context.getString(R.string.unknown))
            binding.subtitle.text = data.group.comment
            binding.avatar.displayGroup(data.group)
        }
    }

    internal inner class SearchCompanyContactViewHolder(private val binding: SearchContactCompanyItemBinding): RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.CompanyContact) {
            binding.title.text = data.contact.getDisplayName(context.getString(R.string.unknown))
            binding.avatar.displayContact(data.contact)
        }
    }

    internal inner class SearchLocalContactViewHolder(private val binding: SearchContactLocalItemBinding): RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.LocalContact) {
            binding.title.text = data.contact.getDisplayName(context.getString(R.string.unknown))
            binding.avatar.displayContact(data.contact)
        }
    }

    internal inner class SearchPhoneBookContactViewHolder(private val binding: SearchContactPhonebookItemBinding): RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.PhoneBookContact) {
            binding.title.text = data.contact.getDisplayName(context.getString(R.string.unknown))
            binding.avatar.displayContact(data.contact)
        }
    }

    internal inner class SearchContactViewHolder(private val binding: SearchContactRainbowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context
            get() = binding.root.context

        fun bind(data: ContactSearchKey.RainbowContact) {
            binding.avatar.displayContact(data.contact)
            binding.title.text = data.contact.getDisplayName(context.getString(R.string.unknown))
            binding.subtitle.text = data.contact.companyName
        }
    }
}