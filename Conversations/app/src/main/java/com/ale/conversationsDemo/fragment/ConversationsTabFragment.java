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

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;
import com.ale.conversationsDemo.adapter.ConversationsTabAdapter;

/**
 * Created by letrongh on 10/04/2017.
 */

public class ConversationsTabFragment extends Fragment implements Contact.ContactListener {

    private static final String TAG = "ConversationsTabFragment";
    private StartupActivity m_activity;

    private ConversationsTabAdapter m_adapter;

    private IItemListChangeListener m_conversationsListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            m_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_adapter.updateConversations();
                    unregisterAllContacts();
                    registerContactsOfConversationsList();
                }
            });
        }
    };

    private IItemListChangeListener m_contactsListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            unregisterAllContacts();
            registerContactsOfConversationsList();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.conversations_tab_fragment, container, false);

        if (m_activity != null && m_activity.getSupportActionBar() != null) {
            m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.conversations));
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        ListView listViewConversations = (ListView)fragmentView.findViewById(R.id.list_view_conversations);
        listViewConversations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IRainbowConversation conversation = (IRainbowConversation)parent.getItemAtPosition(position);
                m_activity.openConversationFragment(conversation);
            }
        });

        m_adapter = new ConversationsTabAdapter(m_activity);
        listViewConversations.setAdapter(m_adapter);

        RainbowSdk.instance().conversations().getAllConversations().registerChangeListener(m_conversationsListener);
        RainbowSdk.instance().contacts().getRainbowContacts().registerChangeListener(m_contactsListener);
        registerContactsOfConversationsList();

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        RainbowSdk.instance().conversations().getAllConversations().unregisterChangeListener(m_conversationsListener);
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
     * Only listen to contacts of the list of conversations
     */
    private void registerContactsOfConversationsList() {
        for (IRainbowConversation conversation : RainbowSdk.instance().conversations().getAllConversations().getItems()) {
            if (conversation.getContact() != null) {
               conversation.getContact().registerChangeListener(this);
            }

        }
    }

    /**
     * Unregister all contacts
     */
    private void unregisterAllContacts() {
        for (IRainbowContact contact: RainbowSdk.instance().contacts().getRainbowContacts().getItems()) {
            contact.unregisterChangeListener(this);
        }
    }

    @Override
    public void contactUpdated(Contact updatedContact) {
        m_activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_adapter.updateConversations();
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
