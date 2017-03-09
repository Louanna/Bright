package com.movit.platform.sc.timer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.manager.ZoneManager;

import com.movit.platform.framework.utils.LogUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Louanna.Lu on 2015/10/28.
 */
public class TimerHelper {

    private Timer timer;
    private Context context;
    private CallBack callBack;

    public TimerHelper(Context context, Timer timer,CallBack callBack) {
        this.timer = timer;
        this.context = context;
        this.callBack = callBack;
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case SCConstants.ZONE_MESSAGE_COUNT_RESULT:// 轮询 消息提醒:
                    LogUtils.d("debug","obj="+(String) msg.obj);
                    callBack.refreshZoneRedPoint((String) msg.obj);
                    break;
                case SCConstants.ZONE_NEW_SAY_COUNT_RESULT:
                    LogUtils.d("debug","obj="+(String) msg.obj);
                    callBack.showZoneRedPoint((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    public interface CallBack{
        public void refreshZoneRedPoint(String jsonStr);

        public void showZoneRedPoint(String jsonStr);
    }

    public void startZoneRedPointTimer(final String officeId){

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    String dCreateTime = MFSPHelper.getString("dCreateTime");
                    if (StringUtils.notEmpty(dCreateTime)) {
                        ZoneManager zoneManager = new ZoneManager(context);
                        zoneManager.havenew(officeId,dCreateTime, handler);
                    } else {
                        handler.sendEmptyMessage(99);
                    }
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            ZoneManager zoneManager = new ZoneManager(context);
                            zoneManager.messagecount(handler);
                        }
                    }, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, 30 * 1000);
    }

    public void stopZoneRedPointTimer(Timer timer){
        if (null!=timer) {
            timer.cancel();
        }
    }
}
