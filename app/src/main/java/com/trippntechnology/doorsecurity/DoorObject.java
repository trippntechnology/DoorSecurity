package com.trippntechnology.doorsecurity;


public class DoorObject {
    String AuthToken;
    String PhoneNumber;
    int ID;


    public DoorObject(String macAddress, String phoneNumber, int ID) {
        AuthToken = macAddress;
        PhoneNumber = phoneNumber;
        this.ID = ID;
    }

    public DoorObject(){}
}
