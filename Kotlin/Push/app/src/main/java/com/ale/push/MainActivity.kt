package com.ale.push

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import com.ale.listener.SigninResponseListener
import com.ale.listener.SignoutResponseListener
import com.ale.rainbowsdk.RainbowSdk
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var loginLogoutButton: MaterialButton
    private lateinit var connectedText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginLogoutButton = findViewById(R.id.login_logout_button)
        connectedText = findViewById(R.id.connected_text)

        loginLogoutButton.setOnClickListener {
            if (RainbowSdk.instance().connection().isConnected) {
                MyApplication.instance.logout {
                    this@MainActivity.runOnUiThread {
                        connectedText.apply {
                            isVisible = false
                        }
                        loginLogoutButton.text = getString(R.string.login)
                    }
                }
            } else {
                MyApplication.instance.login {
                    this@MainActivity.runOnUiThread {
                        connectedText.apply {
                            text = getString(
                                R.string.connected_on,
                                RainbowSdk.instance().myProfile().getConnectedUser()
                                    .getDisplayName("Unknown")
                            )
                            isVisible = true
                        }
                        loginLogoutButton.text = getString(R.string.logout)
                    }
                }
            }
        }
    }
}