package com.ale.filesharingdemo.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsHelper {

    private static final int PERMISSION_STORAGE = 1;
    private static PermissionsHelper singleton = null;

    private PermissionsHelper() {}

    public static PermissionsHelper instance() {
        if (singleton == null) {
            singleton = new PermissionsHelper();
        }
        return singleton;
    }

    public boolean isExternalStorageAllowed(Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
            return false;
        }
        return true;
    }
}
