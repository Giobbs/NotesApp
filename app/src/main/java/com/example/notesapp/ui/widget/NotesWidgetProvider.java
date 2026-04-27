package com.example.notesapp.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.notesapp.R;
import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;

import java.util.List;
import java.util.concurrent.Executors;

public class NotesWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Executors.newSingleThreadExecutor().execute(() -> {

            AppDatabase db = AppDatabase.getInstance(context);
            List<Note> notes = db.noteDao().getRecentNotes();

            for (int appWidgetId : appWidgetIds) {

                RemoteViews views = new RemoteViews(
                        context.getPackageName(),
                        R.layout.widget_notes
                );

                views.setTextViewText(R.id.note1,
                        notes.size() > 0 ? notes.get(0).title : "-");

                views.setTextViewText(R.id.note2,
                        notes.size() > 1 ? notes.get(1).title : "-");

                views.setTextViewText(R.id.note3,
                        notes.size() > 2 ? notes.get(2).title : "-");

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }
}