package com.contacts.activities;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ale.infra.contact.IRainbowContact;
import com.ale.listener.SignoutResponseListener;
import com.ale.rainbowsdk.RainbowSdk;
import com.contacts.R;
import com.contacts.fragments.ContactFragment;
import com.contacts.fragments.ContactsTabFragment;
import com.contacts.fragments.LoginFragment;

public class StartupActivity extends AppCompatActivity {

    public final static int REQUEST_MAKE_NATIVE_CALL = 1;
    private String m_lastContactPhoneNumber;
    private MenuItem m_exitMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        openLoginFragment();
    }

    // Fragments management

    public void openLoginFragment() {
        openFragment(new LoginFragment(), false);
    }

    public void openContactsTabFragment() {
        m_exitMenuItem.setVisible(true);
        openFragment(new ContactsTabFragment(), false);
    }

    public void openContactFragment(IRainbowContact contact) {
        ContactFragment fragment = new ContactFragment();
        fragment.setContact(contact);
        openFragment(fragment, true);
    }

    private void openFragment(Fragment fragment, boolean addToBackStack) {
        if (addToBackStack) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }

    // Menu management

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        m_exitMenuItem = menu.findItem(R.id.exit);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        m_exitMenuItem = menu.findItem(R.id.exit);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                if (RainbowSdk.instance().connection().isConnected()) {
                    RainbowSdk.instance().connection().signout(new SignoutResponseListener() {
                        @Override
                        public void onSignoutSucceeded() {
                            finish();
                        }

                    });
                    return true;
                } else {
                    Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Make native call management

    public void makeNativeCall(String number) {
        m_lastContactPhoneNumber = number;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent initCall = new Intent(Intent.ACTION_CALL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                initCall.setPackage("com.android.server.telecom");
            } else {
                initCall.setPackage("com.android.phone");
            }
            initCall.setData(Uri.parse("tel:" + number));
            startActivity(initCall);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_MAKE_NATIVE_CALL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MAKE_NATIVE_CALL && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makeNativeCall(m_lastContactPhoneNumber);
        } else {
            Toast.makeText(this, "You may grant authorization in order to make a native call.", Toast.LENGTH_SHORT).show();
        }
    }
}

