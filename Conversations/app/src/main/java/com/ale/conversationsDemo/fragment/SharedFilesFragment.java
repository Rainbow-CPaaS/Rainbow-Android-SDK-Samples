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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.room.Room;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.FileStorage;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;
import com.ale.conversationsDemo.adapter.SharedFilesAdapter;

public class SharedFilesFragment extends Fragment {

    private ListView m_listViewFilesShared;
    private SharedFilesAdapter m_adapter;
    private StartupActivity m_activity;
    private FileStorage.GetMode m_mode;

    private IRainbowConversation m_conversation = null;
    private Room m_room = null;

    private IItemListChangeListener m_changeListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            m_adapter.updateListToDisplay();
            m_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_adapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.shared_files_fragment, container, false);

        if (m_activity != null && m_activity.getSupportActionBar() != null) {
            if (m_conversation != null) {
                IRainbowContact contact = m_conversation.getContact();
                m_activity.getSupportActionBar().setTitle("Shared files (" + contact.getFirstName() + " " + contact.getLastName() + ")");
            } else if (m_room != null) {
                m_activity.getSupportActionBar().setTitle("Shared files (" + m_room.getDisplayName("") + ")");
            } else {
                m_activity.getSupportActionBar().setTitle("My shared files");
            }

            m_activity.getSupportActionBar().setHomeButtonEnabled(true);
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        m_listViewFilesShared = (ListView)fragmentView.findViewById(R.id.list_view_files_shared);
        m_listViewFilesShared.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        final Switch switchSentReceived = (Switch)fragmentView.findViewById(R.id.switch_sent_received);
        switchSentReceived.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) { // Sent
                    if (m_conversation != null) {
                        m_adapter.setConversation(m_conversation);
                        m_adapter.setMode(FileStorage.GetMode.ALL_FILES_SENT_IN_CONVERSATION);
                    } else if (m_room != null) {
                        m_adapter.setRoom(m_room);
                        m_adapter.setMode(FileStorage.GetMode.ALL_FILES_SENT_IN_BUBBLE);
                    } else {
                        m_adapter.setMode(FileStorage.GetMode.ALL_FILES_SENT);
                    }
                } else { // Received
                    if (m_conversation != null) {
                        m_adapter.setConversation(m_conversation);
                        m_adapter.setMode(FileStorage.GetMode.ALL_FILES_RECEIVED_IN_CONVERSATION);
                    } else if (m_room != null) {
                        m_adapter.setRoom(m_room);
                        m_adapter.setMode(FileStorage.GetMode.ALL_FILES_RECEIVED_IN_BUBBLE);
                    } else {
                        m_adapter.setMode(FileStorage.GetMode.ALL_FILES_RECEIVED);
                    }
                }

                m_adapter.updateListToDisplay();
                m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        fragmentView.findViewById(R.id.text_view_switch_sent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSentReceived.setChecked(false);
            }
        });

        fragmentView.findViewById(R.id.text_view_switch_received).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSentReceived.setChecked(true);
            }
        });

        if (m_conversation != null) {
            m_adapter = new SharedFilesAdapter(m_activity, m_mode, m_conversation);
        } else if (m_room != null) {
            m_adapter = new SharedFilesAdapter(m_activity, m_mode, m_room);
        } else {
            m_adapter = new SharedFilesAdapter(m_activity, m_mode);
        }

        m_listViewFilesShared.setAdapter(m_adapter);

        RainbowSdk.instance().fileStorage().getAllFilesSent().registerChangeListener(m_changeListener);
        RainbowSdk.instance().fileStorage().getAllFilesReceived().registerChangeListener(m_changeListener);

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        RainbowSdk.instance().fileStorage().getAllFilesSent().unregisterChangeListener(m_changeListener);
        RainbowSdk.instance().fileStorage().getAllFilesReceived().unregisterChangeListener(m_changeListener);

        super.onDestroyView();
    }

    public void setConversation(IRainbowConversation conversation) {
        m_conversation = conversation;
    }

    public void setRoom(Room room) {
        m_room = room;
    }

    public void setMode(FileStorage.GetMode mode) {
        m_mode = mode;
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
