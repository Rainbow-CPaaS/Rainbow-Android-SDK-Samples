package com.ale.rainbowsample

import com.ale.infra.rest.listeners.RainbowError
import com.ale.rainbowsdk.Connection
import com.ale.rainbowsdk.RainbowSdk
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        RainbowSdk().push().onTokenRefresh(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        RainbowSdk().push().onMessageReceived(message.data) {

        }

        super.onMessageReceived(message)
    }
}