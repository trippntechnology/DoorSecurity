package com.trippntechnology.doorsecurity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;


public class RelayToWidget implements RemoteViewsService.RemoteViewsFactory {
    private Relay[] relays;
    private ArrayList<Relay> test = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;
    private static final String FILES = "RegistrationFiles";


    public RelayToWidget(Context context, Intent intent) {
        this.context = context;
        FileGetter fileGetter = new FileGetter();
        if (fileGetter.checkFileExistence(FILES, context)) {
            SavedObjects savedObjects = fileGetter.getSavedObjects(FILES, context);
            relays = savedObjects.relays;
        }
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

//        populateListItem();
    }

    private void populateListItem() {
        for (int i = 0; i < 10; i++) {
            Relay relay = new Relay("relay" + i, i);
            test.add(relay);
        }


//        for (int i = 0; i < 10; i++) {
//            ListItem listItem = new ListItem();
//            listItem.heading = "Heading" + i;
//            listItem.content = i
//                    + " This is the content of the app widget listview.Nice content though";
//            listItemList.add(listItem);
//        }

    }

    @Override
    public int getCount() {
        return test.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews textView = new RemoteViews(
                context.getPackageName(), R.layout.widget_list_layout);
//        Relay relay = test.get(position);
        if (relays.length > 0) {
            Relay relay = relays[position];
            textView.setTextViewText(R.id.widgetText, relay.Description);
            textView.setContentDescription(R.id.widgetText, Integer.toString(relay.ID));
        }
//        ListItem listItem = listItemList.get(position);
//        textView.setTextViewText(R.id.heading, listItem.heading);
//        textView.setTextViewText(R.id.content, listItem.content);

        return textView;
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
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }
}
