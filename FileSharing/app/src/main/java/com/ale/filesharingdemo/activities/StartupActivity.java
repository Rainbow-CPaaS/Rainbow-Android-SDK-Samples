package com.ale.filesharingdemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.ale.filesharingdemo.R;
import com.ale.filesharingdemo.fragments.ConversationsFragment;
import com.ale.filesharingdemo.fragments.FilesFragment;
import com.ale.filesharingdemo.fragments.LoginFragment;

public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        setSupportActionBar(findViewById(R.id.app_toolbar));

        openLoginFragment();
    }

    public void openLoginFragment() {
        openFragment(new LoginFragment(), false);
    }

    public void openFilesFragment() {
        openFragment(new FilesFragment(), true);
    }

    public void openConversationsFragment() {
        openFragment(new ConversationsFragment(), true);
    }

    private void openFragment(Fragment fragment, boolean addToBackStack) {
        if (addToBackStack) {
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragment_container, fragment).commit();

        }
    }
}
