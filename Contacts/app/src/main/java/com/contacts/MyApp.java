package com.contacts;

import android.app.Application;
import android.graphics.Color;

import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.listener.IRainbowGetConversationListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.contacts.activities.StartupActivity;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RainbowSdk.instance().setNotificationBuilder(getApplicationContext(),
            StartupActivity.class,
            R.drawable.biz_on_status,
            getString(R.string.app_name),
            getString(R.string.topic_app),
            Color.RED);

        String applicationId = "YOUR APPLICATION IDENTIFIER";
        String applicationSecret = "YOUR APPLICATION SECRET";
        RainbowSdk.instance().initialize(applicationId, applicationSecret);
    }
}
