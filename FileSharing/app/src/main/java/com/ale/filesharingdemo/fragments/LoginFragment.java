package com.ale.filesharingdemo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ale.filesharingdemo.R;
import com.ale.filesharingdemo.activities.StartupActivity;
import com.ale.listener.SigninResponseListener;
import com.ale.listener.StartResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.StringsUtil;

public class LoginFragment extends Fragment {

    private StartupActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (StartupActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(R.string.app_name);

        View fragmentView = inflater.inflate(R.layout.login_fragment, container, false);

        EditText emailView = fragmentView.findViewById(R.id.email_edit_text);
        EditText passwordView = fragmentView.findViewById(R.id.password_edit_text);
        Button signInButton = fragmentView.findViewById(R.id.sign_in_button);

        if (!StringsUtil.isNullOrEmpty(RainbowSdk.instance().myProfile().getUserLoginInCache()))
            emailView.setText(RainbowSdk.instance().myProfile().getUserLoginInCache());

        if (!StringsUtil.isNullOrEmpty(RainbowSdk.instance().myProfile().getUserPasswordInCache()))
            passwordView.setText(RainbowSdk.instance().myProfile().getUserPasswordInCache());

        signInButton.setOnClickListener(view -> RainbowSdk.instance().connection().start(new StartResponseListener() {
            @Override
            public void onStartSucceeded() {
                RainbowSdk.instance().connection().signin(emailView.getText().toString(), passwordView.getText().toString(), "sandbox.openrainbow.com", new SigninResponseListener() {
                    @Override
                    public void onSigninSucceeded() {
                        activity.runOnUiThread(() -> Toast.makeText(getActivity(), "You are connected", Toast.LENGTH_SHORT).show());
                        activity.openFilesFragment();
                    }

                    @Override
                    public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Sign-in failed: " + s, Toast.LENGTH_LONG).show());
                    }
                });
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Start sign-in failed: " + s, Toast.LENGTH_LONG).show());
            }
        }));

        return fragmentView;
    }
}
