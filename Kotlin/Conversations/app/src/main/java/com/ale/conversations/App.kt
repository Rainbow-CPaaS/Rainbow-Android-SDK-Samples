package com.ale.conversations

import android.app.Application
import com.ale.rainbowsdk.RainbowSdk

class App : Application() {

    private val appID = "YOUR_APP_ID"
    private val appSecret = "YOUR_APP_SECRET"

    override fun onCreate() {
        super.onCreate()
        // Initialize SDK with app id and app secret
        RainbowSdk.instance().initialize(this, appID, appSecret)
    }
}