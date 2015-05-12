package com.trippntechnology.doorsecurity;

/**
 * Created by Nate on 5/11/2015.
 */
public class Relays extends StandardResponse {
    String ID;
    String Description;

    public Relays(String message, String success) {
        super(message, success);
    }
}
