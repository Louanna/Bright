package com.movit.platform.im.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.StringUtils;

/**
 * 聊天记录Message操作类
 */
public class RecordsManager {

    public static final int MESSAGE_TYPE_SEND = 1;
    public static final int MESSAGE_TYPE_RECEIVE = 0;

    private Context context;

    private IMDBHepler dbHelper;
    private SQLiteDatabase db;

    private static RecordsManager manager;

    private RecordsManager(Context context, IMDBHepler dbHelper) throws SQLException {
        this.context = context;
        this.dbHelper = dbHelper;
    }

    public static RecordsManager getInstance(Context mContext, IMDBHepler dbHelper) {
        if (manager == null) {
            manager = new RecordsManager(mContext, dbHelper);
        }
        return manager;
    }

    public boolean isExisted(String msgId) {
        boolean isHas = false;
        db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            Cursor c = db.rawQuery("SELECT * FROM " + IMDBHepler.TABLE_RECORD + " WHERE msgId ='" + msgId + "'", null);
            isHas = c.moveToNext();
            db.setTransactionSuccessful();// 事务成功
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();// 结束事务
            db.close();
        }
        return isHas;
    }

    public void insertRecord(MessageBean messageObj, int messageType, RecordsCallback callback) {

        if (isExisted(messageObj.getMsgId())) {
            updateRecord(messageObj.getTimestamp(), messageObj.getIsSend(), messageObj.getMsgId());
        } else {
            String sql = "INSERT INTO " + IMDBHepler.TABLE_RECORD + " (msgId,content,msgFrom,msgTo,type,status,rsFlag,st,time) VALUES(?,?,?,?,?,?,?,?,?)";
            db = dbHelper.getWritableDatabase();
            SQLiteStatement stat = db.compileStatement(sql);

            try {
                db.beginTransaction();

                stat.bindString(1, messageObj.getMsgId());
                stat.bindString(2, messageObj.getContent());

                if (CommConstants.CHAT_TYPE_GROUP == messageObj.getCtype()) {
                    stat.bindString(3, messageObj.getFriendId().toLowerCase());
                    stat.bindString(4, messageObj.getRoomId().toLowerCase());

                    switch (messageType) {
                        case MESSAGE_TYPE_SEND:
                            stat.bindLong(6, CommConstants.MSG_SEND_PROGRESS);
                            break;
                        case MESSAGE_TYPE_RECEIVE:
                            stat.bindLong(6, CommConstants.MSG_SEND_SUCCESS);
                            break;
                        default:
                            break;
                    }
                } else if (CommConstants.CHAT_TYPE_SINGLE == messageObj.getCtype()) {
                    switch (messageType) {
                        case MESSAGE_TYPE_SEND:
                            stat.bindString(3, messageObj.getCuserId().toLowerCase());
                            stat.bindString(4, messageObj.getFriendId().toLowerCase());
                            stat.bindLong(6, CommConstants.MSG_SEND_PROGRESS);
                            break;
                        case MESSAGE_TYPE_RECEIVE:
                            stat.bindString(3, messageObj.getFriendId().toLowerCase());
                            stat.bindString(4, messageObj.getCuserId().toLowerCase());
                            stat.bindLong(6, CommConstants.MSG_SEND_SUCCESS);
                            break;
                        default:
                            break;
                    }
                }

                stat.bindLong(5, messageObj.getCtype());
                stat.bindLong(7, messageType);
                stat.bindString(8, messageObj.getTimestamp());
                stat.bindString(9, messageObj.getFormateTime());
                stat.executeInsert();

                db.setTransactionSuccessful();// 事务成功
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();// 结束事务
                db.close();
                callback.sendBroadcast();
            }
        }
    }

    //两种情况下会调用如下的方法
    //1、EnterSession获取sessionList
    //2、API获取历史消息
    public void insertRecords(List<MessageBean> messageObjs, RecordsCallback callback) {

        String sql = "INSERT INTO " + IMDBHepler.TABLE_RECORD + " (msgId,content,msgFrom,msgTo,type,status,rsFlag,st,time) VALUES(?,?,?,?,?,?,?,?,?)";
        db = dbHelper.getWritableDatabase();
        SQLiteStatement stat = db.compileStatement(sql);
        try {
            db.beginTransaction();

            //目前默认从服务器端每次取200条数据
            //如果正好取到200条则将手机本地的聊天记录全部删除
            //防止未读消息超过200条，而此时手机本地还存在很早之前的数据，造成本地数据不连贯的现象
            if(messageObjs.size()==200){
                db.delete(IMDBHepler.TABLE_RECORD, "(msgFrom = " + "'" + messageObjs.get(0).getFriendId().toLowerCase() + "'"+" and msgTo = " + "'" + messageObjs.get(0).getCuserId().toLowerCase() + "')"+" or " +"(msgFrom = " + "'" + messageObjs.get(0).getCuserId().toLowerCase() + "'"+" and msgTo = " + "'" + messageObjs.get(0).getFriendId().toLowerCase() + "')",
                        null);
            }

            for (MessageBean messageObj : messageObjs) {
                Cursor c = db.rawQuery("SELECT * FROM  " + IMDBHepler.TABLE_RECORD + "  WHERE msgId ='" + messageObj.getMsgId() + "'", null);
                boolean isHas = c.moveToNext();
                c.close();

                if (!isHas) {

                    stat.bindString(1, messageObj.getMsgId());
                    stat.bindString(2, messageObj.getContent());

                    if (CommConstants.CHAT_TYPE_GROUP == messageObj.getCtype()) {
                        stat.bindString(3, messageObj.getFriendId().toLowerCase());
                        stat.bindString(4, messageObj.getRoomId().toLowerCase());
                    } else if (CommConstants.CHAT_TYPE_SINGLE == messageObj.getCtype()) {
                        switch (messageObj.getRsflag()) {
                            case MESSAGE_TYPE_SEND:
                                stat.bindString(3, messageObj.getCuserId().toLowerCase());
                                stat.bindString(4, messageObj.getFriendId().toLowerCase());
                                break;
                            case MESSAGE_TYPE_RECEIVE:
                                stat.bindString(3, messageObj.getFriendId().toLowerCase());
                                stat.bindString(4, messageObj.getCuserId().toLowerCase());
                                break;
                            default:
                                break;
                        }
                    }

                    stat.bindLong(5, messageObj.getCtype());
                    stat.bindLong(6, CommConstants.MSG_SEND_PROGRESS);
                    stat.bindLong(7, messageObj.getRsflag());
                    stat.bindString(8, messageObj.getTimestamp());
                    stat.bindString(9, messageObj.getFormateTime());
                    stat.executeInsert();
                }
            }
                db.setTransactionSuccessful();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                db.endTransaction();// 结束事务
                db.close();
                if (null != callback) {
                    callback.sendBroadcast();
                }
            }
        }

        //获取本地DB中最新一条聊天消息的st

    public String getStartTimeAndEndTime(final String firendId, final String cuserId) {
        String sql = "select * from " + IMDBHepler.TABLE_RECORD + " where msgFrom ='" + firendId.toLowerCase() + "' and msgTo ='" + cuserId.toLowerCase() + "' or msgFrom ='" + cuserId.toLowerCase() + "' and msgTo='" + firendId.toLowerCase() + "' ORDER BY time desc, st desc";
        return getSt(sql);
    }

    private String getSt(String sql){
        ArrayList<MessageBean> messageBeans = ExecSQLForMessageBean(sql);
        String endTime = "";
        if (null != messageBeans && messageBeans.size() > 0) {
            for (int i = 0; i < messageBeans.size(); i++) {
                MessageBean messageBean = messageBeans.get(i);
                if (StringUtils.notEmpty(messageBean.getTimestamp())) {
                    if (messageBean.getInsertFlag() == 0) {
                        endTime = messageBean.getTimestamp();
                    } else {
                        return messageBean.getTimestamp() + "," + endTime;
                    }
                }
            }
        }
        return endTime;
    }

    //获取当前session本地DB中最新一条聊天消息的st
    public String getStartTimeAndEndTime(final String roomId) {
        String sql = "select * from " + IMDBHepler.TABLE_RECORD + " where msgTo ='" + roomId.toLowerCase() + "' ORDER BY time desc, st desc";
        return getSt(sql);
    }

    //获取聊天记录，在单聊界面显示
    public ArrayList<MessageBean> getRecordsByFriendId(final String firendId, final String cuserId) {
        String sql = "select * from (select * from " + IMDBHepler.TABLE_RECORD + " where msgFrom ='" + firendId.toLowerCase() + "' and msgTo ='" + cuserId.toLowerCase() + "' or msgFrom ='" + cuserId.toLowerCase() + "' and msgTo='" + firendId.toLowerCase() + "' ORDER BY time desc, st desc limit 0, 20) ORDER BY time asc,st asc";
        return ExecSQLForSingleChat(sql);
    }

    //获取聊天记录，在群聊界面显示
    public ArrayList<MessageBean> getRecordsByRoomId(final String roomId) {
        String sql = "select * from (select * from " + IMDBHepler.TABLE_RECORD + " where msgTo ='" + roomId.toLowerCase() + "' ORDER BY time desc, st desc limit 0, 20) ORDER BY time asc, st asc";
        return ExecSQLForMessageBean(sql);
    }

    //修改消息写入标识
    //在当前消息聊天窗口时调用
    public void updateMsgInsertFlag(String msgId, int insertFlag) {
        String sql = "UPDATE " + IMDBHepler.TABLE_RECORD + " SET insertFlag = " + insertFlag + " WHERE msgId =" + "'" + msgId + "'";
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();// 结束事务
            db.close();
        }
    }

    //用于APP与服务器端同步聊天记录
    //用于修改APP端重发曾发送失败的聊天记录
    public void updateRecord(String timestamp, int status, String msgId) {
        String sql;

        if (null != timestamp && !"".equalsIgnoreCase(timestamp)) {
            sql = "UPDATE " + IMDBHepler.TABLE_RECORD + " SET st = '" + timestamp + "', status =" + status + " WHERE msgId =" + "'" + msgId + "'";
        } else {
            sql = "UPDATE " + IMDBHepler.TABLE_RECORD + "  SET status =" + status + " WHERE msgId =" + "'" + msgId + "'";
        }

        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();// 结束事务
            db.close();
        }
    }

    //修改本地音频、图片文件路径
    public void updateRecord(String content, String msgId) {
        String sql = "UPDATE " + IMDBHepler.TABLE_RECORD + "  SET content ='" + content + "' WHERE msgId =" + "'" + msgId + "'";
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();// 结束事务
            db.close();
        }
    }

    //获取聊天记录，在单聊界面显示
    public ArrayList<MessageBean> getHistoryRecords(MessageBean messageBean) {
        String sql = "select * from " + "(select * from " + IMDBHepler.TABLE_RECORD + " where (msgFrom ='" + messageBean.getFriendId().toLowerCase() + "' and msgTo ='" + messageBean.getCuserId().toLowerCase() + "' and st < '" + messageBean.getTimestamp() + "') or (msgFrom ='" + messageBean.getCuserId().toLowerCase() + "' and msgTo='" + messageBean.getFriendId().toLowerCase() + "' and st < '" + messageBean.getTimestamp() + "') ORDER BY time desc limit 20) ORDER BY time asc";
        return ExecSQLForSingleChat(sql);
    }

    //获取聊天记录，在群聊界面显示
    public ArrayList<MessageBean> getRoomHistoryRecords(MessageBean messageBean) {
        String sql = "select * from (select * from " + IMDBHepler.TABLE_RECORD + " where msgTo ='" + messageBean.getRoomId().toLowerCase() + "' and st < '" + messageBean.getTimestamp() + "' ORDER BY time desc limit 20) ORDER BY time asc";
        return ExecSQLForMessageBean(sql);
    }


    /**
     * 执行SQL命令返回list
     *
     * @param sql
     * @return
     */
    private ArrayList<MessageBean> ExecSQLForSingleChat(String sql) {
        ArrayList<MessageBean> list = new ArrayList<MessageBean>();
        Cursor c = ExecSQLForCursor(sql);
        while (c.moveToNext()) {
            MessageBean record = new MessageBean();

            record.setMsgId(c.getString(c.getColumnIndex("msgId")));
            record.setContent(c.getString(c.getColumnIndex("content")));
            record.setRsflag(c.getInt(c.getColumnIndex("rsFlag")));
            switch (record.getRsflag()) {
                case MESSAGE_TYPE_SEND:
                    record.setFriendId(c.getString(c.getColumnIndex("msgTo")));
                    record.setCuserId(c.getString(c.getColumnIndex("msgFrom")));
                    break;
                case MESSAGE_TYPE_RECEIVE:
                    record.setFriendId(c.getString(c.getColumnIndex("msgFrom")));
                    record.setCuserId(c.getString(c.getColumnIndex("msgTo")));
                    break;
                default:
                    break;
            }
            record.setCtype(c.getInt(c.getColumnIndex("type")));
            record.setIsSend(c.getInt(c.getColumnIndex("status")));
            record.setTimestamp(c.getString(c.getColumnIndex("st")));
            record.setFormateTime(c.getString(c.getColumnIndex("time")));
            record.setInsertFlag(c.getInt(c.getColumnIndex("insertFlag")));

            UserDao userDao = UserDao.getInstance(context);
            UserInfo userInfo = userDao.getUserInfoByADName(record.getFriendId());
            record.setUserInfo(userInfo);
            list.add(record);
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * 执行SQL命令返回list
     *
     * @param sql
     * @return
     */
    private ArrayList<MessageBean> ExecSQLForMessageBean(String sql) {
        ArrayList<MessageBean> list = new ArrayList<MessageBean>();
        Cursor c = ExecSQLForCursor(sql);
        while (c.moveToNext()) {
            MessageBean record = new MessageBean();

            record.setMsgId(c.getString(c.getColumnIndex("msgId")));
            record.setContent(c.getString(c.getColumnIndex("content")));
            record.setFriendId(c.getString(c.getColumnIndex("msgFrom")));
            record.setCuserId(c.getString(c.getColumnIndex("msgTo")));
            record.setCtype(c.getInt(c.getColumnIndex("type")));
            record.setIsSend(c.getInt(c.getColumnIndex("status")));
            record.setRsflag(c.getInt(c.getColumnIndex("rsFlag")));
            record.setTimestamp(c.getString(c.getColumnIndex("st")));
            record.setFormateTime(c.getString(c.getColumnIndex("time")));
            record.setInsertFlag(c.getInt(c.getColumnIndex("insertFlag")));

            UserDao userDao = UserDao.getInstance(context);
            UserInfo userInfo = userDao.getUserInfoByADName(c.getString(c.getColumnIndex("msgFrom")));
            record.setUserInfo(userInfo);
            list.add(record);
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * 执行SQL，返回游标
     *
     * @param sql
     * @return
     */
    private Cursor ExecSQLForCursor(String sql) {
        db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public interface RecordsCallback {
        public void sendBroadcast();
    }

}
