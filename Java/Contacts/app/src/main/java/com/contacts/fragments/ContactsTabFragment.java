package com.contacts.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.contacts.R;
import com.contacts.activities.StartupActivity;
import com.contacts.adapters.ContactsTabAdapter;


public class ContactsTabFragment extends Fragment implements IRainbowContact.IContactListener {

    private StartupActivity m_activity;
    private ContactsTabAdapter m_adapter;

    private IItemListChangeListener m_contactsListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            m_adapter.updateContacts();
            registerAllContacts();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.contacts_tab_fragment, container, false);
        if (m_activity.getSupportActionBar() != null) {
            m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.contacts));
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        m_adapter = new ContactsTabAdapter(m_activity);

        ListView m_listViewContacts = fragmentView.findViewById(R.id.list_view_contacts);
        m_listViewContacts.setAdapter(m_adapter);

        m_listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                IRainbowContact contact = (IRainbowContact)m_adapter.getItem(position);

                if (contact != null) {
                    m_activity.openContactFragment(contact);
                }
            }
        });

        RainbowSdk.instance().contacts().getRainbowContacts().registerChangeListener(m_contactsListener);
        registerAllContacts();

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        RainbowSdk.instance().contacts().getRainbowContacts().unregisterChangeListener(m_contactsListener);
        unregisterAllContacts();

        super.onDestroyView();
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

    /**
     * Listen to all rainbow contacts
     */
    private void registerAllContacts() {
        for (IRainbowContact contact : RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList()) {
            contact.registerChangeListener(this);
        }
    }

    /**
     * Unregister all contacts
     */
    private void unregisterAllContacts() {
        for (IRainbowContact contact : RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList()) {
            contact.unregisterChangeListener(this);
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

