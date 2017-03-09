package com.movit.platform.im.db;

import android.content.Context;
import android.database.SQLException;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;

/**
 * Created by Administrator on 2016/6/15.
 */
public class IMDBFactory {

    private Context context;
    private IMDBHepler dbHelper;
    private static IMDBFactory manager;

    private IMDBFactory(Context context) throws SQLException {
        this.context = context;
        String DB_NAME = MFSPHelper.getString(CommConstants.EMPADNAME) + "_im.db";
        dbHelper = new IMDBHepler(context, DB_NAME);
    }

    public static IMDBFactory getInstance(Context mContext) {
        if (manager == null) {
            manager = new IMDBFactory(mContext);
        }
        return manager;
    }

    public SessionManager getSessionManager() {
        return SessionManager.getInstance(dbHelper);
    }

    public RecordsManager getRecordsManager() {
        return RecordsManager.getInstance(context,dbHelper);
    }

}
