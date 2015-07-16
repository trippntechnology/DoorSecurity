package com.trippntechnology.doorsecurity;


import android.content.Context;

import com.google.gson.Gson;
import com.trippntechnology.tntlibrary.FileGetter;

public class FileOverrides extends FileGetter {
    Gson gson = new Gson();
    @Override
    public boolean checkFileExistence(String fileName, Context context) {
        return super.checkFileExistence(fileName, context);
    }

    public FileOverrides() {
        super();
    }

    @Override
    public String getMacAddress(Context context) {
        return super.getMacAddress(context);
    }

    @Override
    public String getPhoneNumber(Context context) {
        return super.getPhoneNumber(context);
    }

    @Override
    public byte[] readFile(String filename, Context context) {
        return super.readFile(filename, context);
    }


    private SavedObjects jsonToObject(byte[] data) {
        String jsonString = new String(data);
        return gson.fromJson(jsonString, SavedObjects.class);
    }
    public SavedObjects getSavedObjects(String filename,Context context){
        return jsonToObject(readFile(filename, context));
    }
}
