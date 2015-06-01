package com.trippntechnology.doorsecurity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
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

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget);
            Intent startActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(R.id.title, startActivityPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);

            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.widgetListView, intent);

            rv.setEmptyView(R.id.widgetListView, R.id.empty_view);

            Intent toastIntent = new Intent(context, WidgetProvider.class);

            toastIntent.setAction(WidgetProvider.TOAST_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widgetListView, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
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

        }
        super.onReceive(context, intent);
    }
}
