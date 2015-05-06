package com.trippntechnology.doorsecurity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;


public class RestDal implements DAL {


    @Override
    public String register(RegistrationObject registrationObject) {
        return null;
    }

    @Override
    public void openDoor(byte[] encryptedData, String phoneNumber, int id) {

    }

    @Override
    public String register(@Body RegistrationObject registrationObject, Callback<String> callback) {
        final String[] f = new String[0];
        callback = new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                f[0] = s;
            }

            @Override
            public void failure(RetrofitError error) {

            }
        };
        return f[0];
    }


}
