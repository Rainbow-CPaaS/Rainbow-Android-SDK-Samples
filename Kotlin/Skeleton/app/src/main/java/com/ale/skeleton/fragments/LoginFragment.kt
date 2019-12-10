package com.ale.conversations.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.ale.conversations.R
import com.ale.conversations.activities.StartupActivity
import com.ale.conversations.extensions.toast
import com.ale.listener.SigninResponseListener
import com.ale.listener.StartResponseListener
import com.ale.rainbowsdk.RainbowSdk
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : Fragment() {

    private lateinit var activity: StartupActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as StartupActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity.supportActionBar?.hide()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailView = edit_email
        val passwordView = edit_password
        val signInButton = sign_in_button

        // Circular progress on button
        bindProgressButton(signInButton)
        signInButton.attachTextChangeAnimator()

        // If user has already logged in we can retrieve information in cache
        if (!RainbowSdk.instance().myProfile().userLoginInCache.isNullOrEmpty())
            emailView.setText(RainbowSdk.instance().myProfile().userLoginInCache)

        if (!RainbowSdk.instance().myProfile().userPasswordInCache.isNullOrEmpty())
            passwordView.setText(RainbowSdk.instance().myProfile().userPasswordInCache)

        signInButton.setOnClickListener {
            signInButton.showProgress {
                progressColor = Color.WHITE
            }
            // Passing listener to start function
            RainbowSdk.instance().connection().start(object : StartResponseListener() {
                override fun onRequestFailed(p0: RainbowSdk.ErrorCode?, p1: String?) = activity.toast("Start failed : $p1")

                override fun onStartSucceeded() {
                    // Sign in to rainbow
                    RainbowSdk.instance().connection().signin(emailView.text.toString(), passwordView.text.toString(), object : SigninResponseListener() {
                        override fun onRequestFailed(p0: RainbowSdk.ErrorCode?, p1: String?) {
                            activity.toast("Sign in failed : $p1")
                            activity.runOnUiThread { signInButton.hideProgress(R.string.action_sign_in) }
                        }

                        override fun onSigninSucceeded() {
                            activity.toast("You are connected")
                            activity.runOnUiThread { signInButton.hideProgress(R.string.action_sign_in) }
                        }
                    })
                }
            })
        }
    }
}