package com.ale.conversationsDemo;

import android.app.Application;
import android.graphics.Color;
import android.os.StrictMode;

import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.activity.StartupActivity;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final String APPLICATION_ID = "1dcc58f0d87711e9a360ebbee0f11a54";
        final String SECRET_KEY = "LeemLFz1k5Ov48DBdOwF7sOGD259AWLohdzH7PMBDXj2zOWc2ZPUMG69MbE560ir";
        RainbowSdk.instance().initialize(this, APPLICATION_ID, SECRET_KEY);
    }
}
