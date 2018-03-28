package com.ale.conversationsDemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.infra.list.ArrayItemList;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;



public class ContactsTabAdapter extends BaseAdapter {

    private ArrayItemList<IRainbowContact> m_roster = new ArrayItemList<>();
    private Context m_context;

    public ContactsTabAdapter(Context context) {
        m_context = context;
        updateContacts();
    }

    @Override
    public int getCount() {
        if (m_roster != null)
        {
            return m_roster.getCount();
        }
        return 0;

    }

    @Override
    public Object getItem(int position) {
        return m_roster.get(position);
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
            convertView = inflater.inflate(R.layout.row_list_contact, parent, false);

            myViewHolder = new MyViewHolder();
            myViewHolder.displayName = (TextView)convertView.findViewById(R.id.display_name);
            myViewHolder.company = (TextView)convertView.findViewById(R.id.company);
            myViewHolder.photo = (ImageView)convertView.findViewById(R.id.photo);
            myViewHolder.presence = (ImageView)convertView.findViewById(R.id.presence);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder)convertView.getTag();
        }

        IRainbowContact contact = (IRainbowContact)getItem(position);
        myViewHolder.displayName.setText(contact.getFirstName() + " " + contact.getLastName());
        myViewHolder.company.setText(contact.getCompanyName());
        if (contact.getPhoto() == null) {
            myViewHolder.photo.setImageResource(R.drawable.contact);
        } else {
            myViewHolder.photo.setImageBitmap(contact.getPhoto());
        }

        RainbowPresence presence = contact.getPresence();
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

        return convertView;
    }

    public void updateContacts() {
        m_roster = RainbowSdk.instance().contacts().getRainbowContacts();
        notifyDataSetChanged();
    }

    private class MyViewHolder {
        TextView displayName;
        TextView company;
        ImageView photo;
        ImageView presence;
    }
}
