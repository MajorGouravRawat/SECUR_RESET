package com.example.m.secur_reset;

import android.app.AlertDialog;
import android.app.admin.DeviceAdminInfo;
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
import android.widget.TextView;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Random;
import java.util.Arrays;

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

        // Assign the wipe button to a variable
        Button wipeB;
        wipeB = findViewById(R.id.wipeB);


        // Listen for button click
        // This method-in-a-method is correct, as the method is being defined within setOnClickListener
        wipeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                boolean isAdmin = devicePolicyManager.isAdminActive(appDeviceAdmin);
                if (isAdmin) {
                    // BEGIN BUTTON_CLICKED BLOCK
                    // TODO: Add features within this block for more comprehensive wiping


                    // Move to in-progress activity
                    setContentView(R.layout.activity_inprogress);


                    // Start junk writer
                    //writeJunk();


                    // Start full device wipe
                    devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    devicePolicyManager.wipeData(1);

                    // END BUTTON_CLICKED BLOCK
                }
                else {
                    // Take the user to settings to have them enable the app as a device admin
                    alertAdmin();
                }
            }
        });
    }

    // Writes junk to small files to fill all free space on the device
    public void writeJunk() {

        // TODO: Debug this

        // Get free space amount
        long freeSpace = getFreeSpace();

        // long availMB = (freeSpace / 1048576); // 1024^2 to get free space in MB

        // Open new file with rand 24 char name, write 1000b of data to make 1kb file
        // Repeat for the number of freespace we have (represented as kb)


        // Start new file
        String filename = getRandName();
        new File(getBaseContext().getFilesDir(), filename);
        try {
            // Open the file output stream to write to file
            FileOutputStream outputStream;
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);

            for(long i = 0; i < freeSpace; i += 1) {
                // Display amount of data written on the screen
                TextView amountWritten = findViewById(R.id.amountWritten);
                String updateTextView = "Written: " + i + "/" + freeSpace + " bytes";
                amountWritten.setText(updateTextView);

                // Write 1b of data to the file (int is 4 bytes)
                // Build array of 250 rand ints to write to the file in bulk
                byte randB;
                int temp = new Random().nextInt(Integer.MAX_VALUE);
                randB = (byte)temp;
                outputStream.write(randB);
            }

            // Close the stream
            outputStream.close();
        }
        catch (Exception e) {
            System.out.println("Something went wrong when writing to the file");
        }



    }

    // Generates a random filename
    public String getRandName() {
        // Init necessary vars
        String tempAccChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        char[] accChars = tempAccChars.toCharArray();
        int accMax = tempAccChars.length();
        char[] filename = new char[24];

        // Append 24 random chars to the filename
        for (int i = 0; i < 24; i++) {
            int rand = new Random().nextInt(accMax);
            filename[i] = accChars[rand];
        }
        return Arrays.toString(filename);
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

                //startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")));

                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, appDeviceAdmin);
                intent.putExtra("Factory Reset", DeviceAdminInfo.USES_POLICY_WIPE_DATA);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This allows the app to factory reset the device after it has been wiped.");
                startActivityForResult(intent, 0);
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
