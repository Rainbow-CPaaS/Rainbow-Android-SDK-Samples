package webrtccall;

import android.app.Application;
import android.graphics.Color;

import com.ale.rainbowsdk.RainbowSdk;

import webrtccall.activities.StartupActivity;
import webrtccall.callapplication.R;

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
