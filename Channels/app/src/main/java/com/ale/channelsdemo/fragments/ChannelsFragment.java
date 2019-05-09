package com.ale.channelsdemo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.activities.StartupActivity;
import com.ale.channelsdemo.adapters.ChannelsAdapter;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.channel.Channel;
import com.ale.rainbowsdk.RainbowSdk;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChannelsFragment extends Fragment {

    private StartupActivity m_activity;
    private RecyclerView channelsRecyclerView;
    private List<Channel> channels = new ArrayList<>();

    public static ChannelsFragment newInstance() {
        return new ChannelsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_activity = (StartupActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        m_activity.getSupportActionBar().setTitle(m_activity.getResources().getString(R.string.channels));

        View fragmentView = inflater.inflate(R.layout.channels_fragment, container, false);
        channelsRecyclerView = fragmentView.findViewById(R.id.channels_recyclerview);
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(m_activity));
        channelsRecyclerView.setAdapter(new ChannelsAdapter(channels, m_activity));

        ArrayItemList<Channel> allChannels = RainbowSdk.instance().channels().getAllChannels();
        allChannels.registerChangeListener(() -> {
            channels.clear();
            channels.addAll(allChannels.getCopyOfDataList());
            m_activity.runOnUiThread(() -> channelsRecyclerView.getAdapter().notifyDataSetChanged());
        });

        FloatingActionButton fab = fragmentView.findViewById(R.id.fab_new_channel);
        fab.setOnClickListener(v -> m_activity.openNewChannelFragment());

        return fragmentView;
    }
}
