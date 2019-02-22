package com.ale.channelsdemo.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ale.channelsdemo.R;
import com.ale.channelsdemo.fragments.ChannelDetailFragment;
import com.ale.channelsdemo.fragments.ChannelsFragment;
import com.ale.channelsdemo.fragments.LoginFragment;
import com.ale.channelsdemo.fragments.NewChannelFragment;
import com.ale.channelsdemo.fragments.NewChannelItemFragment;
import com.ale.infra.manager.channel.Channel;

public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        setSupportActionBar(findViewById(R.id.app_toolbar));

        openLoginFragment();
    }

    public void openLoginFragment() {
        openFragment(LoginFragment.newInstance(), false);
    }

    public void openChannelsFragment() {
        openFragment(ChannelsFragment.newInstance(), true);
    }

    public void openChannelDetailFragment(Channel channel) {
        ChannelDetailFragment fragment = ChannelDetailFragment.newInstance(channel);
        openFragment(fragment, true);
    }

    public void openNewChannelItemFragment(Channel channel) {
        NewChannelItemFragment fragment = NewChannelItemFragment.newInstance(channel);
        openFragment(fragment, true);
    }

    private void openFragment(Fragment fragment, boolean addToBackStack) {
        if (addToBackStack) {
            getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        } else {
            getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action, menu);
        MenuItem menuItem = menu.findItem(R.id.action_create);
        menuItem.setVisible(false);
        return true;
    }

    public void openNewChannelFragment() {
        NewChannelFragment newChannelFragment = NewChannelFragment.newInstance();
        openFragment(newChannelFragment, true);
    }
}
