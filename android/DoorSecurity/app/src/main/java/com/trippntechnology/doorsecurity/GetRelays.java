package com.trippntechnology.doorsecurity;



public class GetRelays extends StandardResponse {
    Relay[] Relays;

//    public GetRelays(String message,String success,Relay relay){
//        super(message,success);
//        Relays.add(relay);
//    }

    public GetRelays(String message, String success) {
        super(message, success);
    }
}
