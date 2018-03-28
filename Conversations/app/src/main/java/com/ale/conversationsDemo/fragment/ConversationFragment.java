package com.ale.conversationsDemo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.listener.IRainbowImListener;
import com.ale.rainbowsdk.FileStorage;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;
import com.ale.conversationsDemo.adapter.ConversationAdapter;

import java.util.List;

/**
 * COnversation fragment.
 */

public class ConversationFragment extends Fragment implements IRainbowImListener {

    private static final String TAG = "ConversationFragment";

    private static final int NB_MESSAGES_TO_RETRIEVE = 10;

    private SwipeRefreshLayout m_swipeContainer = null;
    private ListView m_listViewMessages;
    private StartupActivity m_activity;
    private EditText m_editTextMessage;

    private IRainbowConversation m_conversation;
    private ConversationAdapter m_adapter;

    private static final int PICK_FILE = 555;

    private IItemListChangeListener m_changeListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            m_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_adapter.setMessages(m_conversation.getMessages());
                    m_adapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.conversation_fragment, container, false);

        if (m_activity != null && m_activity.getSupportActionBar() != null) {
            // Set presence in the ActionBar
            if (m_conversation.getContact() != null && m_conversation.getContact().getPresence() != null) {
                IRainbowContact contact = m_conversation.getContact();
                m_activity.getSupportActionBar().setTitle(contact.getFirstName() + " " + contact.getLastName() + " (" + contact.getPresence().toString() + ")");
            }
            m_activity.getSupportActionBar().setHomeButtonEnabled(true);
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        m_adapter = new ConversationAdapter(getActivity(), m_conversation);
        m_listViewMessages = (ListView)fragmentView.findViewById(R.id.list_view_messages);
        m_listViewMessages.setAdapter(m_adapter);

        m_swipeContainer = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipeContainer);
        m_swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.getLogger().verbose(TAG, "onRefresh");
                RainbowSdk.instance().im().getMoreMessagesFromConversation(m_conversation, NB_MESSAGES_TO_RETRIEVE);
            }
        });

        RainbowSdk.instance().im().getMessagesFromConversation(m_conversation, NB_MESSAGES_TO_RETRIEVE);
        m_conversation.getMessages().registerChangeListener(m_changeListener);

        m_editTextMessage = (EditText)fragmentView.findViewById(R.id.edit_text_message);

        Button sendMessageButton = (Button)fragmentView.findViewById(R.id.button_send_message);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().im().sendMessageToConversation(m_conversation, m_editTextMessage.getText().toString());
                m_editTextMessage.getText().clear();
            }
        });

        FloatingActionButton attachmentButton = (FloatingActionButton)fragmentView.findViewById(R.id.fab_attach_file);
        attachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE);
            }
        });
        attachmentButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (m_conversation.isRoomType()) {
                    m_activity.openSharedFilesFragment(FileStorage.GetMode.ALL_FILES_SENT_IN_BUBBLE, m_conversation.getRoom());
                } else {
                    m_activity.openSharedFilesFragment(FileStorage.GetMode.ALL_FILES_SENT_IN_CONVERSATION, m_conversation);
                }
                return true;
            }
        });

        RainbowSdk.instance().im().markMessagesFromConversationAsRead(m_conversation);

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        m_conversation.getMessages().registerChangeListener(m_changeListener);

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        RainbowSdk.instance().im().registerListener(this);
        RainbowSdk.instance().im().markMessagesFromConversationAsRead(m_conversation);
    }

    @Override
    public void onPause() {
        RainbowSdk.instance().im().unregisterListener(this);
        super.onPause();
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

    public void setConversation(IRainbowConversation conversation) {
        m_conversation = conversation;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            RainbowSdk.instance().fileStorage().uploadFileToConversation(m_conversation, data.getData(), m_editTextMessage.getText().toString(), new IFileProxy.IUploadFileListener() {
                @Override
                public void onUploadSuccess(RainbowFileDescriptor fileDescriptor) {

                }

                @Override
                public void onUploadInProgress(int percent) {

                }

                @Override
                public void onUploadFailed(RainbowServiceException exception) {

                }
            });
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Listeners on IM events
    @Override
    public void onImReceived(String conversationId, IMMessage message) {
        RainbowSdk.instance().im().markMessagesFromConversationAsRead(m_conversation);
    }
    @Override
    public void onImSent(String conversationId, IMMessage message) {

    }

    @Override
    public void isTypingState(IRainbowContact other, boolean isTyping, String roomId) {
        // Add the layout "... is typing" for example
    }

    @Override
    public void onMessagesListUpdated(int status, String conversationId, List<IMMessage> messages) {
        if (conversationId.equals(m_conversation.getId())) {
            m_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_swipeContainer.setRefreshing(false);
                }
            });
            RainbowSdk.instance().im().markMessagesFromConversationAsRead(m_conversation);
        }
    }


    @Override
    public void onMoreMessagesListUpdated(int status, String conversationId, final List<IMMessage> messages) {
        if (conversationId.equals(m_conversation.getId())) {
            m_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_swipeContainer.setRefreshing(false);
                    if (m_listViewMessages != null) {
                        m_listViewMessages.post(new Runnable() {
                            @Override
                            public void run() {
                                m_listViewMessages.setSelection(0);
                            }
                        });
                    }
                }
            });
        }
    }
}
