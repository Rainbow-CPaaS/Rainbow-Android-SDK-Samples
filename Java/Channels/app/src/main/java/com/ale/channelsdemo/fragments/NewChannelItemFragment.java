package com.ale.channelsdemo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.activities.StartupActivity;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.channel.Channel;
import com.ale.infra.manager.channel.ChannelItem;
import com.ale.infra.manager.channel.ChannelItemBuilder;
import com.ale.infra.proxy.channel.IChannelProxy;
import com.ale.rainbowsdk.RainbowSdk;

public class NewChannelItemFragment extends Fragment {

    private StartupActivity m_activity;
    private Channel channel;

    private static final String ARG_CHANNEL_ID = "Arg1";

    public static NewChannelItemFragment newInstance(Channel channel) {
        NewChannelItemFragment newChannelItemFragment = new NewChannelItemFragment();
        newChannelItemFragment.setHasOptionsMenu(true);
        Bundle args = new Bundle();
        args.putString(ARG_CHANNEL_ID, channel.getId());
        newChannelItemFragment.setArguments(args);
        return newChannelItemFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_activity = (StartupActivity) getActivity();

        if (getArguments() != null) {
            String channelId = getArguments().getString(ARG_CHANNEL_ID);
            channel = RainbowSdk.instance().channels().getChannel(channelId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.create_new_post));
        return inflater.inflate(R.layout.new_channelitem_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_create).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create) {
            Editable message = ((EditText) m_activity.findViewById(R.id.new_channelitem_body)).getText();
            if (message != null && !message.toString().isEmpty()) {
                ChannelItemBuilder channelItemBuilder = new ChannelItemBuilder()
                        .setMessage(message.toString());
                RainbowSdk.instance().channels().createItem(channel, channelItemBuilder.build(), new IChannelProxy.IChannelCreateItemListener() {
                    @Override
                    public void onCreateMessageItemSuccess(String status) {
                        m_activity.runOnUiThread(() -> Toast.makeText(m_activity, R.string.message_sent, Toast.LENGTH_SHORT).show());
                        getFragmentManager().popBackStack();
                    }

                    @Override
                    public void onCreateMessageItemFailed(RainbowServiceException exception) {
                        m_activity.runOnUiThread(() -> Toast.makeText(m_activity, R.string.message_fail, Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                Toast.makeText(m_activity, R.string.empty_message, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
