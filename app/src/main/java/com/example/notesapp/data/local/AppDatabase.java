package com.example.notesapp.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {Note.class},
        version = 2,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    // =========================
    // DAO
    // =========================
    public abstract NoteDao noteDao();

    // =========================
    // SINGLETON (thread-safe)
    // =========================
    private static volatile AppDatabase INSTANCE;

    private static final String DB_NAME = "notes.db";

    public static AppDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(context);
                }
            }
        }
        return INSTANCE;
    }

    // =========================
    // BUILD DATABASE
    // =========================
    private static AppDatabase buildDatabase(Context context) {
        return Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DB_NAME
                )
                .addCallback(roomCallback)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // solo dev, da rimuovere in produzione
                .build();
    }

    // =========================
    // CALLBACK (seed / init)
    // =========================
    private static final RoomDatabase.Callback roomCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                 }
            };

    // =========================
    // MIGRATION STRATEGY
    // =========================
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL(
                    "ALTER TABLE notes ADD COLUMN isProtected INTEGER NOT NULL DEFAULT 0"
            );

            database.execSQL(
                    "ALTER TABLE notes ADD COLUMN encryptedContent TEXT"
            );
        }
    };
}