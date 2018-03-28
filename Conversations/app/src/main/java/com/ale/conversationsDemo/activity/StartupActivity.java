package com.ale.conversationsDemo.activity;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.manager.room.Room;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.listener.IRainbowGetConversationListener;
import com.ale.listener.SignoutResponseListener;
import com.ale.rainbowsdk.FileStorage;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.util.log.Log;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.fragment.ContactsTabFragment;
import com.ale.conversationsDemo.fragment.ConversationFragment;
import com.ale.conversationsDemo.fragment.ConversationsTabFragment;
import com.ale.conversationsDemo.fragment.LoginFragment;
import com.ale.conversationsDemo.fragment.SharedFilesFragment;

/**
 * A login screen that offers login via email/password.
 */
public class StartupActivity extends AppCompatActivity {
    private static final String TAG = "StartupActivity";

    private DrawerLayout m_drawerLayout;
    private StartupActivity m_activity;

    private ConversationFragment m_conversationFragment;

    private final static int PICK_FILE = 555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup_activity);

        m_activity = this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        createDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!RainbowSdk.instance().connection().isConnected()) {
            openLoginFragment();
        } else {
            unlockDrawer();
            if (getIntent() != null && "displayConversation".equals(getIntent().getAction())) {
                RainbowSdk.instance().conversations().getConversationFromContact(getIntent().getStringExtra("contactId"), new IRainbowGetConversationListener() {
                            @Override
                            public void onGetConversationSuccess(IRainbowConversation conversation) {
                                openConversationFragment(conversation);
                            }

                            @Override
                            public void onGetConversationError() {

                            }
                        }
                );
            }
        }
    }

    /**
     * Create the drawer with the presence and disconnecting items
     */
    private void createDrawer() {
        m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        String[] drawerItems = new String[]{"My shared files", "PresenceToOnline", "PresenceToAway", "PresenceToInvisible", "PresenceToDnD", "Disconnect"};
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerItems));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (RainbowSdk.instance().connection().isConnected()) {
                    switch (position) {
                        case 0:
                            openSharedFilesFragment(FileStorage.GetMode.ALL_FILES_SENT);
                            break;
                        case 1:
                            setPresenceTo(RainbowPresence.ONLINE);
                            break;
                        case 2:
                            setPresenceTo(RainbowPresence.AWAY);
                            break;
                        case 3:
                            setPresenceTo(RainbowPresence.OFFLINE);
                            break;
                        case 4:
                            setPresenceTo(RainbowPresence.DND);
                            break;
                        case 5:
                            signout();
                            break;
                        default:
                            break;
                    }
                } else {
                    Toast.makeText(m_activity, m_activity.getResources().getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
                }
                m_drawerLayout.closeDrawers();
            }
        });
    }

    public void unlockDrawer() {
        m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void setPresenceTo(RainbowPresence rainbowPresence) {
        RainbowSdk.instance().myProfile().setPresenceTo(rainbowPresence);
    }

    private void signout() {
        RainbowSdk.instance().connection().signout(new SignoutResponseListener() {
            @Override
            public void onSignoutSucceeded() {
                finish();
            }

            @Override
            public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
                Log.getLogger().info(TAG, "Failed to signout");
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
     * Open the ConversationFramgnet with the conversation in parameter
     * @param conversation      IRainbowConversation to display
     */
    public void openConversationFragment(IRainbowConversation conversation) {
        m_conversationFragment = new ConversationFragment();
        m_conversationFragment.setConversation(conversation);
        openFragment(m_conversationFragment, true);
    }

    /**
     * Open the ConversationsTabFragment (list of conversations)
     */
    public void openConversationsTabFragment() {
        ConversationsTabFragment fragment = new ConversationsTabFragment();
        openFragment(fragment, false);
    }

    /**
     * Open the ContactsTabFragment (list of contacts/rosters)
     */
    public void openContactsTabFragment() {
        ContactsTabFragment fragment = new ContactsTabFragment();
        openFragment(fragment, false);
    }

    /**
     * Open the LoginFragment
     */
    public void openLoginFragment() {
        LoginFragment fragment = new LoginFragment();
        openFragment(fragment, false);
    }

    public void openSharedFilesFragment(FileStorage.GetMode mode) {
        SharedFilesFragment fragment = new SharedFilesFragment();
        fragment.setMode(mode);
        openFragment(fragment, true);
    }

    public void openSharedFilesFragment(FileStorage.GetMode mode, IRainbowConversation conversation) {
        SharedFilesFragment fragment = new SharedFilesFragment();
        fragment.setMode(mode);
        fragment.setConversation(conversation);
        openFragment(fragment, true);
    }

    public void openSharedFilesFragment(FileStorage.GetMode mode, Room room) {
        SharedFilesFragment fragment = new SharedFilesFragment();
        fragment.setMode(mode);
        fragment.setRoom(room);
        openFragment(fragment, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.conversations:
                if (RainbowSdk.instance().connection().isConnected()) {
                    openConversationsTabFragment();
                }
                return true;
            case R.id.contacts:
                if (RainbowSdk.instance().connection().isConnected()) {
                    openContactsTabFragment();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case PICK_FILE:
                m_conversationFragment.onActivityResult(requestCode, resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void openFile(final RainbowFileDescriptor fileDescriptor) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(fileDescriptor.getFile()), fileDescriptor.getTypeMIME());

        if (!getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot open this file", Toast.LENGTH_SHORT).show();
        }
    }
}

