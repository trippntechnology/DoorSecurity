package com.trippntechnology.doorsecurity;



public class KeyReturn {
    String Key;
    String IV;
    String Success;
    String Message;

    public KeyReturn(String IV, String key, String message, String success) {
        this.IV = IV;
        Key = key;
        Message = message;
        Success = success;
    }
    public KeyReturn(){}
}
