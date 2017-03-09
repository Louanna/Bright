package com.movit.platform.im.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.entities.SerializableObj;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.im.constants.IMConstants;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class MessageReceiver extends BroadcastReceiver {

    private CallBack callBack;

    public MessageReceiver(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (CommConstants.ACTION_NEW_MESSAGE.equals(action)) {
            try {
                MessageBean bean = (MessageBean) intent
                        .getSerializableExtra("messageDataObj");
                //声音提醒
//                if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
//                    if (!IMConstants.CHATTING_ID.equalsIgnoreCase(bean.getFriendId())) {
                        IMConstants.Dingdong(context);
                        //设置红点数字提醒
                        setPoint();
//                    }
//                } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
//                    if (!IMConstants.CHATTING_ID.equalsIgnoreCase(bean.getRoomId())) {
//                        IMConstants.Dingdong(context);
//                        //设置红点数字提醒
//                        setPoint();
//                    }
//                }
                callBack.receiveNewMessage(bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(CommConstants.ACTION_SESSION_MESSAGE_LIST.equalsIgnoreCase(action)){
            List<MessageBean> messageBeans = (List<MessageBean>) intent
                    .getSerializableExtra("sessionMessageList");
            String atMessage = intent.getStringExtra("tipsAtMessage");
            callBack.receiveSessionList(messageBeans,atMessage);

        } else if (CommConstants.ACTION_SET_REDPOINT.equals(action)) {
            //设置红点数字提醒
            setPoint();
        } else if (CommConstants.MSG_UPDATE_SEND_STATUS_ACTION.equals(action)) {
            //发送广播更新界面UI显示
            callBack.updateRecordList(((SerializableObj) (intent.getSerializableExtra("recordsMap"))).getMap());
        }else if(CommConstants.ACTION_GROUP_LIST_RESPONSE.equals(action)){
            //更新group信息
            callBack.updateGroup();
        }
    }

    public void setPoint() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                try {
                    int point = 0;
                    Set<String> unreadIds = MFSPHelper.getStringSet(CommConstants.MARK_UNREAD_IDS);
                    Set<String> readIds = MFSPHelper.getStringSet(CommConstants.MARK_READ_IDS);
                    for (MessageBean messageBean : IMConstants.contactListDatas) {
                        if (messageBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                            if (unreadIds.contains(messageBean.getFriendId())) {
                                point += 1;
                            } else if (readIds.contains(messageBean.getFriendId())) {
                                continue;
                            } else {
                                point += messageBean.getUnReadCount();
                            }
                        } else if (messageBean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            if (unreadIds.contains(messageBean.getRoomId())) {
                                point += 1;
                            } else if (readIds.contains(messageBean.getRoomId())) {
                                continue;
                            } else {
                                point += messageBean.getUnReadCount();
                            }
                        } else {
                            point += messageBean.getUnReadCount();
                        }
                    }
                    callBack.setRedPoint(point);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface CallBack {
        public void setRedPoint(int point);

        public void updateRecordList(Map<String, Integer> recordMap);

        public void receiveNewMessage(MessageBean messageBean);

        public void receiveSessionList(List<MessageBean> messageBeans,String atMessage);

        public void updateGroup();
    }
}
