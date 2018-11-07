package com.example.m.secur_reset;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * This is the component that is responsible for actual device administration.
 */
public class appDeviceAdminReceiver extends DeviceAdminReceiver {

    // Called when app is approved to be a device administrator.
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "Device Admin Enabled",
                Toast.LENGTH_SHORT).show();
    }

    // Called when device administrator is disabled for this app.
    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, "Device Admin Disabled",
                Toast.LENGTH_SHORT).show();
    }


}