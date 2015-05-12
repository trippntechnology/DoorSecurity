package com.trippntechnology.doorsecurity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "KEY TAG";
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
        } else {
            Intent i = new Intent(this, Register.class);
            startActivity(i);
            finish();
        }
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
        door.PhoneNumber = authToken.getPhoneNumber();
        //Create restcall
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url).build();
        client = restAdapter.create(Interface.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        encrypted = authToken.encrypt(keySpec, ivSpec);
        door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
        client.getDoors(door, new Callback<Relays[]>() {
            // button will be displayed
            @Override
            public void success(Relays[] relays, Response response) {
                //Set sizes in DP
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                        getResources().getDisplayMetrics());
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120,
                        getResources().getDisplayMetrics());
                int verticalMargins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                        getResources().getDisplayMetrics());
                int horizontalMargins = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
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
                for (int i = 0; i < relays.length; i += 2) {
                    LinearLayout ll = new LinearLayout(context);
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    ll.setLayoutParams(params);
                    ll.setGravity(LinearLayout.VERTICAL);
                    if (i == (relays.length - 1)) {
                        Button b = new Button(context);
                        b.setLayoutParams(buttonParams);
                        b.setId(Integer.parseInt(relays[i].ID));
                        b.setText(relays[i].Description);
                        b.setOnClickListener(new DoorButtonListener());
                        ll.addView(b);
                    } else{
                        for (int j = 0; j < 2; j++) {
                            Button b = new Button(context);
                            b.setLayoutParams(buttonParams);
                            b.setId(Integer.parseInt(relays[i + j].ID));
                            b.setText(relays[i + j].Description);
                            b.setOnClickListener(new DoorButtonListener());
                            ll.addView(b);
                        }
                    }
                    main.addView(ll);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void openDoor(View view) {
        //Initialize
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        int id = 0;
        //Encrypt
        encrypted = authToken.encrypt(keySpec, ivSpec);
        if (encrypted != null) {
            door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
            //Rest Call
            client.openDoor(door, new Callback<StandardResponse>() {
                @Override
                public void success(StandardResponse standardResponse, Response response) {
                    boolean success = Boolean.parseBoolean(standardResponse.Success);
                    String message = standardResponse.Message;

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

    }

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

    private class DoorButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //Initialize
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            //Encrypt
            encrypted = authToken.encrypt(keySpec, ivSpec);
            if (encrypted != null) {
                door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
                door.ID = v.getId();
                //Rest Call
                client.openDoor(door, new Callback<StandardResponse>() {
                    @Override
                    public void success(StandardResponse standardResponse, Response response) {
                        boolean success = Boolean.parseBoolean(standardResponse.Success);
                        String message = standardResponse.Message;
                        Toast toast = Toast.makeText(getApplicationContext(), success + "\n" + message, Toast.LENGTH_SHORT);
                        toast.show();
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
