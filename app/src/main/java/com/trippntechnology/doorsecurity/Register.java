package com.trippntechnology.doorsecurity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;


public class Register extends Activity {


    private String url;
    private EditText token, urlBox;
    private Button regButton;
    private ProgressDialog progress;
    private RegistrationObject RO = new RegistrationObject();
    private SavedObjects files = new SavedObjects();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        token = (EditText) findViewById(R.id.tokenText);
        urlBox = (EditText) findViewById(R.id.urlText);
        regButton = (Button) findViewById(R.id.registerButton);
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.progress_title_register);
        progress.setMessage("Retrieving registration information");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation fade = new AlphaAnimation(0, 1);
        fade.setDuration(1500);
        fade.setStartTime(AnimationUtils.currentAnimationTimeMillis() + 250);
        Animation slide = new TranslateAnimation(0, 0, 100, 0);
        slide.setDuration(750);
        slide.setStartTime(AnimationUtils.currentAnimationTimeMillis() + 250);
        AnimationSet animate = new AnimationSet(false);
        animate.addAnimation(fade);
        animate.addAnimation(slide);
        urlBox.setAnimation(animate);
        token.setAnimation(animate);
        regButton.setAnimation(animate);
    }

    public void registerButton(View view) {
        progress.show();

        RO.MacAddress = getMacAddress();
        RO.PhoneNumber = getPhoneNumber();
        RO.Token = token.getText().toString();
        url = urlBox.getText().toString();

//                Set-up restAdapter

        OkHttpClient http = new OkHttpClient();
        http.setConnectTimeout(6000, TimeUnit.MILLISECONDS);
        RestAdapter restAdapter = new RestAdapter.Builder().
                setClient(new OkClient(http))
                .setEndpoint(url).build();
        Interface client = restAdapter.create(Interface.class);
        client.register(RO, new Callback<KeyReturn>() {
            @Override
            public void success(KeyReturn keyReturn, Response response) {
                writeFiles(keyReturn);
            }

            @Override
            public void failure(RetrofitError error) {
                registrationError(error);
            }
        });
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token.getWindowToken(), 0);

    }

    public void writeFiles(KeyReturn keyReturn) {
        boolean success = Boolean.parseBoolean(keyReturn.Success);
        if (success) {
            byte[] key = Base64.decode(keyReturn.Key, Base64.DEFAULT);
            byte[] IV = Base64.decode(keyReturn.IV, Base64.DEFAULT);
            files.key = key;
            files.iv = IV;
            files.URL = url;
            String json = gson.toJson(files);
            try {
                FileOutputStream fos = openFileOutput(MainActivity.FILES,MODE_PRIVATE);
                fos.write(json.getBytes());
                fos.close();
                progress.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_SHORT);
                toast.show();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            } catch (IOException e) {
                progress.dismiss();
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), R.string.storing_key_error, Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            progress.dismiss();
            if (keyReturn.Message == null || keyReturn.Message.contains("")) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.return_key_error, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), keyReturn.Message, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public void registrationError(RetrofitError error) {
        progress.dismiss();
        if (error.getMessage().contains("failed to connect")) {
            Toast toast = Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG);
            toast.show();
        }else {
            Toast toast = Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG);
            toast.show();
        }
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
