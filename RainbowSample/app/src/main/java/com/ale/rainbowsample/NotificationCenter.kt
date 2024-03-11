package com.ale.rainbowsample

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.ale.infra.manager.IMMessage
import com.ale.infra.manager.call.ITelephonyListener
import com.ale.infra.manager.call.WebRTCCall
import com.ale.infra.manager.channel.Channel
import com.ale.infra.manager.channel.ChannelItem
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.rainbowsdk.Channels
import com.ale.rainbowsdk.Im
import com.ale.rainbowsdk.RainbowSdk

class NotificationCenter(private val applicationContext: Context) : Channels.IChannelsListener, Im.IRainbowImListener, ITelephonyListener {

    private val sampleChannelID = "SAMPLE_CHANNEL_ID"
    private val channelGroupID = "CHANNEL_GROUP_ID"

    fun start() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(NotificationChannel(sampleChannelID, "Default notification", NotificationManager.IMPORTANCE_DEFAULT))

        RainbowSdk().channels().registerChannelsListener(this)
        RainbowSdk().im().registerListener(this)
        RainbowSdk().webRTC().registerTelephonyListener(this)
    }

    fun stop() {
        RainbowSdk().channels().unregisterChannelsListener(this)
        RainbowSdk().im().unregisterListener(this)
        RainbowSdk().webRTC().unregisterTelephonyListener(this)
    }

    // Display notifications when a channel message is received
    override fun notifyChannelMessage(channelItem: ChannelItem, channel: Channel) {
        val contactName = channelItem.contact?.getDisplayName("Unknown") ?: "Unknown"
        val text = "$contactName has published a new message in the channel"

        val notification = NotificationCompat.Builder(applicationContext, sampleChannelID)
            .setGroup(channelGroupID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setColor(ContextCompat.getColor(applicationContext, R.color.md_theme_primary))
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setContentTitle(channel.name)
            .setContentText(text)
            .build()

        // Check whether the notifications permissions have been accepted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(applicationContext).notify(channel.id.hashCode(), notification)
        }
    }

    override fun onImReceived(conversation: IRainbowConversation, message: IMMessage?) {
        // Add some logic to know if you want to display a notification for this message
    }

    override fun onCallAdded(call: WebRTCCall?, secondCall: Boolean) {
        // Add your logic to display an incoming call notification
    }

    override fun onCallModified(call: WebRTCCall?, secondCall: Boolean) {
        // Add your logic to handle call modified
    }

    override fun onCallRemoved(call: WebRTCCall?, secondCall: Boolean) {
        // Add your logic to remove the incoming call notification if necessary
    }
}