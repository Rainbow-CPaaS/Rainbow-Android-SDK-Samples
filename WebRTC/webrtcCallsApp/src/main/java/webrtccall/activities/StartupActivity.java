package webrtccall.activities;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.listener.SignoutResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;

import webrtccall.callapplication.R;
import webrtccall.fragments.ContactFragment;
import webrtccall.fragments.ContactsTabFragment;
import webrtccall.fragments.LoginFragment;

public class StartupActivity  extends AppCompatActivity implements ITelephonyListener {
    private static final String TAG = "StartupActivity";

    public final static int REQUEST_MAKE_AUDIO_CALL = 1;
    public final static int REQUEST_MAKE_VIDEO_CALL = 2;
    public final static int REQUEST_TAKE_AUDIO_CALL = 3;
    public final static int REQUEST_TAKE_VIDEO_CALL = 4;

    private IRainbowContact m_lastOpenedContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!RainbowSdk.instance().connection().isConnected()) {
            openLoginFragment();
        }
    }

    private void signout() {
        RainbowSdk.instance().connection().signout(new SignoutResponseListener() {
            @Override
            public void onSignoutSucceeded() {
                finish();
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                Log.getLogger().info(TAG, "Failed to signout");
            }
        });
    }

    /**
     * Open the fragment in parameter in the fragment_container
     *
     * @param fragment          Fragment to open
     * @param addToBackStack    Add or not to the stack
     */
    public void openFragment(Fragment fragment, boolean addToBackStack) {
        if (addToBackStack) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }

    /**
     * Open the ContactsTabFragment (list of contacts/rosters)
     */
    public void openContactsTabFragment() {
        ContactsTabFragment fragment = new ContactsTabFragment();
        openFragment(fragment, false);
        RainbowSdk.instance().webRTC().registerTelephonyListener(this);
    }

    public void openContactFragment(IRainbowContact contact) {
        ContactFragment fragment = new ContactFragment();
        fragment.setContact(contact);
        m_lastOpenedContact = contact;
        openFragment(fragment, true);
    }

    /**
     * Open the LoginFragment
     */
    public void openLoginFragment() {
        LoginFragment fragment = new LoginFragment();
        openFragment(fragment, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.exit:
                if (RainbowSdk.instance().connection().isConnected()) {
                    signout();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public void onCallAdded(WebRTCCall call) {
        startActivity(new Intent(this, WebRTCActivity.class));
    }

    @Override
    public void onCallModified(WebRTCCall call) {

    }

    @Override
    public void onCallRemoved(WebRTCCall call) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length && allPermissionsGranted; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                }
            }

            if (allPermissionsGranted) {
                switch (requestCode) {
                    case REQUEST_MAKE_AUDIO_CALL:
                        RainbowSdk.instance().webRTC().makeCall((Contact)m_lastOpenedContact, false);
                        break;
                    case REQUEST_MAKE_VIDEO_CALL:
                        RainbowSdk.instance().webRTC().makeCall((Contact)m_lastOpenedContact, true);
                        break;
                    case REQUEST_TAKE_AUDIO_CALL:
                        RainbowSdk.instance().webRTC().takeCall(false);
                        break;
                    case REQUEST_TAKE_VIDEO_CALL:
                        RainbowSdk.instance().webRTC().takeCall(true);
                        break;
                }
            } else {
                Toast.makeText(this, "You may grant authorization in order to make a call.", Toast.LENGTH_SHORT).show();
                if (requestCode == REQUEST_TAKE_AUDIO_CALL || requestCode == REQUEST_TAKE_VIDEO_CALL) {
                    RainbowSdk.instance().webRTC().rejectCall();
                }
            }
        } else {
            Toast.makeText(this, "You may grant authorization in order to make a call.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean hasMicrophonePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}