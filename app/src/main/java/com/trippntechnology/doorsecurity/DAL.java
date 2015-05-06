package com.trippntechnology.doorsecurity;


import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface DAL {
    String register(RegistrationObject registrationObject);
    void openDoor(byte[] encryptedData,String phoneNumber,int id);

    @POST("/Register")
    String register(@Body RegistrationObject registrationObject, Callback<String> callback);

}
