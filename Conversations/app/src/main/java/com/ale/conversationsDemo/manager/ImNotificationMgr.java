package com.ale.conversationsDemo.manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.listener.IRainbowImListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.activity.StartupActivity;

import java.util.List;

/**
 * Created by letrongh on 16/05/2017.
 */

public class ImNotificationMgr implements IRainbowImListener {

    private Context m_context;

    public ImNotificationMgr(Context context) {
        m_context = context;
        RainbowSdk.instance().im().registerListener(this);
    }

    @Override
    public void onImReceived(String conversationId, IMMessage message) {
        IRainbowConversation conversation = RainbowSdk.instance().conversations().getConversationFromId(conversationId);
        if (conversation != null) {
            displayNotification(conversation.getContact(), message);
        }
    }

    @Override
    public void onImSent(String conversationId, IMMessage message) {

    }

    @Override
    public void isTypingState(IRainbowContact other, boolean isTyping, String roomId) {

    }

    @Override
    public void onMessagesListUpdated(int status, String conversationId, List<IMMessage> messages) {

    }

    @Override
    public void onMoreMessagesListUpdated(int status, String conversationId, List<IMMessage> messages) {

    }

    private void displayNotification(IRainbowContact contact, IMMessage message) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(m_context)
                .setSmallIcon(android.support.design.R.drawable.notification_icon_background)
                .setAutoCancel(true)
                .setContentTitle(contact.getLastName() + " " + contact.getFirstName())
                .setContentText(message.getMessageContent());

        Intent resultIntent = new Intent(m_context, StartupActivity.class);
        resultIntent.setAction("displayConversation");
        resultIntent.putExtra("contactId", contact.getContactId());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(m_context);
        stackBuilder.addParentStack(StartupActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager)m_context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(27, builder.build());
    }
}
