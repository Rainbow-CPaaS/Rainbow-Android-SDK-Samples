package com.ale.filesharingdemo;

import android.app.Application;

import com.ale.rainbowsdk.RainbowSdk;

public class App extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        final String APPLICATION_ID = "YOUR_APP_ID";
        final String SECRET_KEY = "YOUR_APP_SECRET";
        RainbowSdk.instance().initialize(this, APPLICATION_ID, SECRET_KEY);
    }
}
