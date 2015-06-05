package com.trippntechnology.doorsecurity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class WidgetProvider extends AppWidgetProvider {
    public static final String TOAST_ACTION = "TOAST_ACTION";
    public static final String EXTRA_ITEM = "EXTRA_ITEM";
    public static final String DOOR_ID = "DOOR_ID";
    public static final String ENCRYPTED = "ENCRYPT";
    public static final String URL = "URL";
    public static final String PHONENUMBER = "PHONE";
    public static final String TITLE = "TITLE";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);  // Identifies the particular widget...
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
// Make the pending intent unique...
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.widgetTitle, pendIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i],views);



            //List View
            Intent listIntent = new Intent(context, WidgetService.class);
            listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.widgetListView, listIntent);

            rv.setEmptyView(R.id.widgetListView, R.id.empty_view);

            Intent listButtons = new Intent(context, WidgetProvider.class);

            listButtons.setAction(WidgetProvider.TOAST_ACTION);
            listButtons.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent listPendingIntent = PendingIntent.getBroadcast(context, 0, listButtons,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widgetListView, listPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.d("TAG", "onReceive() " + intent.getAction());

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(TOAST_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            int id = intent.getIntExtra(DOOR_ID, 0);
            byte[] encrypted = intent.getByteArrayExtra(ENCRYPTED);
            String url = intent.getStringExtra(URL);
            String phoneNumber = intent.getStringExtra(PHONENUMBER);
            DoorObject door = new DoorObject();
            door.PhoneNumber = phoneNumber;
            if (encrypted != null) {
                door.AuthToken = Base64.encodeToString(encrypted, Base64.NO_WRAP);
                door.ID = id;
                //Rest Call
                OkHttpClient http = new OkHttpClient();
                http.setConnectTimeout(6000, TimeUnit.MILLISECONDS);
                RestAdapter restAdapter = new RestAdapter.Builder().setClient(new OkClient(http)).setEndpoint(url).build();
                Interface client = restAdapter.create(Interface.class);
                client.openDoor(door, new Callback<StandardResponse>() {
                    @Override
                    public void success(StandardResponse standardResponse, Response response) {
                        boolean success = Boolean.parseBoolean(standardResponse.Success);
                        if (success) {
                            Toast toast = Toast.makeText(context, R.string.door_open_success, Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            String message = standardResponse.Message;
                            Toast.makeText(context, "Server response" +
                                    R.string.door_open_failure + "\n" + message, Toast.LENGTH_LONG).show();

                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getMessage().contains("failed to connect")) {
                            Toast toast = Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(context, "Retrotfit\n" + error.getMessage(), Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
            } else {
                Toast toast = Toast.makeText(context, R.string.encryption_error, Toast.LENGTH_SHORT);
                toast.show();
            }

        }else if (intent.getAction().equals(TITLE)){
            Toast toast = Toast.makeText(context, "TEST", Toast.LENGTH_SHORT);
            toast.show();
        }
        super.onReceive(context, intent);
    }

}
