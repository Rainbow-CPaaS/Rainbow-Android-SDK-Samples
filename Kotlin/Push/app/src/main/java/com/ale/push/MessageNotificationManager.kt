package com.ale.push

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ale.infra.contact.IRainbowContact
import com.ale.infra.manager.IMMessage
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.listener.IRainbowImListener
import com.ale.rainbowsdk.RainbowSdk

/**
 * This class is used to display a simple notification when a P2P message is received.
 */
class MessageNotificationManager {

    companion object {
        private const val CHANNEL_ID = "com.ale.push_message_id"
    }

    // Listen for changes and display a notification when a message is received
    private val imChangeListener: IRainbowImListener = object : IRainbowImListener {
        override fun onImReceived(conversation: IRainbowConversation?, message: IMMessage?) {
            if (conversation == null || message == null) return

            val contact: IRainbowContact? = RainbowSdk.instance().contacts().getContactFromJid(message.contactJid)

            if (message.callLogEvent != null) {
               // Call log notification, to keep the code simple, only P2P messages have been handled
            } else if (contact != null)
                createNotification(conversation, message, contact)
        }
    }

    init {
        // Register listener
        RainbowSdk.instance().im().registerListener(imChangeListener)

        val channel = NotificationChannel(CHANNEL_ID , "Message channel", NotificationManager.IMPORTANCE_HIGH)
        NotificationManagerCompat.from(MyApplication.instance).createNotificationChannel(channel)
    }

    private fun createNotification(conversation: IRainbowConversation, message: IMMessage, contact: IRainbowContact) {
        val notificationBuilder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_chat_24)
            .setColor(MyApplication.instance.resources.getColor(R.color.purple_500, null))
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setNumber(1) // Could be total of unread message
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentTitle(contact.getDisplayName("Unknown"))
            .setContentText(message.messageContent)

        val notification = notificationBuilder.build()

        NotificationManagerCompat.from(MyApplication.instance).notify(conversation.jid.hashCode(), notification)
    }

    fun stop() {
        // Unregister listener to avoid memory leak
        RainbowSdk.instance().im().unregisterListener(imChangeListener)
    }
}