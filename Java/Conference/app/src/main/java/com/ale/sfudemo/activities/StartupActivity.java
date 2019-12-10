package com.ale.sfudemo.activities;


import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.infra.manager.pgiconference.PgiConference;
import com.ale.infra.manager.room.Room;
import com.ale.listener.SignoutResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.sfudemo.R;
import com.ale.sfudemo.fragments.BubbleFragment;
import com.ale.sfudemo.fragments.BubblesFragment;
import com.ale.sfudemo.fragments.LoginFragment;

public class StartupActivity extends AppCompatActivity implements ITelephonyListener {
    private static final String TAG = "StartupActivity";

    public final static int REQUEST_MAKE_AUDIO_CALL = 1;
    public final static int REQUEST_MAKE_VIDEO_CALL = 2;
    public final static int REQUEST_TAKE_AUDIO_CALL = 3;
    public final static int REQUEST_TAKE_VIDEO_CALL = 4;

    private IRainbowContact m_lastOpenedContact = null;

    private IItemListChangeListener m_bubblesListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Room room : RainbowSdk.instance().bubbles().getAllBubbles().getCopyOfDataList())
                    {

                        if (!room.isUserOwner() && room.getPgiConference() != null && room.getPgiConference().getMediaType() == PgiConference.MediaType.WEB_RTC && RainbowSdk.instance().bubbles().isConferenceJoinable(room) && RainbowSdk.instance().webRTC().getCurrentCall() == null)
                        {
                            openBubbleFragment(room, true);
                        }
                    }

                }
            });
        }
    };

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
     * @param room
     */
    public void openBubbleFragment(Room room, boolean canJoinRoom) {
        BubbleFragment fragment = new BubbleFragment();
        fragment.setRoom(room, canJoinRoom);
        openFragment(fragment, true);

    }

    public void openBubblesFragment() {
        BubblesFragment fragment = new BubblesFragment();
        openFragment(fragment, false);
        RainbowSdk.instance().webRTC().registerTelephonyListener(this);
    }


    /**
     * Open the LoginFragment
     */
    public void openLoginFragment() {
        LoginFragment fragment = new LoginFragment();
        openFragment(fragment, false);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        //RainbowSdk.instance().webRTC().unregisterTelephonyListener(this);
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
        //startActivity(new Intent(this, WebRTCActivity.class));
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
                       Toast.makeText(this, "please retry to start conference...", Toast.LENGTH_SHORT).show();
                        break;
                    case REQUEST_MAKE_VIDEO_CALL:
                        Toast.makeText(this, "please retry to start conference...", Toast.LENGTH_SHORT).show();
                        break;
                    case REQUEST_TAKE_AUDIO_CALL:
                        //RainbowSdk.instance().webRTC().takeCall(false);
                        break;
                    case REQUEST_TAKE_VIDEO_CALL:
                        //RainbowSdk.instance().webRTC().takeCall(true);
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

    public void registerBubblesListener() {
        RainbowSdk.instance().bubbles().getAllBubbles().registerChangeListener(m_bubblesListener);
    }
}
