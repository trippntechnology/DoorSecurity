package com.trippntechnology.doorsecurity;


public class DoorObject {
    String AuthToken;
    String PhoneNumber;


    public DoorObject(String macAddress, String phoneNumber) {
        AuthToken = macAddress;
        PhoneNumber = phoneNumber;
    }
}
