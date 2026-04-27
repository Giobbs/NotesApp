package com.example.notesapp.ui.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

public class WidgetUpdater {

    public static void update(Context context) {

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, NotesWidgetProvider.class);

        int[] ids = manager.getAppWidgetIds(component);

        if (ids != null && ids.length > 0) {
            new NotesWidgetProvider().onUpdate(context, manager, ids);
        }
    }
}