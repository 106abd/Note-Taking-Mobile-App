package com.example.noteme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.noteme.NoteContract.*;

import androidx.annotation.Nullable;

public class NoteDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "noteHistory.db";
    public static final int DATABASE_VERSION = 1;

    public NoteDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        final String SQL_LITE_CREATE_NOTEHISTORY_TABLE = "CREATE TABLE " +
                NoteEntry.TABLE_NAME + " (" +
                NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_COLOR + " INTEGER, " +
                NoteEntry.COLUMN_IMAGE_URI + " TEXT, " +
                NoteEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        database.execSQL(SQL_LITE_CREATE_NOTEHISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME);
        onCreate(database);

    }
}
