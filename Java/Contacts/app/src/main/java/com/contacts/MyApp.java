package com.contacts;

import android.app.Application;
import android.graphics.Color;

import com.ale.rainbowsdk.RainbowSdk;
import com.contacts.activities.StartupActivity;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String applicationId = "YOUR APPLICATION IDENTIFIER";
        String applicationSecret = "YOUR APPLICATION SECRET";
        RainbowSdk.instance().initialize(this, applicationId, applicationSecret);
    }
}
