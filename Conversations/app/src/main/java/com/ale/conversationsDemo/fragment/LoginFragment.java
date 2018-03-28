package com.ale.conversationsDemo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ale.listener.SigninResponseListener;
import com.ale.listener.StartResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;
import com.ale.conversationsDemo.manager.ImNotificationMgr;

/**
 * Created by letrongh on 11/04/2017.
 */

public class LoginFragment extends Fragment {

    private static final String LOG_TAG = "LoginFragment";
    private StartupActivity m_activity;

    private EditText m_loginView;
    private EditText m_passwordView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.login_fragment, container, false);

        if (m_activity != null && m_activity.getSupportActionBar() != null) {
            m_activity.getSupportActionBar().setTitle(R.string.login);
            m_activity.getSupportActionBar().setHomeButtonEnabled(false);
        }

        // Login view and set the default value
        m_loginView = (EditText)fragmentView.findViewById(R.id.login);

        // Password view and set the default value
        m_passwordView = (EditText)fragmentView.findViewById(R.id.password);

        // Button to sign in
        Button signButton = (Button)fragmentView.findViewById(R.id.sign_button);
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        m_loginView.setText(RainbowSdk.instance().myProfile().getUserLoginInCache());
//        /m_passwordView.setText(RainbowSdk.instance().().getUserPasswordInCache());

        return fragmentView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof StartupActivity){
            m_activity = (StartupActivity) context;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity instanceof  StartupActivity) {
                m_activity = (StartupActivity)activity;
            }
        }
    }


    private void attemptLogin() {
        final String email = m_loginView.getText().toString();
        final String password = m_passwordView.getText().toString();

        RainbowSdk.instance().connection().start(new StartResponseListener() {
            @Override
            public void onStartSucceeded() {
                RainbowSdk.instance().connection().signin(email, password, "sandbox.openrainbow.com", new SigninResponseListener() {
                    @Override
                    public void onSigninSucceeded() {
                        m_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ImNotificationMgr(m_activity);
                                m_activity.openConversationsTabFragment();
                                m_activity.unlockDrawer();
                            }
                        });
                    }
                    @Override
                    public void onRequestFailed(final RainbowSdk.ErrorCode errorCode, final String s) {
                        m_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(m_activity, "Signin failed: " + errorCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String err) {
                Log.getLogger().error(LOG_TAG, "SDK start failure !!!");
            }
        });

    }
}
