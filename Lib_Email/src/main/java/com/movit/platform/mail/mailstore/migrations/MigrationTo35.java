package com.movit.platform.mail.mailstore.migrations;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;



class MigrationTo35 {
    public static void updateRemoveXNoSeenInfoFlag(SQLiteDatabase db) {
        try {
            db.execSQL("update messages set flags = replace(flags, 'X_NO_SEEN_INFO', 'X_BAD_FLAG')");
        } catch (SQLiteException e) {

        }
    }
}
