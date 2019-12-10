package com.ale.sfudemo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ale.infra.list.ArrayItemList;
import com.ale.infra.list.IItemListChangeListener;
import com.ale.infra.manager.room.Room;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.sfudemo.R;
import com.ale.sfudemo.activities.StartupActivity;

import java.util.List;

public class BubblesAdapter  extends BaseAdapter {

    private ArrayItemList<Room> m_rooms = new ArrayItemList<>();
    private Context m_context;

    private IItemListChangeListener m_changeListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            if (m_context instanceof StartupActivity) {
                ((StartupActivity)m_context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBubbles();
                    }
                });
            }
        }
    };

    public BubblesAdapter(Context context) {
        m_context = context;
        updateBubbles();
    }

    @Override
    public int getCount() {
        return m_rooms.getCount();
    }

    @Override
    public Object getItem(int position) {
        return m_rooms.get(position);
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
            convertView = inflater.inflate(R.layout.row_list_bubble, parent, false);

            myViewHolder = new MyViewHolder();
            myViewHolder.displayName = (TextView)convertView.findViewById(R.id.display_name);
            myViewHolder.openRooù = (Button) convertView.findViewById(R.id.open_room);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder)convertView.getTag();
        }

        final Room room = (Room)getItem(position);
        myViewHolder.displayName.setText(room.getName());
        myViewHolder.openRooù.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_context instanceof StartupActivity) {
                    ((StartupActivity)m_context).openBubbleFragment(room, false);
                }
            }
        });

        return convertView;
    }

    public void updateBubbles() {
        m_rooms.clear();

        List<Room> rooms = RainbowSdk.instance().bubbles().getAllBubbles().getCopyOfDataList();
        m_rooms.replaceAll(rooms);
        notifyDataSetChanged();
    }

    private class MyViewHolder {
        TextView displayName;
        Button openRooù;
    }
}