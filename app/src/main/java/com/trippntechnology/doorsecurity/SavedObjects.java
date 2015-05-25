package com.trippntechnology.doorsecurity;


public class SavedObjects {
    byte[] key;
    byte[] iv;
    String URL;
    Relay[] relays;

    public SavedObjects(byte[] iv, byte[] key, Relay[] relays, String URL) {
        this.iv = iv;
        this.key = key;
        this.relays = relays;
        this.URL = URL;
    }
    public SavedObjects(){}
}
