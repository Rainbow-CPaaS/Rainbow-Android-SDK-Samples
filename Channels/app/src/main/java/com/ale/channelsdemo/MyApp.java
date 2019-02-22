package com.ale.channelsdemo;

import android.app.Application;

import com.ale.rainbowsdk.RainbowSdk;

public class MyApp extends Application {

    private final String APPLICATION_ID = "YOUR APP ID";
    private final String SECRET_KEY = "YOUR SECRET KEY";

    @Override
    public void onCreate() {
        super.onCreate();
        RainbowSdk.instance().initialize(this, APPLICATION_ID, SECRET_KEY);
    }
}