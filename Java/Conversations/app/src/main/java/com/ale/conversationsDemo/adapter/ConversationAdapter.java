package com.ale.conversationsDemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends BaseAdapter {

    private Context m_context;
    private IRainbowConversation m_conversation;
    private ArrayItemList<IMMessage> m_messages;
    private IRainbowContact m_me;


    public ConversationAdapter(Context context, IRainbowConversation conversation) {
        m_context = context;
        m_conversation = conversation;
        m_messages = new ArrayItemList<>();
        onMessagesListUpdated();
        m_me = RainbowSdk.instance().myProfile().getConnectedUser();
    }

    private void onMessagesListUpdated() {
        updateMessagesList(m_conversation.getMessages().getCopyOfDataList());

    }

    public void onMoreMessagesListUpdated(List<IMMessage> messages) {
        updateMessagesList(messages);

    }

    private void updateMessagesList(List<IMMessage> messages) {
        m_messages.clear();
        for (IMMessage message : messages) {
            m_messages.add(message);
        }
    }

    @Override
    public int getCount() {
            return m_messages.getCount();
    }

    @Override
    public Object getItem(int position) {
        return m_messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyViewHolder myViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_list_message, parent, false);

            myViewHolder = new MyViewHolder();
            myViewHolder.content = (TextView)convertView.findViewById(R.id.content_message);
            myViewHolder.messageDate = (TextView)convertView.findViewById(R.id.message_date);
            myViewHolder.photo = (ImageView)convertView.findViewById(R.id.photo);
            myViewHolder.messageState = (TextView)convertView.findViewById(R.id.message_state);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder)convertView.getTag();
        }

        IMMessage message = (IMMessage)getItem(position);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(message.getMessageDate());
        String timeToDisplay = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        if (DateUtils.isToday(calendar.getTimeInMillis())) {
            myViewHolder.messageDate.setText(timeToDisplay);
        } else {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
            myViewHolder.messageDate.setText(dateFormat.format(calendar.getTime()) + " (" + timeToDisplay + ")");
        }

        if (message.isFileDescriptorAvailable()) {
            myViewHolder.content.setText("A file has been shared");
        } else {
            myViewHolder.content.setText(message.getMessageContent());
        }

        if (!m_conversation.isRoomType()) {
            if (!message.isMsgSent()) {
                myViewHolder.photo.setImageBitmap(m_conversation.getContact().getPhoto());
            } else {
                myViewHolder.photo.setImageBitmap(m_me.getPhoto());
            }
        }


        IMMessage.DeliveryState deliveryState = message.getDeliveryState();
        if (deliveryState.equals(IMMessage.DeliveryState.SENT)) {
            myViewHolder.messageState.setText(R.string.sent);
        } else if (deliveryState.equals(IMMessage.DeliveryState.SENT_SERVER_RECEIVED)) {
            myViewHolder.messageState.setText(R.string.sent_server_received);
        } else if (deliveryState.equals(IMMessage.DeliveryState.SENT_CLIENT_RECEIVED)) {
            myViewHolder.messageState.setText(R.string.sent_client_received);
        } else if (deliveryState.equals(IMMessage.DeliveryState.SENT_CLIENT_READ)) {
            myViewHolder.messageState.setText(R.string.sent_client_read);
        } else {
            myViewHolder.messageState.setText("");
        }

        return convertView;
    }

    private class MyViewHolder {
        ImageView photo;
        TextView content;
        TextView messageDate;
        TextView messageState;
    }

    public void addMessage(IMMessage message) {
        m_messages.add(message);
    }

    public void setMessages(ArrayItemList<IMMessage> messages) {
        m_messages.replaceAll(messages.getCopyOfDataList());
    }


}
