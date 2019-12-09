package com.contacts.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.util.StringsUtil;
import com.contacts.R;
import com.contacts.activities.StartupActivity;


public class ContactFragment extends Fragment implements IRainbowContact.IContactListener {

    private StartupActivity m_activity;
    private IRainbowContact m_contact;

    private ImageView m_photoImageView;
    private TextView m_displayNameTextView;
    private TextView m_presenceTextView;
    private Button m_callNumberButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.contact_fragment, container, false);

        if (m_activity.getSupportActionBar() != null) {
            m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.contacts));
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        m_contact.registerChangeListener(this);

        m_photoImageView = fragmentView.findViewById(R.id.photo_image_view);
        m_displayNameTextView = fragmentView.findViewById(R.id.display_name_text_view);
        m_presenceTextView = fragmentView.findViewById(R.id.presence_text_view);
        m_callNumberButton = fragmentView.findViewById(R.id.call_number_button);

        m_callNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_activity.makeNativeCall(m_contact.getFirstOfficePhoneNumber().getPhoneNumberValue());
            }
        });

        updateViews();

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        m_contact.unregisterChangeListener(this);

        super.onDestroyView();
    }

    public void setContact(IRainbowContact contact) {
        m_contact = contact;
    }

    private void updateViews() {
        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Set the photo
                if (m_contact.getPhoto() != null) {
                    m_photoImageView.setImageBitmap(m_contact.getPhoto());
                } else {
                    m_photoImageView.setImageResource(R.drawable.icon_contact);
                }

                // Set the display name
                String displayName = m_contact.getLoginEmail();
                if (!StringsUtil.isNullOrEmpty(m_contact.getFirstName()))
                    displayName = m_contact.getFirstName() + " " + m_contact.getLastName();
                m_displayNameTextView.setText(displayName);

                // Set the phone number
                if (m_contact.getFirstOfficePhoneNumber() != null) {
                    String displayText = getString(R.string.call) + " (" + m_contact.getFirstOfficePhoneNumber().getPhoneNumberValue() + ")";
                    m_callNumberButton.setText(displayText);
                    m_callNumberButton.setEnabled(true);
                } else {
                    m_callNumberButton.setText(R.string.no_phone_number);
                    m_callNumberButton.setEnabled(false);
                }

                // Set the presence
                if (RainbowPresence.ONLINE.equals(m_contact.getPresence())) {
                    // Online
                    m_presenceTextView.setTextColor(Color.GREEN);
                    m_presenceTextView.setText(R.string.online);
                } else if (RainbowPresence.MOBILE_ONLINE.equals(m_contact.getPresence())) {
                    // Online but on mobile
                    m_presenceTextView.setTextColor(Color.BLUE);
                    m_presenceTextView.setText(R.string.mobile_online);
                } else if (RainbowPresence.AWAY.equals(m_contact.getPresence()) || RainbowPresence.MANUAL_AWAY.equals(m_contact.getPresence())) {
                    // Away and manual away
                    m_presenceTextView.setTextColor(Color.parseColor("#FFA500"));
                    m_presenceTextView.setText(R.string.away);
                } else if (m_contact.getPresence() != null && m_contact.getPresence().toString().contains("busy")) {
                    // Busy / Busy audio / Busy video / Busy phone
                    m_presenceTextView.setTextColor(Color.RED);
                    m_presenceTextView.setText(R.string.busy);
                } else if (RainbowPresence.DND.equals(m_contact.getPresence())) {
                    // Do not disturb
                    m_presenceTextView.setTextColor(Color.RED);
                    m_presenceTextView.setText(R.string.do_not_disturb);
                } else if (RainbowPresence.DND_PRESENTATION.equals(m_contact.getPresence())) {
                    // On presentation
                    m_presenceTextView.setTextColor(Color.RED);
                    m_presenceTextView.setText(R.string.do_not_disturb_on_presentation);
                } else {
                    m_presenceTextView.setTextColor(Color.GRAY);
                    m_presenceTextView.setText(R.string.offline);
                }
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

    @Override
    public void contactUpdated(IRainbowContact iRainbowContact) {

    }

    @Override
    public void onPresenceChanged(IRainbowContact iRainbowContact, RainbowPresence rainbowPresence) {

    }

    @Override
    public void onCompanyChanged(String s) {

    }

    @Override
    public void onActionInProgress(boolean clickActionInProgress) {

    }
}

