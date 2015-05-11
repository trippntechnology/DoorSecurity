package com.trippntechnology.doorsecurity;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Nate on 5/11/2015.
 */
public class AuthToken extends Request {


    public byte[] encrypt(SecretKeySpec keySpec, IvParameterSpec parameterSpec,String time){
        byte[] encrypted = null;
        String mac = getMacAddress()+"|"+time;

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            encrypted = c.doFinal(mac.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

}
