package com.ale.push

import android.app.Application
import androidx.core.view.isVisible
import com.ale.listener.SigninResponseListener
import com.ale.listener.SignoutResponseListener
import com.ale.rainbowsdk.RainbowSdk

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    private val applicationID = "your_application_id_here"
    private val applicationSecret = "your_application_secret_here"

    private var messageNotificationManager: MessageNotificationManager? = null
    private var telephonyNotificationMgr: TelephonyNotificationMgr? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        RainbowSdk.instance().initialize(this, applicationID, applicationSecret)
        RainbowSdk.instance().push().activate(this)
    }

    /**
     * This method is used when a call is received in push mode and the xmpp connection could not be established.
     * In this case, we must first login the user with cached data.
     * Once connected, we can process the push message
     */
    fun startSilently(onSignInSucceed : () -> Unit) {
        login(onSignInSucceed)
    }

    fun login(onSignInSucceed: () -> Unit) {
        // To keep this sample simple, the login and password are added directly
        RainbowSdk.instance().connection().signin("your_login_here", "your_password_here", "sandbox.openrainbow.com", object: SigninResponseListener() {
            override fun onRequestFailed(errorCode: RainbowSdk.ErrorCode?, err: String?) {
                // Error cases are not handled in this sample
            }

            override fun onSigninSucceeded() {
                initialize()
                onSignInSucceed()
            }
        })
    }

    fun logout(onLogoutSucceed: () -> Unit) {
        RainbowSdk.instance().connection().signout(object : SignoutResponseListener() {
            override fun onSignoutSucceeded() {
                unInitialize()
                onLogoutSucceed()
            }
        })
    }

    fun initialize() {
        messageNotificationManager = MessageNotificationManager()
        telephonyNotificationMgr = TelephonyNotificationMgr()
    }

    fun unInitialize() {
        messageNotificationManager?.stop()
        telephonyNotificationMgr?.stop()
    }
}