package com.contacts.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ale.listener.SigninResponseListener;
import com.ale.listener.StartResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.StringsUtil;
import com.contacts.R;
import com.contacts.activities.StartupActivity;

public class LoginFragment extends Fragment {

    private StartupActivity m_activity;

    private EditText m_emailView;
    private EditText m_passwordView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.login_fragment, container, false);

        m_emailView = fragmentView.findViewById(R.id.email_edit_text);
        m_passwordView = fragmentView.findViewById(R.id.password_text_view);

        if (!StringsUtil.isNullOrEmpty(RainbowSdk.instance().myProfile().getUserLoginInCache()))
            m_emailView.setText(RainbowSdk.instance().myProfile().getUserLoginInCache());

        if (!StringsUtil.isNullOrEmpty(RainbowSdk.instance().myProfile().getUserPasswordInCache()))
            m_passwordView.setText(RainbowSdk.instance().myProfile().getUserPasswordInCache());

        Button mEmailSignInButton = fragmentView.findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RainbowSdk.instance().connection().start(new StartResponseListener() {
                    @Override
                    public void onStartSucceeded() {
                        RainbowSdk.instance().connection().signin(m_emailView.getText().toString(), m_passwordView.getText().toString(), "sandbox.openrainbow.com", new SigninResponseListener() {
                            @Override
                            public void onSigninSucceeded() {
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "You are connected.", Toast.LENGTH_SHORT).show();
                                        m_activity.openContactsTabFragment();
                                    }
                                });
                            }

                            @Override
                            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(m_activity, "Signin failed: " + s, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                        // Something goes wrong
                    }
                });
            }
        });

        return fragmentView;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof StartupActivity) {
            m_activity = (StartupActivity)context;
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
}
