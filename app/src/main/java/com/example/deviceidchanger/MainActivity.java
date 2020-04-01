package com.example.deviceidchanger;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    String deviceModel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get root Permissions
        try {
            Runtime.getRuntime().exec(new String[]{"su",});
        } catch (IOException e) {
            e.printStackTrace();
        }
        setId();
        //Set current model
        TextView textViewDeviceModel = findViewById(R.id.textViewDeviceModel);
        deviceModel = BuildProp.getProp("ro.product.model", true);
        textViewDeviceModel.setText(deviceModel);
        Log.e("device model - ", deviceModel);

        Button btnChangeId = findViewById(R.id.btnChangeId);
        btnChangeId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextId = findViewById(R.id.editTextDeviceId);
                changeDeviceId(editTextId.getText().toString());
                showCustomToast("Android ID updated");
            }
        });

        Button btnRefreshId = findViewById(R.id.btnRefreshId);
        btnRefreshId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setId();
            }
        });

        Button btnRandomizeId = findViewById(R.id.btnRandomizeId);
        btnRandomizeId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String randomId = generateRandomAndroidId();
                Log.e("Generated random ID - ", randomId);
                changeDeviceId(randomId);
                showCustomToast("Android ID updated");
            }
        });

        Button btnInstall = findViewById(R.id.btnInstallTrell);
        btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String installCommand = "pm install -r data/local/tmp/trell.apk";
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(new String[]{"su", "-c", installCommand});
                    process.waitFor();
                    Toast.makeText(MainActivity.this, "TRELL installed!", Toast.LENGTH_SHORT).show();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnUninstall = findViewById(R.id.btnUninstallTrell);
        btnUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uninstallCommand = "pm uninstall app.trell";
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(new String[]{"su", "-c", uninstallCommand});
                    process.waitFor();
                    Toast.makeText(MainActivity.this, "TRELL uninstalled!", Toast.LENGTH_SHORT).show();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnChangeModel = findViewById(R.id.btnChangeModel);
        btnChangeModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextId = findViewById(R.id.editTextDeviceModel);
                changeDeviceModel(deviceModel, editTextId.getText().toString());


            }
        });


        Button btnReboot = findViewById(R.id.btnReboot);
        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rebootDevice();
            }
        });
    }

    private void setId() {

        try {


            String yourCommand = " content query --uri content://settings/secure --projection value --where \"name = 'android_id'\" ";

            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand});
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            // Grab the results
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");
            }
            String androidID = log.toString();
            androidID = androidID.substring(androidID.indexOf('=') + 1);

            // Update the view
            TextView tv = (TextView) findViewById(R.id.textViewDeviceId);
            tv.setText(androidID);
        } catch (IOException | InterruptedException e) {
        }


    }

    void changeDeviceId(String customId) {

        String yourCommand = " content query --uri content://settings/secure --where \"name = 'android_id'\" ";
        String yourCommand1 = "content delete --uri content://settings/secure --where \"name = 'android_id'\"";
        String yourCommand2 = "content insert --uri content://settings/secure --bind name:s:android_id --bind value:s:" + customId + "\n";

        try {
            Process p1 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand});
            p1.waitFor();
            Process p2 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand1});
            p2.waitFor();
            Process p3 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand2});
            p3.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    String generateRandomAndroidId() {
        StringBuilder randomString = new StringBuilder();
        do {

            String st = "12ab345cd67890ef";
            Random rn = new Random();
            for (int i = 0; i < 16; i++) {
                randomString.append(st.charAt(rn.nextInt(st.length())));
            }
        } while (!isValid(randomString.toString().toLowerCase()));

        return randomString.toString().toLowerCase();
    }

    private boolean isValid(String input) {
        return (input.matches("^([0-9a-f]{2})+$") && input.length() == 16);

    }

    void showCustomToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    void changeDeviceModel(String model, String newModel) {

        String yourCommand = " cp /system/build.prop /storage/emulated/0 ";
        Log.e("newmodelchngmethd", newModel);
        String yourCommand1 = "sed -i 's/" + model + "/" + newModel + "/g' /storage/emulated/0/build.prop";
        String yourCommand2 = "mount -o rw,remount /system";
        String yourCommand2_1 = "mount -o rw,remount /";
        String yourCommand3 = "mv /storage/emulated/0/build.prop /system";
        String yourCommand4 = "chmod 644 /system/build.prop";


        try {
            Process p1 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand});
            p1.waitFor();
            Process p2 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand1});
            p2.waitFor();
            Process p3 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand2});
            p3.waitFor();
            Process p3_1 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand2_1});
            p3_1.waitFor();
            Process p4 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand3});
            p4.waitFor();
            Process p5 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand4});
            p5.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        showCustomToast("Model updated in build file");

    }

    void rebootDevice() {
        String yourCommand = "reboot";
        try {
            Process p1 = Runtime.getRuntime().exec(new String[]{"su", "-c", yourCommand});
            p1.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

