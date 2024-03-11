package com.ale.rainbowsample;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.infra.manager.channel.Channel;
import com.ale.infra.manager.channel.ChannelItem;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.Channels;
import com.ale.rainbowsdk.Im;
import com.ale.rainbowsdk.RainbowSdk;

class NotificationCenterJava implements Channels.IChannelsListener, Im.IRainbowImListener, ITelephonyListener {

   private final String sampleChannelID = "SAMPLE_CHANNEL_ID";
   private final String channelGroupID = "CHANNEL_GROUP_ID";
   private final Context applicationContext;

   public NotificationCenterJava(Context applicationContext) {
      this.applicationContext = applicationContext;
   }

   public void start() {
      NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

      if (notificationManager != null) {
         notificationManager.createNotificationChannel(new NotificationChannel(sampleChannelID, "Default notification", NotificationManager.IMPORTANCE_DEFAULT));
      }

      RainbowSdk.instance().channels().registerChannelsListener(this);
      RainbowSdk.instance().channels().registerChannelsListener(this);
      RainbowSdk.instance().channels().registerChannelsListener(this);
   }

   public void stop() {
      RainbowSdk.instance().channels().unregisterChannelsListener(this);
      RainbowSdk.instance().channels().unregisterChannelsListener(this);
      RainbowSdk.instance().channels().unregisterChannelsListener(this);
   }


   // Display notifications when a channel message is received
   @Override
   public void notifyChannelMessage(@NonNull ChannelItem channelItem, @NonNull Channel channel) {
      IRainbowContact contact = channelItem.getContact();
      String contactName = "Unknown";

      if (contact != null)
         contactName = channelItem.getContact().getDisplayName("Unknown");

      String text = contactName + " has published a new message in the channel";

      Notification notification = new NotificationCompat.Builder(applicationContext, sampleChannelID)
              .setGroup(channelGroupID)
              .setSmallIcon(R.drawable.ic_notifications_black_24dp)
              .setColor(ContextCompat.getColor(applicationContext, R.color.md_theme_primary))
              .setAutoCancel(true)
              .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
              .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
              .setContentTitle(channel.getName())
              .setContentText(text)
              .build();

      // Check whether the notifications permissions have been accepted
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED) {
         NotificationManagerCompat.from(applicationContext).notify(channel.getId().hashCode(), notification);
      }
   }


   @Override
   public void onImReceived(@NonNull IRainbowConversation conversation, IMMessage message) {
      // Add some logic to know if you want to display a notification for this message
   }

   @Override
   public void onCallAdded(WebRTCCall call, boolean secondCall) {
      // Add your logic to display an incoming call notification
   }

   @Override
   public void onCallModified(WebRTCCall call, boolean secondCall) {
      // Add your logic to handle call modified
   }

   @Override
   public void onCallRemoved(WebRTCCall call, boolean secondCall) {
      // Add your logic to remove the incoming call notification if necessary
   }
}
