package com.ale.channelsdemo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.activities.StartupActivity;
import com.ale.channelsdemo.adapters.ChannelDetailAdapter;
import com.ale.infra.manager.channel.Channel;
import com.ale.infra.manager.channel.ChannelItem;
import com.ale.rainbowsdk.RainbowSdk;

import java.util.ArrayList;
import java.util.List;

public class ChannelDetailFragment extends Fragment {

    private StartupActivity m_activity;
    private RecyclerView channelRecyclerView;
    private List<ChannelItem> channelItems = new ArrayList<>();
    private Channel channel;

    private static final String ARG_CHANNEL_NAME = "Arg1";

    public static ChannelDetailFragment newInstance(Channel channel) {
        ChannelDetailFragment channelDetailFragment = new ChannelDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHANNEL_NAME, channel.getName());
        channelDetailFragment.setArguments(args);
        return channelDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_activity = (StartupActivity) getActivity();

        if (getArguments() != null) {
            String channelName = getArguments().getString(ARG_CHANNEL_NAME);
            channel = RainbowSdk.instance().channels().getChannelByName(channelName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.channel) + ": " + channel.getDisplayName(""));

        channelItems.clear();
        channelItems.addAll(channel.getChannelItems().getCopyOfDataList());

        View fragmentView = inflater.inflate(R.layout.channel_fragment, container, false);
        channelRecyclerView = fragmentView.findViewById(R.id.channel_recyclerview);
        channelRecyclerView.setLayoutManager(new LinearLayoutManager(m_activity));
        channelRecyclerView.setAdapter(new ChannelDetailAdapter(channelItems));

        channel.getChannelItems().registerChangeListener(() -> {
            channelItems.clear();
            channelItems.addAll(channel.getChannelItems().getCopyOfDataList());
            m_activity.runOnUiThread(() -> channelRecyclerView.getAdapter().notifyDataSetChanged());
        });

        FloatingActionButton fab = fragmentView.findViewById(R.id.fab_new_channelitem);
        if (channel.canPublish()) {
            fab.setOnClickListener(v -> m_activity.openNewChannelItemFragment(channel));
        } else {
            fab.hide();
        }

        return fragmentView;
    }
}
