package com.trippntechnology.doorsecurity;



public class KeyReturn {
    String Key;
    String IV;
    String Success;
    String Message;
    String YourName;

    public KeyReturn(String IV, String key, String message, String success, String YourName) {
        this.IV = IV;
        Key = key;
        Message = message;
        Success = success;
        this.YourName = YourName;
    }
    public KeyReturn(){}
}
