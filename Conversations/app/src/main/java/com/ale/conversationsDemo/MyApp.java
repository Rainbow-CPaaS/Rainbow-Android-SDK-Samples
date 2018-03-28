package com.ale.conversationsDemo;

import android.app.Application;
import android.graphics.Color;
import android.os.StrictMode;

import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.activity.StartupActivity;

/**
 * Launch of the application test/
 */

public class MyApp extends Application {
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Work around: https://stackoverflow.com/a/45569709
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        RainbowSdk.instance().setNotificationBuilder(getApplicationContext(),
                                                    StartupActivity.class,
                                                    R.drawable.biz_on_status,
                                                    getString(R.string.app_name),
                                                    getString(R.string.connect_to_rainbow),
                                                    Color.RED);

		String applicationId = "YOUR APPLICATION IDENTIFIER";
        String applicationSecret = "YOUR APPLICATION SECRET";
        RainbowSdk.instance().initialize(applicationId, applicationSecret);
    }
}
