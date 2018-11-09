package com.example.m.secur_reset;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import java.io.FileOutputStream;

import java.io.File;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

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

        // Set device admin bool to false
        // SharedPreferences allow us to change this value when the user enables this outside of the app
        SharedPreferences devAdminP = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = devAdminP.edit();
        editor.putBoolean("ADMIN_ENABLED", false);
        editor.apply();

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


                // Take the user to settings to have them enable the app as a device admin
                alertAdmin();
                // TODO: check if the app is actually a device admin before attempting to wipe


                // Move to in-progress activity
                setContentView(R.layout.activity_inprogress);


                // Start junk writer
                writeJunk();


                // Start full device wipe
                devicePolicyManager.wipeData(0);

                // END BUTTON_CLICKED BLOCK

                // TODO: Error codes from

            }
        });
    }

    // Writes junk to small files to fill all free space on the device
    public void writeJunk() {

        // TODO: Debug this

        // Get free space amount
        long freeSpace = getFreeSpace();
        freeSpace /= 1024;

        // TODO: show our progress on the wiping screen out of available MB
        // long availMB = (freeSpace / 1048576); // 1024^2 to get free space in MB

        // Open new file with rand 24 char name, write 1000b of data to make 1kb file
        // Repeat for the number of freespace we have (represented as kb)
        for(long i = 0; i < freeSpace; i += 1000) {
            String filename = getRandName();
            new File(getBaseContext().getFilesDir(), filename);

            // Write 1kb of data to the file (int is 4 bytes)
            // Build array of 250 rand ints to write to the file in bulk
            int[] randA = new int[250];
            for (int a = 0; a < 250; a++) {
                int temp = new Random().nextInt(Integer.MAX_VALUE);
                randA[a] = temp;
            }
            try {
                // Open the file output stream to write to file
                FileOutputStream outputStream;
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);

                // loop through the array and write each entry to the file
                for (int w = 0; w < 250; w++) {
                    outputStream.write(randA[w]);
                }

                // Close the stream
                outputStream.close();
            }
            catch (Exception e) {
                break;
            }
        }
    }

    // Generates a random filename
    public String getRandName() {
        // Init necessary vars
        String filename = "";
        String tempAccChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        char[] accChars = tempAccChars.toCharArray();
        int accMax = tempAccChars.length();

        // Append 24 random chars to the filename
        for (int i = 0; i < 24; i++) {
            int rand = new Random().nextInt(accMax);
            filename = filename + accChars[rand];
        }
        return filename;
    }

    // Method to alert user to allow this app to be a device admin
    public void alertAdmin() {

        // Set up alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Click \"Okay\" to be taken to settings to enable this app as a Device Admin");
        builder.setTitle(R.string.app_name);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "okay"
                // Prompt user to activate Device Admin for the app
                Toast.makeText(getApplicationContext(), "Please enable this app as a Device Admin",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));
            }
        });

        // Display the alert
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    // Method to get free space on the device
    public long getFreeSpace() {
        File spaceCheck = Environment.getDataDirectory();
        StatFs space = new StatFs(spaceCheck.getPath());
        long blockSize = space.getBlockSizeLong();
        long availableBlocks = space.getAvailableBlocksLong();
        return (blockSize * availableBlocks);
    }
}
