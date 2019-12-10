package com.ale.channelsdemo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.activities.StartupActivity;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.channel.Channel;
import com.ale.infra.proxy.channel.IChannelProxy;
import com.ale.rainbowsdk.RainbowSdk;


public class NewChannelFragment extends Fragment {

    private StartupActivity m_activity;

    public static NewChannelFragment newInstance() {
        NewChannelFragment newChannelFragment = new NewChannelFragment();
        newChannelFragment.setHasOptionsMenu(true);
        return newChannelFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_activity = (StartupActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_activity.getSupportActionBar().setTitle("Create new channel");
        return inflater.inflate(R.layout.new_channel_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_create).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create) {
            createNewChannel();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void createNewChannel() {
        String name = ((EditText) m_activity.findViewById(R.id.newchannel_name)).getText().toString();
        String description = ((EditText) m_activity.findViewById(R.id.newchannel_topic)).getText().toString();
        String category = ((EditText) m_activity.findViewById(R.id.newchannel_category)).getText().toString();
        int channelMode = ((RadioGroup) m_activity.findViewById(R.id.newchannel_mode_radiogroup)).getCheckedRadioButtonId();

        if (channelMode == R.id.newchannel_mode_company_closed) {
            boolean autoProvisioning = ((CheckBox) m_activity.findViewById(R.id.newchannel_autoprovisioning)).isChecked();
            RainbowSdk.instance().channels().createClosedChannel(name, description, category, autoProvisioning,100, new IChannelProxy.IChannelCreateListener() {
                @Override
                public void onCreateSuccess(Channel channel) {
                    m_activity.runOnUiThread(() -> Toast.makeText(m_activity, R.string.channel_created, Toast.LENGTH_SHORT).show());
                    getFragmentManager().popBackStack();
                }

                @Override
                public void onCreateFailed(RainbowServiceException exception) {
                    m_activity.runOnUiThread(() -> Toast.makeText(m_activity, R.string.channel_fail, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            RainbowSdk.instance().channels().createPublicChannel(name, description, category, 100, new IChannelProxy.IChannelCreateListener() {
                @Override
                public void onCreateSuccess(Channel channel) {
                    m_activity.runOnUiThread(() -> Toast.makeText(m_activity, R.string.channel_created, Toast.LENGTH_SHORT).show());
                    getFragmentManager().popBackStack();
                }

                @Override
                public void onCreateFailed(RainbowServiceException exception) {
                    m_activity.runOnUiThread(() -> Toast.makeText(m_activity, R.string.channel_fail, Toast.LENGTH_SHORT).show());
                }
            });
        }


    }
}
