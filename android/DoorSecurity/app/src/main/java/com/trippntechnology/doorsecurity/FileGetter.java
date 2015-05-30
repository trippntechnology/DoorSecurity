package com.trippntechnology.doorsecurity;

import android.content.Context;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileGetter {
    Gson gson = new Gson();

    public FileGetter(){}

    public SavedObjects getSavedObjects(String filename,Context context){
        return jsonToObject(readFile(filename,context));
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
