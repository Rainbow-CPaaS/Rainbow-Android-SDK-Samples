package com.ale.conversationsDemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;

import java.util.List;

/**
 * Created by letrongh on 27/03/2017.
 */

public class ConversationsTabAdapter extends BaseAdapter {

    private ArrayItemList<IRainbowConversation> m_conversations = new ArrayItemList<>();
    private Context m_context;

    private IItemListChangeListener m_changeListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            if (m_context instanceof StartupActivity) {
                ((StartupActivity)m_context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateConversations();
                    }
                });
            }
        }
    };

    public ConversationsTabAdapter(Context context) {
        m_context = context;
        updateConversations();
    }

    @Override
    public int getCount() {
        return m_conversations.getCount();
    }

    @Override
    public Object getItem(int position) {
        return m_conversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyViewHolder myViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)m_context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_list_conversation, parent, false);

            myViewHolder = new MyViewHolder();
            myViewHolder.displayName = (TextView)convertView.findViewById(R.id.display_name);
            myViewHolder.lastMessage = (TextView)convertView.findViewById(R.id.last_message);
            myViewHolder.photo = (ImageView)convertView.findViewById(R.id.photo);
            myViewHolder.presence = (ImageView)convertView.findViewById(R.id.presence);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder)convertView.getTag();
        }

        IRainbowConversation conversation = (IRainbowConversation)getItem(position);
        if (conversation.isRoomType()) {
            myViewHolder.displayName.setText(conversation.getRoom().getName());
        } else {
            if (conversation.getContact() != null) {
                myViewHolder.displayName.setText(conversation.getContact().getFirstName() + " " + conversation.getContact().getLastName());
            }
        }

        myViewHolder.lastMessage.setText(conversation.getLastMessage().getMessageContent());

        if (conversation.getContact() != null) {
           if ( conversation.getContact().getPhoto() == null) {
                myViewHolder.photo.setImageResource(R.drawable.contact);
            } else {
                myViewHolder.photo.setImageBitmap(conversation.getContact().getPhoto());
            }

        }

        if (conversation.getContact() != null) {
            RainbowPresence presence = conversation.getContact().getPresence();
            if (presence != null) {
                if (presence.isOnline()) {
                    myViewHolder.presence.setBackgroundResource(R.drawable.online);
                } else if (presence.isMobileOnline()) {
                    myViewHolder.presence.setBackgroundResource(R.drawable.online_mobile);
                } else if (presence.isAway() || presence.isManualAway()) {
                    myViewHolder.presence.setBackgroundResource(R.drawable.away);
                } else if (presence.isDND() || presence.isBusy()) {
                    myViewHolder.presence.setBackgroundResource(R.drawable.do_not_disturb);
                } else {
                    myViewHolder.presence.setBackgroundResource(R.drawable.offline);
                }
            }
        }

        return convertView;
    }

    public void updateConversations() {
        m_conversations.clear();

        List<IRainbowConversation> conversations = RainbowSdk.instance().conversations().getAllConversations().getCopyOfDataList();
        m_conversations.replaceAll(conversations);
        notifyDataSetChanged();
    }

    private class MyViewHolder {
        TextView displayName;
        TextView lastMessage;
        ImageView photo;
        ImageView presence;
    }
}
