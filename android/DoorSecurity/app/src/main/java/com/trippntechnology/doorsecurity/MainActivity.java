package com.trippntechnology.doorsecurity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    private static final String REGISTRATION_FILE = "RegistrationKey";
    private static final String IV_FILE = "RegistrationIV";
    private static final String URL = "Url";
    private byte[] key;
    private byte[] iv;
    private byte[] encrypted;
    private Interface client;
    private DoorObject door = new DoorObject();
    private AuthToken authToken = new AuthToken();
    private SecretKeySpec keySpec;
    private IvParameterSpec ivSpec;
    private LinearLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkFileExistence(REGISTRATION_FILE) || checkFileExistence(IV_FILE)) {
            setContentView(R.layout.activity_main);
            //Get layout
            main = (LinearLayout) findViewById(R.id.layoutMain);
            //Get files
            key = readFile(REGISTRATION_FILE);
            iv = readFile(IV_FILE);
            String url = new String(readFile(URL));
            //Generate encryption keys
            keySpec = new SecretKeySpec(key, "AES");
            ivSpec = new IvParameterSpec(iv);
            //Get number
            door.PhoneNumber = getPhoneNumber();
            //Create restcall
            RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url).build();
            client = restAdapter.create(Interface.class);
        } else {
            Intent i = new Intent(this, Register.class);
            startActivity(i);
            finish();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        encrypted = authToken.encrypt(keySpec, ivSpec, getMacAddress());
        door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
        client.getDoors(door, new Callback<GetRelays>() {
            // button will be displayed
            @Override
            public void success(GetRelays getRelays, Response response) {
                //Set sizes in DP
                if (getRelays.Relays != null && getRelays.Relays.length >= 1) {
                    int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            60,
                            getResources().getDisplayMetrics());
                    int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            130,
                            getResources().getDisplayMetrics());
                    int verticalMargins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            5,
                            getResources().getDisplayMetrics());
                    int horizontalMargins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            20,
                            getResources().getDisplayMetrics());
                    //Create LayoutParameters
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(width, height);
                    buttonParams.setMargins(horizontalMargins, verticalMargins,
                            horizontalMargins, verticalMargins);
                    //Get Context
                    Context context = getApplicationContext();
                    //Generate Layout
                    for (int i = 0; i < getRelays.Relays.length; i += 2) {
                        LinearLayout ll = new LinearLayout(context);
                        ll.setOrientation(LinearLayout.HORIZONTAL);
                        ll.setLayoutParams(params);
                        ll.setGravity(LinearLayout.VERTICAL);
                        if (i == (getRelays.Relays.length - 1)) {
                            Button b = new Button(context);
                            b.setLayoutParams(buttonParams);
                            b.setId(getRelays.Relays[i].ID);
                            b.setText(getRelays.Relays[i].Description);
                            b.setOnClickListener(new DoorButtonListener());
                            ll.addView(b);
                        } else {
                            for (int j = 0; j < 2; j++) {
                                Button b = new Button(context);
                                b.setLayoutParams(buttonParams);
                                b.setId(getRelays.Relays[i + j].ID);
                                b.setText(getRelays.Relays[i + j].Description);
                                b.setOnClickListener(new DoorButtonListener());
                                ll.addView(b);
                            }
                        }
                        main.addView(ll);
                    }
                }else{
                    TextView tv = new TextView(getApplicationContext());
                    tv.setText(R.string.relay_return_error);
                    tv.setTextColor(Color.BLACK);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(40);
                    main.addView(tv);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                TextView tv = new TextView(getApplicationContext());
                tv.setText(error.getMessage());
                tv.setTextColor(Color.BLACK);
                main.addView(tv);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        main.removeAllViews();
    }

//    public void openDoor(View view) {
//        //Initialize
//        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//        //Encrypt
//        encrypted = authToken.encrypt(keySpec, ivSpec, getMacAddress());
//        if (encrypted != null) {
//            door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
//            //Rest Call
//            client.openDoor(door, new Callback<StandardResponse>() {
//                @Override
//                public void success(StandardResponse standardResponse, Response response) {
//                    boolean success = Boolean.parseBoolean(standardResponse.Success);
//                    String message = standardResponse.Message;
//
//                    Toast toast = Toast.makeText(getApplication(), success + "\n" + message, Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//
//                @Override
//                public void failure(RetrofitError error) {
//                    Toast toast = Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG);
//                    toast.show();
//                }
//            });
//        } else {
//            Toast toast = Toast.makeText(this, R.string.encryption_error, Toast.LENGTH_SHORT);
//            toast.show();
//        }
//
//    }

    public boolean checkFileExistence(String fileName) {
        File file = getBaseContext().getFileStreamPath(fileName);
        return file.exists();
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

    public String getMacAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

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
            file.delete();
            file1.delete();
            file2.delete();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class DoorButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Initialize
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            //Encrypt
            encrypted = authToken.encrypt(keySpec, ivSpec, getMacAddress());
            if (encrypted != null) {
                door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
                door.ID = v.getId();
                //Rest Call
                client.openDoor(door, new Callback<StandardResponse>() {
                    @Override
                    public void success(StandardResponse standardResponse, Response response) {
                        boolean success = Boolean.parseBoolean(standardResponse.Success);
                        if (success) {

                            Toast toast = Toast.makeText(getApplicationContext(), R.string.door_open_success, Toast.LENGTH_SHORT);
                            toast.show();
                        }else {
                            String message = standardResponse.Message;
                            Toast.makeText(getApplicationContext(),
                                    R.string.door_open_failure+"\n"+message,Toast.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.encryption_error, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


}
