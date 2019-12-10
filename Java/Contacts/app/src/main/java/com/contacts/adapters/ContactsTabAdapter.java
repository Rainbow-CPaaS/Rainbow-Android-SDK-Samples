package com.contacts.adapters;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.ale.util.StringsUtil;
import com.contacts.R;
import com.contacts.activities.StartupActivity;

public class ContactsTabAdapter extends BaseAdapter {

    private ArrayItemList<IRainbowContact> m_contacts = new ArrayItemList<>();
    private Context m_context;

    public ContactsTabAdapter(Context context) {
        m_context = context;
        updateContacts();
    }

    @Override
    public int getCount() {
        return m_contacts.getCount();
    }

    @Override
    public Object getItem(int position) {
        return m_contacts.get(position);
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
            myViewHolder.displayName = convertView.findViewById(R.id.display_name);
            myViewHolder.company = convertView.findViewById(R.id.company);
            myViewHolder.photo = convertView.findViewById(R.id.photo_contact);
            myViewHolder.presence = convertView.findViewById(R.id.presence_text_view);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder)convertView.getTag();
        }

        // Get the IRainbowContact object
        IRainbowContact contact = (IRainbowContact)getItem(position);

        // Set the display name
        String displayName = contact.getLoginEmail();
        if (!StringsUtil.isNullOrEmpty(contact.getFirstName()))
            displayName = contact.getFirstName() + " " + contact.getLastName();

        myViewHolder.displayName.setText(displayName);

        // Set the photo if found
        if (contact.getPhoto() == null) {
            myViewHolder.photo.setImageResource(R.drawable.icon_contact);
        } else {
            myViewHolder.photo.setImageBitmap(contact.getPhoto());
        }

        // Set the company
        myViewHolder.company.setText(contact.getCompanyName());

        // Set the presence
        if (RainbowPresence.ONLINE.equals(contact.getPresence())) {
            // Online
            myViewHolder.presence.setTextColor(Color.GREEN);
            myViewHolder.presence.setText(R.string.online);
        } else if (RainbowPresence.MOBILE_ONLINE.equals(contact.getPresence())) {
            // Online but on mobile
            myViewHolder.presence.setTextColor(Color.BLUE);
            myViewHolder.presence.setText(R.string.mobile_online);
        } else if (RainbowPresence.AWAY.equals(contact.getPresence()) || RainbowPresence.MANUAL_AWAY.equals(contact.getPresence())) {
            // Away and manual away
            myViewHolder.presence.setTextColor(Color.parseColor("#FFA500"));
            myViewHolder.presence.setText(R.string.away);
        } else if (contact.getPresence() != null && contact.getPresence().toString().contains("busy")) {
            // Busy / Busy audio / Busy video / Busy phone
            myViewHolder.presence.setTextColor(Color.RED);
            myViewHolder.presence.setText(R.string.busy);
        } else if (RainbowPresence.DND.equals(contact.getPresence())) {
            // Do not disturb
            myViewHolder.presence.setTextColor(Color.RED);
            myViewHolder.presence.setText(R.string.do_not_disturb);
        } else if (RainbowPresence.DND_PRESENTATION.equals(contact.getPresence())) {
            // On presentation
            myViewHolder.presence.setTextColor(Color.RED);
            myViewHolder.presence.setText(R.string.do_not_disturb_on_presentation);
        } else {
            myViewHolder.presence.setTextColor(Color.GRAY);
            myViewHolder.presence.setText(R.string.offline);
        }

        return convertView;
    }

    public void updateContacts() {
        if (m_context instanceof StartupActivity) {
            ((StartupActivity)m_context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_contacts.replaceAll(RainbowSdk.instance().contacts().getRainbowContacts().getCopyOfDataList());
                    notifyDataSetChanged();
                }
            });
        }
    }

    private class MyViewHolder {
        TextView displayName;
        TextView company;
        ImageView photo;
        TextView presence;
    }
}
