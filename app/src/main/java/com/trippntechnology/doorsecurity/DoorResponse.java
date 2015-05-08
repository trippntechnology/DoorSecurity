package com.trippntechnology.doorsecurity;

/**
 * Created by Nate on 5/8/2015.
 */
public class DoorResponse {
    String Success;
    String Message;

    public DoorResponse(String message, String success) {
        Message = message;
        Success = success;
    }
}
