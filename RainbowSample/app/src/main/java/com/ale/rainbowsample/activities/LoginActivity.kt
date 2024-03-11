package com.ale.rainbowsample.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ale.rainbowsample.databinding.ActivityLoginBinding
import com.ale.rainbowsdk.Connection
import com.ale.rainbowsdk.RainbowSdk

class LoginActivity : AppCompatActivity(), Connection.IConnectionListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        RainbowSdk().connection().registerConnectionListener(this)
    }

    override fun onPause() {
        super.onPause()
        RainbowSdk().connection().unregisterConnectionListener(this)
    }

    override fun onUserLogoutForced(restart: Boolean) {
        // TODO navigate back
    }
}