package com.ale.push

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ale.infra.manager.call.ITelephonyListener
import com.ale.infra.manager.call.WebRTCCall
import com.ale.rainbowsdk.RainbowSdk

/**
 * This class is used to display a simple notification when an incoming call is received.
 */
class TelephonyNotificationMgr {

    companion object {
        private const val CHANNEL_ID = "com.ale.push_telephony_id"

    }

    // Listen for changes and display a notification when an incoming call is received
    private val telephonyListener = object : ITelephonyListener {
        override fun onCallAdded(call: WebRTCCall?, secondCall: Boolean) {
            createNotification(call, secondCall)
        }

        override fun onCallModified(call: WebRTCCall?, secondCall: Boolean) {

        }

        // Dismiss the notification when the call is removed
        override fun onCallRemoved(call: WebRTCCall?, secondCall: Boolean) {
            if (call == null) return
            NotificationManagerCompat.from(MyApplication.instance).cancel(call.jid.hashCode())
        }
    }

    init {
        // Register listener
        RainbowSdk.instance().webRTC().registerTelephonyListener(telephonyListener)

        val channel = NotificationChannel(CHANNEL_ID , "Telephony channel", NotificationManager.IMPORTANCE_HIGH)
        NotificationManagerCompat.from(MyApplication.instance).createNotificationChannel(channel)
    }

    private fun createNotification(call: WebRTCCall?, secondCall: Boolean) {
        if (call == null) return

        val displayName = call.distant?.getDisplayName("Unknown") ?: "Unknown"

        val notificationBuilder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_phone_callback_24)
            .setColor(MyApplication.instance.resources.getColor(R.color.purple_500, null))
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentTitle(MyApplication.instance.resources.getString(R.string.incoming_call))
            .setContentText(MyApplication.instance.resources.getString(R.string.from, displayName))

        val notification = notificationBuilder.build()

        NotificationManagerCompat.from(MyApplication.instance).notify(call.jid.hashCode(), notification)
    }

    fun stop() {
        // Unregister listener to avoid memory leak
        RainbowSdk.instance().webRTC().unregisterTelephonyListener(telephonyListener)
    }
}