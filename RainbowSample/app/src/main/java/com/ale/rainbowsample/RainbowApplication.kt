package com.ale.rainbowsample

import android.app.Application
import com.ale.rainbowsdk.RainbowSdk

class RainbowApplication : Application() {

    private lateinit var notificationCenter: NotificationCenter

    override fun onCreate() {
        super.onCreate()
        RainbowSdk().initialize(
            applicationContext = this,
            applicationId = BuildConfig.APPLICATION_ID,
            applicationSecret = BuildConfig.APPLICATION_SECRET,
        )

        notificationCenter = NotificationCenter(this)
        notificationCenter.start()
    }

    fun stop() {
        notificationCenter.stop()
    }
}