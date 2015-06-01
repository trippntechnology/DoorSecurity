package com.trippntechnology.doorsecurity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private List<Relay> relayList = new ArrayList<Relay>();
    private Context context;
    private Relay[] relays = new Relay[0];
    private int mAppWidgetId;
    private final static boolean testing = false;
    private byte[] key,iv,encrypted;
    private String url;
    private AuthToken authToken = new AuthToken();
    private String phone;

    public WidgetFactory(Context context, Intent intent) {
        this.context = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    // Initialize the data set.
    @Override
    public void onCreate() {
        if (testing){
            for (int i = 0; i < 10; i++) {
                relayList.add(new Relay("Relay"+i,i));
            }
        }else {
            FileGetter fileGetter = new FileGetter();
            phone = fileGetter.getPhoneNumber(context);
            if (fileGetter.checkFileExistence(MainActivity.FILES, context)){
                SavedObjects savedObjects = fileGetter.getSavedObjects(MainActivity.FILES,context);
                relays = savedObjects.relays;
                key = savedObjects.key;
                iv = savedObjects.iv;
                url = savedObjects.URL;
                encrypted = authToken.encrypt(new SecretKeySpec(key,"AES"),new IvParameterSpec(iv),
                        fileGetter.getMacAddress(context));
                if(relays !=null) {
                    Arrays.asList(relays);
                }
            }
        }
        // In onCreate() you set up any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    }


    @Override
    public int getCount() {
        return relays.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    @Override
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_list_layout);
        rv.setTextViewText(R.id.widgetText, relays[position].Description);

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(WidgetProvider.EXTRA_ITEM, position);
        extras.putInt(WidgetProvider.DOOR_ID, relays[position].ID);
        extras.putByteArray(WidgetProvider.ENCRYPTED, encrypted);
        extras.putString(WidgetProvider.URL, url);
        extras.putString(WidgetProvider.PHONENUMBER,phone);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        rv.setOnClickFillInIntent(R.id.widgetText, fillInIntent);


        // Return the RemoteViews object.
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

}
