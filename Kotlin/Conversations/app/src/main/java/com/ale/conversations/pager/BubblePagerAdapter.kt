package com.ale.conversations.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ale.conversations.R
import com.ale.conversations.fragments.BubbleConversationPagerFragment
import com.ale.conversations.fragments.BubbleParticipantsFragment
import com.ale.conversations.fragments.ConversationFragment
import com.ale.rainbowsdk.RainbowSdk

private const val TAB_COUNT = 2

class BubblePagerAdapter(val fragment: BubbleConversationPagerFragment) : FragmentStateAdapter(fragment){

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ConversationFragment(RainbowSdk.instance().conversations().getConversationFromRoom(fragment.room))
        else -> BubbleParticipantsFragment(fragment.room)
    }

    fun getTitle(position: Int): String = when (position) {
        0 -> fragment.getString(R.string.conversation)
        else -> fragment.getString(R.string.participants)
    }

}