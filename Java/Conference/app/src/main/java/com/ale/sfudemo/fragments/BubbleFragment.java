package com.ale.sfudemo.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ale.infra.manager.pgiconference.IPgiConferenceProxy;
import com.ale.infra.manager.room.Room;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.sfudemo.R;
import com.ale.sfudemo.activities.StartupActivity;

public class BubbleFragment extends Fragment {

    private static final String TAG = "BubbleFragment";
    private StartupActivity m_activity;
    private Room m_room;
    private boolean m_isCanJoinableRoom = false;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.bubble_fragment, container, false);

        if (m_activity != null && m_activity.getSupportActionBar() != null) {
            if (m_room != null && m_room.getName() != null)
                m_activity.getSupportActionBar().setTitle(m_room.getName());
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        Button startConference = (Button) fragmentView.findViewById(R.id.start_conference);
        Button stopConference = (Button) fragmentView.findViewById(R.id.stop_conference);
        Button joinConference = (Button) fragmentView.findViewById(R.id.join_conference);

        TextView labelForConfStarted = (TextView) fragmentView.findViewById(R.id.conf_started);

        if (m_room.isUserOwner()) {
            startConference.setVisibility(View.VISIBLE);
            startConference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean hasMicrophonePermission = m_activity.hasMicrophonePermission();
                    boolean hasCameraPermission = m_activity.hasCameraPermission();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasMicrophonePermission && hasCameraPermission) {
                        // The Android version is lower than M OR we already have the permission "RECORD_AUDIO" --> just make the audio call
                        RainbowSdk.instance().bubbles().startAndJoinConference(m_room, new IPgiConferenceProxy.IJoinAudioCallListener() {
                            @Override
                            public void onJoinAudioCallSuccess(String jingleJid) {
                                if (m_room.getPgiConference() != null)
                                    RainbowSdk.instance().webRTC().makeConferenceCall(m_room.getPgiConference().getId(), jingleJid);
                                if (m_activity != null) {
                                    m_activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            stopConference.setVisibility(View.VISIBLE);
                                            startConference.setVisibility(View.INVISIBLE);
                                            labelForConfStarted.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onJoinAudioCallFailed(IPgiConferenceProxy.ConferenceError error) {
                                if (m_activity != null) {
                                    m_activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(m_activity, "Error when you try to start and join the conference... " + error.name(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        if (!hasMicrophonePermission && !hasCameraPermission) {
                            // Both permissions are missing --> ask for them
                            ActivityCompat.requestPermissions(m_activity, new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, StartupActivity.REQUEST_MAKE_VIDEO_CALL);
                        } else if (!hasMicrophonePermission){
                            // Permission "RECORD_AUDIO" is missing --> ask for it
                            ActivityCompat.requestPermissions(m_activity, new String[] {Manifest.permission.RECORD_AUDIO}, StartupActivity.REQUEST_MAKE_VIDEO_CALL);
                        } else {
                            // Permission "CAMERA" is missing --> ask for it
                            ActivityCompat.requestPermissions(m_activity, new String[] {Manifest.permission.CAMERA}, StartupActivity.REQUEST_MAKE_VIDEO_CALL);
                        }
                    }

                }
            });
            stopConference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RainbowSdk.instance().bubbles().stopAudioConference(m_room, new IPgiConferenceProxy.IStopAudioConfListener() {
                        @Override
                        public void onStopAudioConfSuccess() {
                            if (m_activity != null) {
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stopConference.setVisibility(View.INVISIBLE);
                                        startConference.setVisibility(View.VISIBLE);
                                        labelForConfStarted.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onStopAudioConfFailed(IPgiConferenceProxy.ConferenceError error) {

                        }
                    });
                }
            });
        } else if (m_isCanJoinableRoom) {
            if (m_activity != null) {
                m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopConference.setVisibility(View.INVISIBLE);
                        startConference.setVisibility(View.INVISIBLE);
                        labelForConfStarted.setVisibility(View.VISIBLE);
                        joinConference.setVisibility(View.VISIBLE);
                    }
                });
            }

            joinConference.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RainbowSdk.instance().bubbles().joinAudioConference(m_room, null, new IPgiConferenceProxy.IJoinAudioCallListener() {
                        @Override
                        public void onJoinAudioCallSuccess(String jingleJid) {
                            if (m_room.getPgiConference() != null)
                                RainbowSdk.instance().webRTC().makeConferenceCall(m_room.getPgiConference().getId(), jingleJid);
                            if (m_activity != null) {
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stopConference.setVisibility(View.VISIBLE);
                                        joinConference.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onJoinAudioCallFailed(IPgiConferenceProxy.ConferenceError error) {
                            if (m_activity != null) {
                                m_activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(m_activity, "Error when you try to join the conference... " + error.name(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } else {
            TextView labelForUserNotConfOwner = (TextView) fragmentView.findViewById(R.id.label_not_conf_owner);
            labelForUserNotConfOwner.setVisibility(View.VISIBLE);
        }



        return fragmentView;
    }

    public void setRoom(Room room, boolean canJoinRoom) {
        m_room = room;
        m_isCanJoinableRoom = canJoinRoom;
    }

}
