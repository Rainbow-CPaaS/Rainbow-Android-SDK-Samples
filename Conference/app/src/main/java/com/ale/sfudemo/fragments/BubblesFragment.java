package com.ale.sfudemo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ale.infra.list.IItemListChangeListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.sfudemo.R;
import com.ale.sfudemo.activities.StartupActivity;
import com.ale.sfudemo.adapters.BubblesAdapter;

public class BubblesFragment  extends Fragment {

    private static final String TAG = "BubblesFragment";
    private StartupActivity m_activity;

    private IItemListChangeListener m_bubblesListener = new IItemListChangeListener() {
        @Override
        public void dataChanged() {
            m_activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_adapter.updateBubbles();
                }
            });
        }
    };
    private BubblesAdapter m_adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.bubbles_fragment, container, false);

        if (m_activity != null && m_activity.getSupportActionBar() != null) {
            m_activity.getSupportActionBar().setTitle(R.string.bubbles);
            m_activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        ListView listViewBubbles = (ListView)fragmentView.findViewById(R.id.list_view_bubbles);

        m_adapter = new BubblesAdapter(m_activity);
        listViewBubbles.setAdapter(m_adapter);

        RainbowSdk.instance().bubbles().getAllBubbles().registerChangeListener(m_bubblesListener);

        return fragmentView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof StartupActivity){
            m_activity = (StartupActivity) context;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity instanceof  StartupActivity) {
                m_activity = (StartupActivity)activity;
            }
        }
    }

}
