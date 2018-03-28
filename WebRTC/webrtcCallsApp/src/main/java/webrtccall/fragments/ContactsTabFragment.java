package webrtccall.fragments;


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

import webrtccall.activities.StartupActivity;
import webrtccall.adapters.ContactsTabAdapter;
import webrtccall.callapplication.R;

public class ContactsTabFragment extends Fragment implements Contact.ContactListener {

    private StartupActivity m_activity;
    private ContactsTabAdapter m_adapter;

    private IItemListChangeListener m_contactsListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            if (m_activity != null)
            {
                m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_adapter.updateContacts();
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.contacts_tab_fragment, container, false);

        // Get the list view of all contacts
        ListView listViewContacts = (ListView)fragmentView.findViewById(R.id.list_view_contacts);
        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the IRainbowContact object in the adapter
                IRainbowContact contact = (IRainbowContact)parent.getItemAtPosition(position);

                if (!contact.isBot()) {
                    m_activity.openContactFragment(contact);
                }
            }
        });

        m_adapter = new ContactsTabAdapter(m_activity);
        listViewContacts.setAdapter(m_adapter);

        // Listen to contacts changes
        RainbowSdk.instance().contacts().getRainbowContacts().registerChangeListener(m_contactsListener);

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        // Un-listen to contacts changes
        RainbowSdk.instance().contacts().getRainbowContacts().unregisterChangeListener(m_contactsListener);

        super.onDestroyView();
    }

    @Override
    public void contactUpdated(Contact updatedContact) {
        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_adapter.updateContacts();
            }
        });
    }

    @Override
    public void onPresenceChanged(Contact contact, RainbowPresence presence) {

    }

    @Override
    public void onActionInProgress(boolean clickActionInProgress) {

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

