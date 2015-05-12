package com.trippntechnology.doorsecurity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AuthToken extends Request {

    public byte[] encrypt(SecretKeySpec keySpec, IvParameterSpec parameterSpec){
        byte[] encrypted = null;
        String mac = getAuthToken();

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            encrypted = c.doFinal(mac.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public String getAuthToken(){
        return getMacAddress()+"|"+getTime();
    }

    public String getTime(){
        SimpleDateFormat time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        time.setTimeZone(TimeZone.getTimeZone("UTC"));
        return time.format(new Date());
    }

}
