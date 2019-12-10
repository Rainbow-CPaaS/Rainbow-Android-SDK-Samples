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
import com.ale.infra.manager.channel.ChannelItem;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ChannelDetailAdapter extends RecyclerView.Adapter<ChannelDetailAdapter.ChannelDetailViewHold> {

    private List<ChannelItem> channelItems;

    public ChannelDetailAdapter(List<ChannelItem> channelItems) {
        this.channelItems = channelItems;
    }

    @NonNull
    @Override
    public ChannelDetailViewHold onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View rootVIew = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item_cardview, parent, false);
        return new ChannelDetailViewHold(rootVIew);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelDetailViewHold channelDetailViewHold, int position) {
        channelDetailViewHold.bind(channelItems.get(position));
    }

    @Override
    public int getItemCount() {
        return channelItems.size();
    }

    class ChannelDetailViewHold extends RecyclerView.ViewHolder {
        public ChannelDetailViewHold(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(ChannelItem channelItem) {
            String displayName = channelItem.getContact().getDisplayName("no display name");
            Date datetime = channelItem.getDate();
            ((TextView)itemView.findViewById(R.id.item_user_name)).setText(displayName);
            ((TextView)itemView.findViewById(R.id.item_datetime)).setText(DateFormat.getDateTimeInstance().format(datetime));
            ((TextView)itemView.findViewById(R.id.item_type)).setText("item type: " + channelItem.getType());
            ((TextView)itemView.findViewById(R.id.item_body)).setText(channelItem.getMessage());

            if (!channelItem.getAttachedFileList().isEmpty()) {
                String files;
                files = "Files:";
                for (RainbowFileDescriptor fileDescriptor : channelItem.getAttachedFileList()) {
                    files = files + "\n" + fileDescriptor.getFileName();
                }

                TextView filesTextView = itemView.findViewById(R.id.item_files);
                filesTextView.setText(files);
                filesTextView.setVisibility(View.VISIBLE);
            } else {
                itemView.findViewById(R.id.item_files).setVisibility(View.GONE);
            }

            Bitmap contactAvatar = channelItem.getContact().getPhoto();
            if (contactAvatar != null) {
                ((ImageView)itemView.findViewById(R.id.item_user_avatar)).setImageBitmap(contactAvatar);
            }
        }
    }
}

