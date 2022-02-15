package com.ale.push

import com.ale.rainbowsdk.Push.IMessageReceivedListener
import com.ale.rainbowsdk.RainbowSdk
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationPushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        RainbowSdk.instance().push().onTokenRefresh(token);
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // The IMessageReceivedListener replaces the boolean of older sdk versions. Method onApplicationNotStarted is called if a login is required
        RainbowSdk.instance().push().onMessageReceived(message.data, object : IMessageReceivedListener {
            override fun onApplicationNotStarted() {
                MyApplication.instance.startSilently {
                    // When the application is launched and the user is logged in, the onMessageReceived() method is called again. This time it can handle the entire push message
                    RainbowSdk.instance().push().onMessageReceived(message.data, null)
                }
            }
        })
    }
}