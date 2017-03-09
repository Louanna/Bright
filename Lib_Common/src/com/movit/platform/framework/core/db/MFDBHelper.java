package com.movit.platform.framework.core.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/11.
 */
public abstract class MFDBHelper extends SQLiteOpenHelper {

    //key:table名字
    //value:table创建语句
    private static Map<String,String> tableMap = new HashMap<>();

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    public MFDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MFDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        tableMap = initTable();

        db.beginTransaction();
        for(String key : tableMap.keySet()){
            db.execSQL(CREATE_TABLE + key + " " + tableMap.get(key));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(String key : tableMap.keySet()){
            db.execSQL(DROP_TABLE + key);
        }
        onCreate(db);
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    //定义Table名称和创建语句
    protected abstract Map<String,String> initTable();
}
