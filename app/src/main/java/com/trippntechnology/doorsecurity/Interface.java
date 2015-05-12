package com.trippntechnology.doorsecurity;

import android.app.ProgressDialog;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;


public interface Interface {
    @POST("/Register")
    void register(@Body RegistrationObject registrationObject, Callback<KeyReturn> callback);

    @POST("/Open")
    void openDoor(@Body DoorObject doorObject, Callback<StandardResponse> callback);

    @POST("/GetRelays")
    void getDoors(@Body DoorObject doorObject,Callback<GetRelays> callback);
}
