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
