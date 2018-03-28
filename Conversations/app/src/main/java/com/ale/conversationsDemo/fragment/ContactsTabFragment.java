package com.ale.conversationsDemo.fragment;

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
import android.widget.Toast;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.listener.IRainbowGetConversationListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;
import com.ale.conversationsDemo.adapter.ContactsTabAdapter;


public class ContactsTabFragment extends Fragment implements Contact.ContactListener {

    private static final String TAG = "ContactsTabFragment";
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
                        registerAllContacts();
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.contacts_tab_fragment, container, false);

        if (m_activity.getSupportActionBar() != null) {
            m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.contacts));
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        ListView listViewContacts = (ListView)fragmentView.findViewById(R.id.list_view_contacts);
        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final IRainbowContact contact = (IRainbowContact)parent.getItemAtPosition(position);

                RainbowSdk.instance().conversations().getConversationFromContact(contact.getImJabberId(), new IRainbowGetConversationListener() {
                   @Override
                    public void onGetConversationSuccess(IRainbowConversation conversation) {
                       if (getActivity() instanceof StartupActivity) {
                           ((StartupActivity)getActivity()).openConversationFragment(conversation);
                       }
                   }

                   @Override
                    public void onGetConversationError() {
                       Toast.makeText(m_activity, "Cannot get the conversation for: " + contact.getFirstName() + " " + contact.getLastName(), Toast.LENGTH_SHORT).show();
                   }
                });
            }
        });

        m_adapter = new ContactsTabAdapter(m_activity);
        listViewContacts.setAdapter(m_adapter);

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
        for (IRainbowContact contact : RainbowSdk.instance().contacts().getRainbowContacts().getItems()) {
            contact.registerChangeListener(this);
        }
    }

    /**
     * Unregister all contacts
     */
    private void unregisterAllContacts() {
        for (IRainbowContact contact : RainbowSdk.instance().contacts().getRainbowContacts().getItems()) {
            contact.unregisterChangeListener(this);
        }
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
}
