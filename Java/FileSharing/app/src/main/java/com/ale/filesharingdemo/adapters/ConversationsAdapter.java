package com.ale.filesharingdemo.adapters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.filesharingdemo.R;
import com.ale.filesharingdemo.activities.StartupActivity;
import com.ale.filesharingdemo.interfaces.RecyclerViewClickListener;
import com.ale.infra.proxy.conversation.IRainbowConversation;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private StartupActivity activity;
    private List<IRainbowConversation> conversationsList;
    private RecyclerViewClickListener itemListener;

    public ConversationsAdapter(List<IRainbowConversation> conversationsList, StartupActivity activity, RecyclerViewClickListener itemListener) {
        this.conversationsList = conversationsList;
        this.activity = activity;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversations_row, parent, false);
        return new ConversationViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder conversationViewHolder, int position) {
        conversationViewHolder.bind(conversationsList.get(position));
    }

    @Override
    public int getItemCount() {
        return conversationsList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setTag(this);
            itemView.setOnClickListener(this);
        }

        void bind(IRainbowConversation conversation) {

            String name;
            Bitmap photo;
            String presence;

           if (conversation.isRoomType()) {
               name = conversation.getRoom().getName();
               presence = conversation.getRoom().getTopic() != null ? conversation.getRoom().getTopic() : "";
               photo = conversation.getRoom().getPhoto();
           } else {
               name = conversation.getContact().getFirstName() + " " + conversation.getContact().getLastName();
               presence = conversation.getContact().getPresence().getPresence();
               photo = conversation.getContact().getPhoto();
           }

            // Create Bitmap with initials if photo is undefined
            if (photo == null) {
                Drawable drawable = activity.getResources().getDrawable(R.drawable.circle);
                photo = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(photo);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(canvas);

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(48);
                paint.setColor(Color.WHITE);

                if(conversation.isRoomType()) {
                    canvas.drawText(name.substring(0, 1).toUpperCase(), (photo.getWidth() / 2) - 15, (photo.getHeight() / 2) + 15, paint);
                } else {
                    canvas.drawText(name.substring(0, 1) + conversation.getContact().getLastName().substring(0, 1), (photo.getWidth() / 2) - 30, (photo.getHeight() / 2) + 12, paint);
                }
            }
            ((TextView) itemView.findViewById(R.id.conversation_name)).setText(name);
            ((TextView) itemView.findViewById(R.id.conversation_presence)).setText(presence);
            ((ImageView) itemView.findViewById(R.id.conversation_photo)).setImageBitmap(photo);
        }

        @Override
        public void onClick(View view) {
            itemListener.recyclerViewListClicked(view, getLayoutPosition());
        }
    }
}
