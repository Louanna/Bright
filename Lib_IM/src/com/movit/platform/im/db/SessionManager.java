package com.movit.platform.im.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;

/**
 * 聊天记录SessionList操作类
 */
public class SessionManager {

    private IMDBHepler dbHelper;
    private SQLiteDatabase db;

    private static SessionManager manager;
    private SessionManager(IMDBHepler dbHelper) {
        this.dbHelper = dbHelper;
        this.db = dbHelper.getWritableDatabase();
    }

    public static SessionManager getInstance(IMDBHepler dbHelper) {
        if (manager == null) {
            manager = new SessionManager(dbHelper);
        }
        return manager;
    }

    /***
     * 共有两处会调用该方法
     * 1、getContactList之后，将服务器端返回的sessionList保存
     * 2、XMPP连接断开时，保存当前页面中的SessionList到数据库中
     * ***/
    public int insertSession(MessageBean mData) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (mData.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
            values.put("sid", mData.getFriendId().toLowerCase());
            values.put("ctype", "chat");
        } else if (mData.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
            values.put("sid", mData.getRoomId().toLowerCase());
            values.put("ctype", "groupchat");
        }
        long sid = db.insert(IMDBHepler.TABLE_SESSION, null, values);
        return (int) sid;
    }

    public void deleteAllSession() {
        db = dbHelper.getReadableDatabase();
        db.delete(IMDBHepler.TABLE_SESSION, null, null);
    }

    public void deleteSession(String sid) {
        db = dbHelper.getReadableDatabase();
        db.delete(IMDBHepler.TABLE_SESSION, "sid = " + "'" + sid.toLowerCase() + "'",
                null);
    }

    public ArrayList<String> getSessionList() {
        db = dbHelper.getReadableDatabase();
        Cursor c = db.query(IMDBHepler.TABLE_SESSION, null, null, null,
                null, null, null);
        ArrayList<String> list = new ArrayList<String>();
        while (c.moveToNext()) {
            String sid = c.getString(c.getColumnIndex("sid")).toLowerCase();
            list.add(sid);
        }
        if(!c.isClosed()){
            c.close();
        }
        return list;
    }

    /**
     * 关闭数据库
     */
    public void closeDb() {
        try {
            if (db != null) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
