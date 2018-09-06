package com.ale.sfudemo;

import android.app.Application;
import android.graphics.Color;
import android.os.StrictMode;

import com.ale.infra.application.ApplicationData;
import com.ale.rainbowsdk.RainbowSdk;

public class MyApp extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();


        String applicationId = "YOUR APPLICATION IDENTIFIER";
        String applicationSecret = "YOUR APPLICATION SECRET";
        RainbowSdk.instance().initialize(this, applicationId, applicationSecret);


    }
}
