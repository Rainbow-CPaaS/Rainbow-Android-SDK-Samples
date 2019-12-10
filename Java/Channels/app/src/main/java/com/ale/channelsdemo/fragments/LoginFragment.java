package com.ale.channelsdemo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.activities.StartupActivity;
import com.ale.listener.SigninResponseListener;
import com.ale.listener.StartResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.StringsUtil;


public class LoginFragment extends Fragment {

    private StartupActivity m_activity;

    public static Fragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_activity = (StartupActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.app_name));

        View fragmentView = inflater.inflate(R.layout.login_fragment, container, false);
        EditText emailView = fragmentView.findViewById(R.id.email_edit_text);
        EditText passwordView = fragmentView.findViewById(R.id.password_edit_text);

        if (!StringsUtil.isNullOrEmpty(RainbowSdk.instance().myProfile().getUserLoginInCache()))
            emailView.setText(RainbowSdk.instance().myProfile().getUserLoginInCache());

        if (!StringsUtil.isNullOrEmpty(RainbowSdk.instance().myProfile().getUserPasswordInCache()))
            passwordView.setText(RainbowSdk.instance().myProfile().getUserPasswordInCache());

        Button signInButton = fragmentView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> RainbowSdk.instance().connection().start(new StartResponseListener() {
            @Override
            public void onStartSucceeded() {
                RainbowSdk.instance().connection().signin(emailView.getText().toString(), passwordView.getText().toString(), "sandbox.openrainbow.com", new SigninResponseListener() {
                    @Override
                    public void onSigninSucceeded() {
                        m_activity.runOnUiThread(() -> Toast.makeText(getActivity(), "You are connected.", Toast.LENGTH_SHORT).show());
                        m_activity.openChannelsFragment();
                    }

                    @Override
                    public void onRequestFailed(RainbowSdk.ErrorCode errorCode, final String s) {
                        m_activity.runOnUiThread(() -> Toast.makeText(m_activity, "Sign-in failed: " + s, Toast.LENGTH_LONG).show());
                    }
                });
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                m_activity.runOnUiThread(() -> Toast.makeText(m_activity, "Start sign-in failed: " + s, Toast.LENGTH_LONG).show());
            }
        }));

        return fragmentView;
    }
}
