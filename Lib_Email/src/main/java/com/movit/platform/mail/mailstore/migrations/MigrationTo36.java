package com.movit.platform.mail.mailstore.migrations;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


class MigrationTo36 {
    public static void addAttachmentsContentIdColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE attachments ADD content_id TEXT");
        } catch (SQLiteException e) {

        }
    }
}
