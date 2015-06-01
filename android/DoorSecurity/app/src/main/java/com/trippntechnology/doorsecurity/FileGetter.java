package com.trippntechnology.doorsecurity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class FileGetter {
    Gson gson = new Gson();

    public FileGetter(){}

    public String getPhoneNumber(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    public String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public SavedObjects getSavedObjects(String filename,Context context){
        return jsonToObject(readFile(filename,context));
    }
    public boolean checkFileExistence(String fileName,Context context) {
        File file = context.getFileStreamPath(fileName);
        return file.exists();
    }

    private byte[] readFile(String filename,Context context) {
        int bytesRead;
        byte[] bytes = null;
        try {
            InputStream fileReader = context.openFileInput(filename);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while ((bytesRead = fileReader.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }



    private SavedObjects jsonToObject(byte[] data) {
        String jsonString = new String(data);
        return gson.fromJson(jsonString, SavedObjects.class);
    }
}
