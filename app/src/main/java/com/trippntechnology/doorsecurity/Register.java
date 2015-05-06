package com.trippntechnology.doorsecurity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class Register extends ActionBarActivity {

    private static final String TAG = "KEY TAG";
    private static final String REGISTRATION_FILE = "RegistrationKey";
    private static final String IV_FILE = "RegistrationIV";
    private static final String URL = "Url";

    EditText token;
    EditText urlBox;
    TestDal dal = new TestDal();

    ProgressDialog progressDialog;
    String key;
    FileOutputStream fos;
    RegistrationObject RO = new RegistrationObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        token = (EditText) findViewById(R.id.tokenText);
        urlBox = (EditText) findViewById(R.id.urlText);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Registering");
        progressDialog.setMessage("Getting key from server");

    }

    public void registerButton(View view) {
//        progressDialog.show();

        RO.MacAddress = getMacAddress();
        RO.PhoneNumber = getPhoneNumber();
        RO.Token = token.getText().toString();
        final String url = urlBox.getText().toString();
        //Request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Rest call
        KeyReturnRequest key = new KeyReturnRequest(url,
                RO,progressDialog, new com.android.volley.Response.Listener<KeyReturn>() {
            @Override
            public void onResponse(KeyReturn keyReturn) {
                boolean success = Boolean.parseBoolean(keyReturn.Success);
                if (success) {
                    byte[] key = Base64.decode(keyReturn.Key, Base64.DEFAULT);
                    byte[] IV = Base64.decode(keyReturn.IV, Base64.DEFAULT);
                    try {
                        fos = openFileOutput(REGISTRATION_FILE, MODE_PRIVATE);
                        fos.write(key);
                        fos = openFileOutput(IV_FILE, MODE_PRIVATE);
                        fos.write(IV);
                        fos = openFileOutput(URL, MODE_PRIVATE);
                        fos.write(url.getBytes());
                        fos.close();
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_SHORT);
                        toast.show();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.storing_key_error, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), keyReturn.Message, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(volleyError.getMessage().contains("failed to connect")){
                Toast toast = Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT);
                toast.show();
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(), volleyError.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
                progressDialog.dismiss();
            }
        });
       requestQueue.add(key);

//                Set-up restAdapter
//        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://10.0.0.63:12345").build();
//        Interface client = restAdapter.create(Interface.class);
//
//                Get key
//        KeyReturn keyReturn = client.registerr(RO);
//        boolean success = Boolean.parseBoolean(keyReturn.Success);
//        if (success) {
//            byte[] key = Base64.decode(keyReturn.Key, Base64.DEFAULT);
//            byte[] IV = Base64.decode(keyReturn.IV, Base64.DEFAULT);
//            try {
//                fos = openFileOutput(REGISTRATION_FILE, MODE_PRIVATE);
//                fos.write(key);
//                fos = openFileOutput(IV_FILE, MODE_APPEND);
//                fos.write(IV);
//                fos.close();
//                Toast toast = Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT);
//                toast.show();
//                Intent i = new Intent(this, MainActivity.class);
//                startActivity(i);
//                finish();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast toast = Toast.makeText(this,R.string.storing_key_error,Toast.LENGTH_SHORT);
//                toast.show();
//            }
//
//        } else {
//            Toast toast = Toast.makeText(this, keyReturn.Message, Toast.LENGTH_SHORT);
//            toast.show();
//        }
//
//        client.register(RO, new Callback<KeyReturn>() {
//
//            byte[] key;
//            byte[] IV;
//            String name;
//
//            @Override
//            public void success(KeyReturn keyReturn, Response response) {
//
//                if (keyReturn == null) {
//                    Toast toast = Toast.makeText(getApplicationContext(), R.string.return_key_error, Toast.LENGTH_SHORT);
//                    toast.show();
//                } else {
//                    key = Base64.decode(keyReturn.Key, Base64.DEFAULT);
//                    IV = Base64.decode(keyReturn.IV, Base64.DEFAULT);
//                    name = keyReturn.YourName;
//                    byte[] unencoded = Base64.decode(name,Base64.DEFAULT);
//                    Log.i(TAG, Arrays.toString(unencoded));
//                    String unencodedString;
//                    try {
//                        unencodedString = new String(unencoded, "UTF-8");
//                        Log.i(TAG,unencodedString);
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        fos = openFileOutput(REGISTRATION_FILE, MODE_PRIVATE);
//                        fos.write(key);
//                        fos = openFileOutput(IV_FILE, MODE_APPEND);
//                        fos.write(IV);
//                        fos.close();
//                        Toast toast = Toast.makeText(getApplicationContext(), keyReturn.Success, Toast.LENGTH_SHORT);
//                        toast.show();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(i);
//                finish();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Toast toast = Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_LONG);
//                toast.show();
//            }
//        });
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

}
