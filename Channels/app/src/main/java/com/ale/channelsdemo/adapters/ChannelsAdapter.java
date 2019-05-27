package com.ale.channelsdemo.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.activities.StartupActivity;
import com.ale.infra.manager.channel.Channel;

import java.util.List;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {

    private List<Channel> channelsList;
    private StartupActivity m_activity;

    public ChannelsAdapter(List<Channel> channelsList, StartupActivity m_activity) {
        this.channelsList = channelsList;
        this.m_activity = m_activity;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.channels_row, parent, false);
        return new ChannelViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder channelViewHolder, int position) {
        channelViewHolder.bind(channelsList.get(position));
    }

    @Override
    public int getItemCount() {
        return channelsList.size();
    }

    class ChannelViewHolder extends RecyclerView.ViewHolder {
        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setTag(this);
            itemView.setOnClickListener(view -> {
                ChannelViewHolder viewHolder = (ChannelViewHolder) view.getTag();
                int position = viewHolder.getAdapterPosition();
                m_activity.openChannelDetailFragment(channelsList.get(position));
            });
        }

        public void bind(Channel channel) {
            String name = channel.getDisplayName("no channel name");
            String category = channel.getCategory();
            String topic = channel.getDescription();
            int subscribersCount = channel.getNbSubscriberUsers();
            Bitmap avatar = channel.getChannelAvatar();

            String description;
            if (category != null && !category.isEmpty()) {
                description = "[" + category + "] " + topic;
            } else {
                description = topic;
            }

            ((TextView) itemView.findViewById(R.id.channel_name)).setText(name);
            ((TextView) itemView.findViewById(R.id.channel_detail)).setText(description);
            ((TextView) itemView.findViewById(R.id.channel_subscribers)).setText(String.valueOf(subscribersCount));
            if (avatar != null) {
                ((ImageView) itemView.findViewById(R.id.channel_avatar)).setImageBitmap(avatar);
            }
        }
    }
}
