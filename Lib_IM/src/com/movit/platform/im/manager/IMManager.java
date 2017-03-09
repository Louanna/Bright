package com.movit.platform.im.manager;

import android.content.Context;
import android.content.Intent;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.utils.JSONConvert;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/22.
 */
public class IMManager {

    public interface CallBack{
        public void refreshUI();
    }

    //获取聊天记录列表
    public static void getContactList(final Context context, final CallBack callBack){
        String url = CommConstants.URL_EOP_IM + "im/getContactList?userName=" + MFSPHelper.getString(CommConstants.USERNAME).toLowerCase();
        HttpManager.getJsonWithToken(url, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                if(null!=callBack){
                    callBack.refreshUI();
                }
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    try {
                        String jsonStr = new JSONObject(response).getString("objValue");
                        if (StringUtils.notEmpty(jsonStr)) {
                            Map<String, Object> responseMap = JSONConvert.json2MessageBean(jsonStr, context);

                            IMConstants.contactListDatas.clear();
                            IMConstants.contactListDatas = (ArrayList<MessageBean>) responseMap.get("messageBean");

                            Intent contactIntent = new Intent(CommConstants.ACTION_CONTACT_LIST);
                            contactIntent.setPackage(context.getPackageName());
                            context.sendBroadcast(contactIntent);

                            //通知更新聊天未读数
                            Intent intent = new Intent(CommConstants.ACTION_SET_REDPOINT);
                            intent.setPackage(context.getPackageName());
                            context.sendBroadcast(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(null!=callBack){
                            callBack.refreshUI();
                        }
                    }
                }
            }
        });
    }

    //离开单聊会话
    public static void leavePrivateSession(String session) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "leaveSessionDialog");
            jsonObject.put("toJID", session.toLowerCase());
            ChatManager manager = XmppManager.getInstance()
                    .getConnection().getChatManager();
            Chat chat = manager.createChat("admin" + MessageManager.SUFFIX, null);
            chat.sendMessage(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //离开群聊会话
    public static void leaveGroupSession(String session) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "leaveRoomSessionDialog");
            jsonObject.put("roomID", session.toLowerCase());
            ChatManager manager = XmppManager.getInstance()
                    .getConnection().getChatManager();
            Chat chat = manager.createChat("admin" + MessageManager.SUFFIX,
                    null);
            chat.sendMessage(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
