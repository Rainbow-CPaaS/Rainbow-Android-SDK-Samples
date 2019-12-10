package com.ale.conversations.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ale.conversations.R
import com.ale.conversations.fragments.ContactsFragment
import com.ale.conversations.fragments.ConversationsListFragment

private const val TAB_COUNT = 2

class MainPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = TAB_COUNT

    override fun createFragment(position: Int): Fragment = when(position) {
        0 -> ConversationsListFragment()
        else -> ContactsFragment()
    }

    fun getTitle(position: Int): String = when (position) {
        0 -> fragment.getString(R.string.conversations)
        else -> fragment.getString(R.string.contacts)
    }

}