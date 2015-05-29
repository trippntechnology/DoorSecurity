package com.trippntechnology.doorsecurity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;


public class MainActivity extends Activity {


    private static final String FILES = "RegistrationFiles";
    private byte[] key;
    private byte[] iv;
    private byte[] encrypted;
    private Interface client;
    private DoorObject door = new DoorObject();
    private AuthToken authToken = new AuthToken();
    private SavedObjects files = new SavedObjects();
    private Gson gson = new Gson();
    private SecretKeySpec keySpec;
    private IvParameterSpec ivSpec;
    private LinearLayout main;
    private LinearLayout.LayoutParams buttonParams;
    private LinearLayout.LayoutParams params;
    private ProgressDialog progress;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private boolean hasRelays;



    //Activity Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkFileExistence(FILES)) {
            setContentView(R.layout.activity_main);

            //Get layout
            main = (LinearLayout) findViewById(R.id.layoutMain);

            //Get files
            files = jsonToObject(readFile(FILES));
            key = files.key;
            iv = files.iv;
            String url = files.URL;

            //Generate encryption keys
            keySpec = new SecretKeySpec(key, "AES");
            ivSpec = new IvParameterSpec(iv);

            //Get number
            door.PhoneNumber = getPhoneNumber();

            //Create restcall
            OkHttpClient http = new OkHttpClient();
            http.setConnectTimeout(6000, TimeUnit.MILLISECONDS);
            RestAdapter restAdapter = new RestAdapter.Builder().setClient(new OkClient(http)).setEndpoint(url).build();
            client = restAdapter.create(Interface.class);

            //Create dp sizes
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    60,
                    getResources().getDisplayMetrics());
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    130,
                    getResources().getDisplayMetrics());
            int verticalMargins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    10,
                    getResources().getDisplayMetrics());
            int horizontalMargins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    20,
                    getResources().getDisplayMetrics());

            //Create layoutparams
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            buttonParams = new LinearLayout.LayoutParams(width, height);

            buttonParams.setMargins(horizontalMargins, verticalMargins,
                    horizontalMargins, verticalMargins);


            //Progress Dialog
            progress = new ProgressDialog(this);
            progress.setCanceledOnTouchOutside(false);
            progress.setCancelable(false);


            //Alert Dialog
            builder = new AlertDialog.Builder(this);

            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getDoorsRequestCall();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please reload app to try again", Toast.LENGTH_LONG);
                    toast.show();
                    dialog.dismiss();
                }
            });
            alertDialog = builder.create();


        } else {
            Intent i = new Intent(this, Register.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onStart() {
        progress.dismiss();
        super.onStart();
        if (files.relays != null) {
            createLayout(files.relays);
            hasRelays = true;
        }
        getDoorsRequestCall();
    }

    @Override
    protected void onPause() {
        main.removeAllViews();
        progress.dismiss();
        alertDialog.dismiss();
        super.onPause();


    }


    //Create the Layout
    public void getDoorsRequestCall() {
        progress.setTitle(R.string.progress_title_main);
        progress.setMessage("Getting available doors");
        if (!hasRelays) {
            progress.show();
        }
        encrypted = authToken.encrypt(keySpec, ivSpec, getMacAddress());
        if (encrypted == null){
            Toast toast = Toast.makeText(this,"Encryption Error",Toast.LENGTH_SHORT);
            toast.show();
            progress.dismiss();
        }
        else {
            door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            final boolean finalHasRelays = hasRelays;
            client.getDoors(door, new Callback<GetRelays>() {
                @Override
                public void success(GetRelays getRelays, Response response) {
                    if (finalHasRelays) {
                        newDoors(getRelays.Relays);
                    } else {
                        createLayout(getRelays.Relays);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    fileRetrievalError(error);
                }
            });
        }
    }

    public void newDoors(final Relay[] relays) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.newDoorsTitle);
        alert.setMessage(R.string.newDoorsMessage);
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                createLayout(relays);
            }
        });
        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        files.relays = relays;
        String json = gson.toJson(files);
        try {
            FileOutputStream fos = openFileOutput(FILES, MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createLayout(Relay[] relays) {
        int divide = 1;
        if (relays != null && relays.length >= 1) {
            for (int i = 0; i < relays.length; i += 2) {
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setLayoutParams(params);
                ll.setGravity(LinearLayout.VERTICAL);
                if (i == (relays.length - 1)) {
                    ll.addView(createButton(relays[i]));
                } else {
                    for (int j = 0; j < 2; j++) {
                        ll.addView(createButton(relays[i + j]));
                    }
                }
                TranslateAnimation animation = new TranslateAnimation(0, 0, 1500, 0);
                long time = AnimationUtils.currentAnimationTimeMillis() + (100 * i);
                animation.setStartTime(time);
                if (i < 30) {
                    animation.setDuration((long) (1000 / divide));
                    divide += .4;
                } else {
                    animation.setDuration(50);
                }
                ll.setAnimation(animation);
                main.addView(ll);
            }
        } else {

            TextView tv = new TextView(getApplicationContext());
            tv.setText(R.string.relay_return_error);
            tv.setTextColor(Color.RED);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(40);
            main.addView(tv);
        }
        progress.dismiss();
    }

    public Button createButton(Relay relay) {
        Button b = new Button(this);
        b.setLayoutParams(buttonParams);
        b.setId(relay.ID);
        b.setText(relay.Description);
        b.setOnClickListener(new DoorButtonListener());
        b.setTextSize(15);
        return b;
    }


    //Open Door
    public void openDoor(StandardResponse standardResponse) {
        boolean success = Boolean.parseBoolean(standardResponse.Success);
        if (success) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.door_open_success, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            String message = standardResponse.Message;
            Toast.makeText(getApplicationContext(),"Server response"+
                    R.string.door_open_failure + "\n" + message, Toast.LENGTH_LONG).show();

        }
    }


    //Error handling
    public void fileRetrievalError(RetrofitError error) {
        if (error.getMessage().contains("failed to connect")) {
            builder.setTitle(R.string.connection_error);
            builder.setMessage(R.string.retry_message);
        } else {
            TextView tv = new TextView(this);
            tv.setText(R.string.unknown_error);
            tv.setTextSize(40);
            tv.setTextColor(Color.RED);
            main.addView(tv);
        }
        alertDialog.dismiss();
        alertDialog = builder.create();
        alertDialog.show();
        progress.dismiss();

    }

    public void openDoorFailure(RetrofitError error) {
        Toast toast = Toast.makeText(getApplicationContext(), "Retrotfit\n"+error.getMessage(), Toast.LENGTH_LONG);
        toast.show();
    }


    //Builders and File readers
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

    public AlertDialog.Builder alertBuilder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.alert_title);
        alert.setMessage(R.string.alert_message);
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                deleteFiles();
            }
        });
        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return alert;
    }


    @Override
    protected void onStop() {

        super.onStop();
    }

    //Delete Files
    public void deleteFiles() {
        File file = getBaseContext().getFileStreamPath(FILES);
        file.delete();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


    //Info Getters
    public String getMacAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    public SavedObjects jsonToObject(byte[] data) {
        String jsonString = new String(data);
        return gson.fromJson(jsonString, SavedObjects.class);
    }


    //Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_registration) {
            AlertDialog alert = alertBuilder().create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }


    //On click Listener
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
                        openDoor(standardResponse);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        openDoorFailure(error);
                    }
                });
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.encryption_error, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
