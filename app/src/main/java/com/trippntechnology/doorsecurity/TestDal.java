package com.trippntechnology.doorsecurity;


import android.util.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import retrofit.Callback;
import retrofit.http.Body;

public class TestDal implements DAL {
    @Override
    public void openDoor(byte[] encryptedData, String phoneNumber, int id) {
        return;

    }

    @Override
    public String register(RegistrationObject registrationObject) {
        String stringKey = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.generateKeyPair();
            PrivateKey key = kp.getPrivate();
            stringKey = Base64.encodeToString(key.getEncoded(),Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return stringKey;
    }

    @Override
    public String register(@Body RegistrationObject registrationObject, Callback<String> callback) {
        return null;
    }
}
