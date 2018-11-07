package com.example.m.secur_reset;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import static java.lang.System.loadLibrary;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        loadLibrary("native-lib");
    }

    // Stuff for device administration
    DevicePolicyManager devicePolicyManager;
    ComponentName appDeviceAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // More stuff for device administration
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        appDeviceAdmin = new ComponentName(this, appDeviceAdminReceiver.class);

        // Assign the wipe button to a variable
        Button wipeB;
        wipeB = findViewById(R.id.wipeB);


        // Listen for button click
        // This method-in-a-method is correct, as the method is being defined within setOnClickListener
        wipeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // BEGIN BUTTON_CLICKED BLOCK

                // TODO: Add features within this block for more comprehensive wiping

                // Loop to check if app is device admin or not
                Boolean isAdmin = false;
                while (!isAdmin) {
                    int check = alertAdmin();

                    // TODO: remove this break statement after admin check is good
                    break;
                    //if (check == 1) {
                    // TODO: act on check value from alert dialog
                    //}
                }

                // Prompt user to activate Device Admin for the app
                Toast.makeText(getApplicationContext(), "Please enable this app as a Device Admin",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));

                // Init error val to 0
                int er;

                // Move to in-progress activity
                setContentView(R.layout.activity_inprogress);

                // Start junk writer
                er = writeJunk();


                // Start full device wipe

                // END BUTTON_CLICKED BLOCK

                // Error code description notification
                if (er != 0) {
                    // TODO: add switch statement for error code notifications
                }

            }
        });
    }

    // Junk writer method
    public int writeJunk() {
        int returnCode = 99; // Set to 99 as fail val, change accordingly.

        // TODO: Write small junk files 

        return returnCode;
    }

    // Method to alert user to allow this app to be a device admin
    public int alertAdmin() {
        int returnCode = 99; // Set to 99 as fail val, change accordingly.

        // Set up alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Click \"Okay\" to be taken to settings to enable this app as a Device Admin");
        builder.setTitle(R.string.app_name);

        // Display the alert
        AlertDialog dialog = builder.create();
        dialog.show();

        // Get user input from alert
        // TODO: act on user input from alert dialog

        return returnCode;
    }
}
