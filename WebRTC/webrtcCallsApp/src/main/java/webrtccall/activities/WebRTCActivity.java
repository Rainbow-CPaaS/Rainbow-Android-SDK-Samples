package webrtccall.activities;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.PeerSession;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.rainbow.phone.session.MediaState;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.Map;

import webrtccall.callapplication.R;

public class WebRTCActivity extends Activity implements ITelephonyListener {

    private final static String LOG_TAG = "WebRTCActivity";

    private LinearLayout m_outgoingCallLayout;
    private LinearLayout m_incomingCallLayout;
    private RelativeLayout m_ongoingCallLayout;

    private ImageView m_answerVideoCallButton;
    private CardView m_cardViewPhoto;

    private SurfaceViewRenderer m_bigVideoView;
    private SurfaceViewRenderer m_littleVideoView;
    private VideoRenderer m_remoteVideoRenderer;
    private VideoRenderer m_localVideoRenderer;
    private boolean m_localVideoOnLittleView = true;

    private MediaPlayer m_mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.web_rtc_activity);

        WebRTCCall currentCall = RainbowSdk.instance().webRTC().getCurrentCall();

        if (currentCall == null) {
            Log.getLogger().warn(LOG_TAG, "No call to display");
            finish();
            return;
        } else if (currentCall.getDistant() == null) {
            Log.getLogger().warn(LOG_TAG, "Contact is null");
            finish();
            return;
        }

        m_cardViewPhoto = (CardView) findViewById(R.id.card_view_photo);

        ImageView imageViewPhoto = (ImageView) findViewById(R.id.photo_image_view);
        if (currentCall.getDistant().getPhoto() == null) {
            imageViewPhoto.setImageResource(R.drawable.contact);
        } else {
            imageViewPhoto.setImageBitmap(currentCall.getDistant().getPhoto());
        }

        // ===== m_incomingCallLayout
        m_incomingCallLayout = (LinearLayout) findViewById(R.id.incoming_call_layout);

        ImageView answerAudioCallButton = (ImageView) findViewById(R.id.answer_audio_button);
        answerAudioCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().takeCall(false);
            }
        });

        m_answerVideoCallButton = (ImageView) findViewById(R.id.answer_video_button);
        m_answerVideoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().takeCall(true);
            }
        });

        ImageView rejectCallButton = (ImageView) findViewById(R.id.reject_call_button);
        rejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().rejectCall();
            }
        });


        // ===== m_outgoingCallLayout
        m_outgoingCallLayout = (LinearLayout) findViewById(R.id.outgoing_call_layout);

        ImageView imageViewhangupCall = (ImageView) findViewById(R.id.hangup_call_button);
        imageViewhangupCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().hangupCall();
            }
        });

        // ===== m_ongoingCallLayout
        m_ongoingCallLayout = (RelativeLayout) findViewById(R.id.ongoing_call_layout);

        // Switch camera
        ImageView imageViewSwitchCamera = (ImageView) findViewById(R.id.switch_camera_button);
        imageViewSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().switchCamera();
            }
        });

        // Mute / unmute
        final ImageView imageViewStateMicOff = (ImageView) findViewById(R.id.state_mic_off);
        final ImageView imageViewMute = (ImageView) findViewById(R.id.mute_image_button);
        imageViewMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().mute(!RainbowSdk.instance().webRTC().isMuted(), false);
                imageViewMute.setImageResource(RainbowSdk.instance().webRTC().isMuted() ? R.drawable.btn_mic_on : R.drawable.btn_mic_off);
                imageViewStateMicOff.setVisibility(RainbowSdk.instance().webRTC().isMuted() ? View.VISIBLE : View.GONE);
            }
        });

        // Add / remove video
        final ImageView imageViewAddVideo = (ImageView) findViewById(R.id.add_video_button);
        imageViewAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RainbowSdk.instance().webRTC().getLocalVideoTrack() != null) {
                    if (RainbowSdk.instance().webRTC().dropVideo()) {
                        imageViewAddVideo.setImageResource(R.drawable.btn_camera_on);
                    }
                } else {
                    if (RainbowSdk.instance().webRTC().addVideo()) {
                        imageViewAddVideo.setImageResource(R.drawable.btn_camera_off);
                    }
                }
            }
        });

        // Hang up call
        ImageView imageViewHangupCall = (ImageView) findViewById(R.id.hang_up_image_view);
        imageViewHangupCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().hangupCall();
            }
        });

        // Big video view
        m_bigVideoView = (SurfaceViewRenderer) findViewById(R.id.big_video_view);
        m_bigVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        m_bigVideoView.setMirror(true);
        m_bigVideoView.requestLayout();

        // Little video  (in top right)
        m_littleVideoView = (SurfaceViewRenderer) findViewById(R.id.little_video_view);
        m_littleVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        m_littleVideoView.setZOrderMediaOverlay(true);
        m_littleVideoView.setMirror(true);
        m_littleVideoView.requestLayout();

        // Init these two surface views
        try {
            m_bigVideoView.init(RainbowSdk.instance().webRTC().getCurrentCall().getEglBaseContext(), null);
            m_littleVideoView.init(RainbowSdk.instance().webRTC().getCurrentCall().getEglBaseContext(), null);
        } catch (RuntimeException e) {
            Log.getLogger().warn(LOG_TAG, "EGL context seems to be dead, call must have been removed");
            finish();
            return;
        }

        // Animation to show or hide buttons menu when video is used
        final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (m_ongoingCallLayout.getVisibility() != View.VISIBLE) {
                    m_ongoingCallLayout.setVisibility(View.VISIBLE);
                } else {
                    m_ongoingCallLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        m_bigVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_ongoingCallLayout.getVisibility() != View.VISIBLE) {
                    AlphaAnimation fade_in = new AlphaAnimation(0.0f, 1.0f);
                    fade_in.setDuration(500);
                    fade_in.setAnimationListener(animationListener);
                    m_ongoingCallLayout.startAnimation(fade_in);
                } else {
                    AlphaAnimation fade_out = new AlphaAnimation(1.0f, 0.0f);
                    fade_out.setDuration(500);
                    fade_out.setAnimationListener(animationListener);
                    m_ongoingCallLayout.startAnimation(fade_out);
                }
            }
        });

        m_littleVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_localVideoOnLittleView = !m_localVideoOnLittleView;
                Map<PeerSession.PeerSessionType, MediaStream> streams = RainbowSdk.instance().webRTC().getAddedStreams();
                if (streams.containsKey(PeerSession.PeerSessionType.AUDIO_VIDEO_SHARING)) {
                    MediaStream stream = streams.get(PeerSession.PeerSessionType.AUDIO_VIDEO_SHARING);
                    if (m_localVideoOnLittleView) {
                        renderRemoteVideo(stream, m_bigVideoView);
                        renderLocalVideo(RainbowSdk.instance().webRTC().getLocalVideoTrack(), m_littleVideoView);
                    } else {
                        renderRemoteVideo(stream, m_littleVideoView);
                        renderLocalVideo(RainbowSdk.instance().webRTC().getLocalVideoTrack(), m_bigVideoView);
                    }
                }

            }
        });

        // Listen to events
        RainbowSdk.instance().webRTC().registerTelephonyListener(this);

        updateLayoutWithCall(RainbowSdk.instance().webRTC().getCurrentCall());

        handleRinging();
    }

    @Override
    protected void onDestroy() {
        RainbowSdk.instance().webRTC().unregisterTelephonyListener(this);

        super.onDestroy();
    }

    @Override
    public void onCallAdded(WebRTCCall call) {

    }

    @Override
    public void onCallModified(WebRTCCall call) {
        updateLayoutWithCall(call);
        stopRinging();
    }

    @Override
    public void onCallRemoved(WebRTCCall call) {
        stopRinging();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    private void handleRinging() {
        if (m_mediaPlayer != null) {
            stopRinging();
        }

        try {
            String ringtoneString = "android.resource://call.callapplication/" + R.raw.incoming_ringing;

            if (RainbowSdk.instance().webRTC().getCurrentCall().getState() == MediaState.RINGING_OUTGOING) {
                ringtoneString = "android.resource://call.callapplication/" + R.raw.outgoing_ringing;
            }

            Uri ringtoneUri = Uri.parse(ringtoneString);

            m_mediaPlayer = new MediaPlayer();
            m_mediaPlayer.setLooping(true);
            m_mediaPlayer.setDataSource(getApplicationContext(), ringtoneUri);

            m_mediaPlayer.prepare();
            m_mediaPlayer.start();

        } catch (Exception e) {
            Log.getLogger().error(LOG_TAG, "Impossible to get the default ringtone", e);
        }
    }

    private void stopRinging() {
        if (m_mediaPlayer != null) {
            if (m_mediaPlayer.isPlaying()) {
                m_mediaPlayer.stop();
            }
            m_mediaPlayer.release();
            m_mediaPlayer = null;
        }
    }

    private void updateLayoutWithCall(final WebRTCCall call) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (call.getState() == MediaState.RINGING_OUTGOING) {
                    showLayoutAndHideOthers(m_outgoingCallLayout);
                } else if (call.getState() == MediaState.RINGING_INCOMING) {
                    showLayoutAndHideOthers(m_incomingCallLayout);

                    m_answerVideoCallButton.setEnabled(call.wasInitiatedWithVideo());
                    if (call.wasInitiatedWithVideo()) {
                        m_answerVideoCallButton.setImageResource(R.drawable.bt_answercall_video_active);
                    } else {
                        m_answerVideoCallButton.setImageResource(R.drawable.bt_answercall_video_inactive);
                    }
                } else if (call.getState() == MediaState.ACTIVE) {
                    showLayoutAndHideOthers(m_ongoingCallLayout);

                    m_cardViewPhoto.setVisibility(call.wasInitiatedWithVideo() ? View.INVISIBLE : View.VISIBLE);
                    m_bigVideoView.setVisibility(call.wasInitiatedWithVideo() ? View.VISIBLE : View.GONE);
                    m_littleVideoView.setVisibility(call.wasInitiatedWithVideo() ? View.VISIBLE : View.GONE);
                }
                //m_localVideoOnLittleView = !m_localVideoOnLittleView;
                Map<PeerSession.PeerSessionType, MediaStream> streams = RainbowSdk.instance().webRTC().getAddedStreams();
                if (streams.containsKey(PeerSession.PeerSessionType.AUDIO_VIDEO_SHARING)) {
                    MediaStream stream = streams.get(PeerSession.PeerSessionType.AUDIO_VIDEO_SHARING);
                    if (m_localVideoOnLittleView) {

                        renderRemoteVideo(stream, m_bigVideoView);
                        renderLocalVideo(RainbowSdk.instance().webRTC().getLocalVideoTrack(), m_littleVideoView);
                    } else {
                        renderRemoteVideo(stream, m_littleVideoView);
                        renderLocalVideo(RainbowSdk.instance().webRTC().getLocalVideoTrack(), m_bigVideoView);
                    }
                }
            }
        });
    }

    private void showLayoutAndHideOthers(final ViewGroup layout) {
        m_outgoingCallLayout.setVisibility(View.GONE);
        m_incomingCallLayout.setVisibility(View.GONE);
        m_ongoingCallLayout.setVisibility(View.GONE);

        if (layout != null) {
            layout.setVisibility(View.VISIBLE);
        }
    }

    private void renderRemoteVideo(MediaStream stream, SurfaceViewRenderer surfaceViewRenderer)
    {
        // set the remote renderer to this incoming m_stream:
        if (RainbowContext.getInfrastructure().getCapabilities().isVideoWebRtcAllowed() && stream.videoTracks.size() > 0)
        {
            if (m_remoteVideoRenderer != null)
                stream.videoTracks.get(0).removeRenderer(m_remoteVideoRenderer);

            m_remoteVideoRenderer = new VideoRenderer(surfaceViewRenderer);
            stream.videoTracks.get(0).addRenderer(m_remoteVideoRenderer);

        }
        surfaceViewRenderer.requestLayout();
    }

    private void renderLocalVideo(VideoTrack videoTrack, SurfaceViewRenderer surfaceViewRenderer) {
        if (videoTrack != null) {
            if (m_localVideoRenderer != null)
                videoTrack.removeRenderer(m_localVideoRenderer);

            m_localVideoRenderer = new VideoRenderer(surfaceViewRenderer);

            videoTrack.addRenderer(m_localVideoRenderer);
        }

    }
}
