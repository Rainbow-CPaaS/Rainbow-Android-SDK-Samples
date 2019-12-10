package com.ale.conversations.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ale.conversations.R
import com.ale.conversations.extensions.transaction
import com.ale.conversations.fragments.*
import com.ale.infra.manager.room.Room
import com.ale.infra.proxy.conversation.IRainbowConversation
import kotlinx.android.synthetic.main.activity_main.*

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openLoginFragment()
        setSupportActionBar(app_toolbar)
    }

    fun openLoginFragment() = supportFragmentManager.transaction {
        replace(R.id.fragment_container, LoginFragment())
    }

    fun openSliderFragment() = supportFragmentManager.transaction {
        replace(R.id.fragment_container, MainPagerFragment())
    }

    fun openBubbleConversationFragment(room: Room, popBackStack: Boolean = false) = supportFragmentManager.transaction(addToBackStack = true, popToBackStack = popBackStack) {
        replace(R.id.fragment_container, BubbleConversationPagerFragment(room))
    }

    fun openOneToOneConversationFragment(conversation: IRainbowConversation) = supportFragmentManager.transaction(addToBackStack = true) {
        replace(R.id.fragment_container, ConversationFragment(conversation))
    }

    fun openBubbleCreationFragment() = supportFragmentManager.transaction(addToBackStack = true) {
        replace(R.id.fragment_container, BubbleCreationFragment())
    }
}
