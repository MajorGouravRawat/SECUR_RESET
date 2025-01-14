package com.example.m.secur_reset;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.FileOutputStream;
import java.io.File;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    // Needed for getting the context of the app within a Runnable
    private Context context;

    /*
    Stuff for updating amount of data written to the device.
    Ignore this warning. We can't make this handler static and still
    update the UI, and we shouldn't expect to encounter this issue
    */
    boolean writeComplete = false;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Get data from the thread message and decode it
            Bundle b = msg.getData();
            long i = b.getLong("i");
            long freeSpace = b.getLong("freeSpace");

            // Display amount of data written on the screen
            TextView amountWritten = findViewById(R.id.amountWritten);
            String updateTextView = "Written: " + i + "/" + freeSpace + " bytes";
            amountWritten.setText(updateTextView);
            ProgressBar p = findViewById(R.id.progressBar);
            p.setProgress((int) (100 * ((double) i / (double)freeSpace)));

            if (i == freeSpace) {
                writeComplete = true;
            }
        }
    };

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
                    writeJunk();

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

        context = this;

        /*
        This method will be ran as a background threat, to not
        freeze the UI thread on file creation.
        */
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // Get free space amount
                long freeSpace = getFreeSpace();

                // long availMB = (freeSpace / 1048576); // 1024^2 to get free space in MB

                // Open new file with rand 24 char name, write 1000b of data to make 1kb file
                // Repeat for the number of freespace we have (represented as kb)


                // Start new file
                String filename = getRandName();

                File file = new File(context.getFilesDir(), filename);
                try {
                    // Open the file output stream to write to file
                    FileOutputStream outputStream = new FileOutputStream(file);
                    SecureRandom r = new SecureRandom();

                    long i = 0;
                    while(i < freeSpace) {
                        // Write 1kb of data to the file if we have the room
                        if (freeSpace - i > 1024) {
                            byte[] randA = new byte[1024];
                            r.nextBytes(randA);
                            outputStream.write(randA);
                            i += 1024;
                        }
                        // Write 1b of data to the file if we don't have the room for 1kb
                        else {
                            byte[] randB = new byte[1];
                            r.nextBytes(randB);
                            outputStream.write(randB);
                            i++;
                        }


                        // Send message with a bundle to handler
                        final Message msg = new Message();
                        final Bundle b = new Bundle();
                        b.putLong("i", i);
                        b.putLong("freeSpace", freeSpace);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }

                    // Close the stream
                    outputStream.close();

                    devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    devicePolicyManager.wipeData(1);
                }

                // Can happen if we end up with less space than we first calculated to write files
                catch (Exception e) {
                    System.out.println("Something went wrong when writing to the file");

                    // Realistically, we will have written junk to as much space as we can. Factory reset at this point.
                    devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    devicePolicyManager.wipeData(1);
                }
            }
        };

        // Start the fileWriter thread
        Thread fileWriter = new Thread(r);
        fileWriter.start();



    }

    // Generates a random filename
    public String getRandName() {
        // Init necessary vars
        String tempAccChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        char[] accChars = tempAccChars.toCharArray();
        int accMax = tempAccChars.length();

        // Use a StringBuilder so that we don't have array garbage in our filename
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            int rand = new SecureRandom().nextInt(accMax);
            nameBuilder.append(accChars[rand]);
        }
        return nameBuilder.toString();
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

                // Take the user to settings to activate this app as a device admin
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

    /*
    Method to get free space on the device
    *NOTE* This amount can change while we write to the device, this is just to get a general
    idea on how much we need to write
    */
    public long getFreeSpace() {
        File spaceCheck = Environment.getDataDirectory();
        StatFs space = new StatFs(spaceCheck.getPath());
        long blockSize = space.getBlockSizeLong();
        long availableBlocks = space.getAvailableBlocksLong();
        return (blockSize * availableBlocks);
    }
}