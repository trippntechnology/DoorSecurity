package com.trippntechnology.doorsecurity;

import android.app.ProgressDialog;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;


public interface Interface {
    @POST("/Register")
    void register(@Body RegistrationObject registrationObject, Callback<KeyReturn> callback);

    @POST("/Register")
    KeyReturn registerr(@Body RegistrationObject registrationObject);

    @POST("/Open")
    void openDoor(@Body DoorObject doorObject, Callback<String> callback);

    @POST("/Open")
    String openDoorr(@Body DoorObject doorObject);

}
