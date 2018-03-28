package webrtccall.fragments;

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

import webrtccall.activities.StartupActivity;
import webrtccall.callapplication.R;

public class LoginFragment extends Fragment {

    private static final String LOG_TAG = "LoginFragment";
    private StartupActivity m_activity;

    private EditText m_loginView;
    private EditText m_passwordView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.login_fragment, container, false);

        // Get the login view and set the login in cache
        m_loginView = (EditText)fragmentView.findViewById(R.id.login);
        m_loginView.setText(RainbowSdk.instance().myProfile().getUserLoginInCache());

        // Get the password view and set the password in cache
        m_passwordView = (EditText)fragmentView.findViewById(R.id.password);
        m_passwordView.setText(RainbowSdk.instance().myProfile().getUserPasswordInCache());

        // Button to sign in
        Button signButton = (Button)fragmentView.findViewById(R.id.sign_button);
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        return fragmentView;
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
                                Toast.makeText(m_activity, "Sign in success!", Toast.LENGTH_SHORT).show();
                                m_activity.openContactsTabFragment();
                            }
                        });
                    }
                    @Override
                    public void onRequestFailed(final RainbowSdk.ErrorCode errorCode, final String s) {
                        m_activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(m_activity, "Sign in failed: " + errorCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String err) {
                Log.getLogger().error(LOG_TAG, "The Rainbow SDK service has encountered an error when trying to start.");
            }
        });
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
}
