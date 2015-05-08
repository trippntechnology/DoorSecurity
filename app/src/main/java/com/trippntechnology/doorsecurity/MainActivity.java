package com.trippntechnology.doorsecurity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity {


    private static final String TAG = "KEY TAG";
    private static final String REGISTRATION_FILE = "RegistrationKey";
    private static final String IV_FILE = "RegistrationIV";
    private static final String URL = "Url";


    private String macAddress;
    private String phoneNumber;
    private boolean canOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkFileExistence(REGISTRATION_FILE) || checkFileExistence(IV_FILE)) {
            setContentView(R.layout.activity_main);
        } else {
            Intent i = new Intent(this, Register.class);
            startActivity(i);
            finish();

        }
        phoneNumber = getPhoneNumber();
        macAddress = getMacAddress();


    }

    public void openDoor(View view) {
        //getTime
//        if (canOpen) {
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String time = f.format(new Date());

        //Initialize
        byte[] key = readFile(REGISTRATION_FILE);
        byte[] iv = readFile(IV_FILE);
        String url = new String(readFile(URL));

        String macAddressTime = macAddress + "|" + time;
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        //Encrypt
        byte[] encrypted = null;
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            encrypted = c.doFinal(macAddressTime.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (encrypted != null) {
            String encodedString = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            Log.i(TAG, encodedString);
            DoorObject door = new DoorObject(encodedString, phoneNumber);

            //Rest Call
            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url).build();
            Interface client = restAdapter.create(Interface.class);

            client.openDoor(door, new Callback<DoorResponse>() {
                @Override
                public void success(DoorResponse doorResponse, Response response) {
                    boolean success = Boolean.parseBoolean(doorResponse.Success);
                    String message = doorResponse.Message;

                    Toast toast = Toast.makeText(getApplication(), success + "\n" + message, Toast.LENGTH_SHORT);
                    toast.show();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast toast = Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        } else {
            Toast toast = Toast.makeText(this, R.string.encryption_error, Toast.LENGTH_SHORT);
            toast.show();
        }
//            canOpen = false;
//            while (!canOpen){
//                SystemClock.sleep(5000);
//                canOpen = true;
//            }

//        } else {
//            Toast toast = Toast.makeText(this, R.string.cooldown, Toast.LENGTH_LONG);
//            toast.show();
//        }

    }

    public boolean checkFileExistence(String fileName) {
        File file = getBaseContext().getFileStreamPath(fileName);
        return file.exists();
    }

    public String getMacAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    public byte[] readFile(String filename) {
        int bytesRead;
        byte[] bytes = null;
        try {
            InputStream fileReader = this.openFileInput(filename);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while ((bytesRead = fileReader.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

//    public byte[] getPrivateKey(String filename) {
//        byte[] key = null;
//        int bytesRead;
//        try {
//            InputStream fileReader = this.openFileInput(filename);
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//
//            while ((bytesRead = fileReader.read(buffer)) != -1) {
//                baos.write(buffer, 0, bytesRead);
//            }
//
//            key = baos.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return key;
//
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.delete_registration) {
            File file = getBaseContext().getFileStreamPath(REGISTRATION_FILE);
            File file1 = getBaseContext().getFileStreamPath(IV_FILE);
            File file2 = getBaseContext().getFileStreamPath(URL);
            file1.delete();
            file.delete();
            file2.delete();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
