package com.movit.platform.sc.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.ListCallback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.sc.R;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.entities.ZoneMessage;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.movit.platform.sc.utils.ZoneConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;

public class ZoneManager implements IZoneManager {
    private Context context;

    public ZoneManager(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void getKnowledge(String userId, int page,StringCallback responseCallback) {
        String url = CommConstants.URL_GET_KNOWLEDGE;
        OkHttpUtils.getWithToken()
                .url(url)
                .addParams("userId",userId)
                .addParams("page",page+"")
                .build()
                .execute(responseCallback);
    }

    @Override
    public void saveKnowledgeComment(String knowledgeId, String userId, String content, StringCallback responseCallback) {
        String url = CommConstants.URL_SAVE_KNOWLEDGE_COMMENT;
        OkHttpUtils.postWithToken()
                .url(url)
                .addParams("userId",userId)
                .addParams("knowledgeId",knowledgeId)
                .addParams("content",content)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void saveKnowlegeLike(String knowledgeId, String userId, String createBy, StringCallback responseCallback) {
        String url = CommConstants.URL_SAVE_KNOWLEDGE_LIKE;
        OkHttpUtils.postWithToken()
                .url(url)
                .addParams("userId",userId)
                .addParams("knowledgeId",knowledgeId)
                .addParams("createBy",createBy)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void delKnowledgeLike(String knowledgeId, String userId, StringCallback responseCallback) {
        String url = CommConstants.URL_DEL_KNOWLEDGE_LIKE;
        OkHttpUtils.postWithToken()
                .url(url)
                .addParams("userId",userId)
                .addParams("knowledgeId",knowledgeId)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void uploadFile(final String filePath, final Handler handler) {
        File file = new File(filePath);
        HttpManager.uploadFile(CommConstants.URL_UPLOAD, null, file, "file", new ListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                handler.sendEmptyMessage(5);
            }

            @Override
            public void onResponse(JSONArray response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = 1;
                    msg.obj = response.getJSONObject(0).getString("uName");
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * @param refreshTime 刷新时间
     * @param tCreateTime 第一条时间
     * @param bCreateTime 最后一条时间
     * @param isAfter     默认1 0为时间之前消息
     * @param type        默认 all 消息类型：工作 1\生活 0
     * @param isSecret    默认 0 1隐私消息
     * @param handler
     */
    @Override
    public void getZoneList(final String officeId, final String refreshTime,
                            final String tCreateTime, final String bCreateTime,
                            final String isAfter, final String type, final String isSecret,
                            final Handler handler) {
        String url = "";
        try {
            url = SCConstants.GET_ZONE_LIST_DATA + "?userId="
                    + MFSPHelper.getString(CommConstants.USERID)
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            if (!"".equals(refreshTime)) {
                url += "&refreshTime="
                        + URLEncoder.encode(refreshTime, "UTF-8");
            }
            if (!"".equals(tCreateTime)) {
                url += "&tCreateTime="
                        + URLEncoder.encode(tCreateTime, "UTF-8");
            }
            if (!"".equals(bCreateTime)) {
                url += "&bCreateTime="
                        + URLEncoder.encode(bCreateTime, "UTF-8");
            }
            if (!"".equals(isAfter)) {
                url += "&isAfter=" + isAfter;
            }
            if (!"".equals(type)) {
                url += "&type=" + type;
            }
            if (!"".equals(isSecret)) {
                url += "&isSecret=" + isSecret;
            }

            url += "&officeId=" + officeId;

        } catch (Exception e) {
            e.printStackTrace();
            handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                    context.getString(R.string.get_message_fail)).sendToTarget();
        }

        OkHttpUtils.getWithToken()
                .url(url + "&iPageRowCount=15")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                                context.getString(R.string.get_message_fail)).sendToTarget();
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            ArrayList<String> delList = ZoneConvert.getZoneListDataDel(
                                    result);
                            ArrayList<Zone> newList = ZoneConvert.getZoneListDataNew(
                                    result, context);
                            ArrayList<Zone> oldList = ZoneConvert.getZoneListDataOld(
                                    result, context);
                            ArrayList<Zone> topList = ZoneConvert.getZoneListDataTop(
                                    result, context);

                            JSONObject jsonObject = new JSONObject(result);
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                JSONObject list = jsonObject.getJSONObject("item");
                                if (list.has("refreshTime")) {
                                    String refreshTime = list.getString("refreshTime");
                                    MFSPHelper.setString("refreshTime", refreshTime);
                                }
                            }

                            Message message = new Message();
                            Bundle data = new Bundle();
                            data.putStringArrayList("delList", delList);
                            data.putSerializable("newList", newList);
                            data.putSerializable("oldList", oldList);
                            data.putSerializable("topList", topList);
                            message.setData(data);

                            if ("1".equals(isAfter)) {
                                message.what = SCConstants.ZONE_MORE_RESULT;
                                handler.sendMessage(message);
                            } else {
                                if (!newList.isEmpty()) {
                                    String dCreateTime = newList.get(0)
                                            .getdCreateTime();
                                    MFSPHelper.setString("dCreateTime", dCreateTime);
                                }
                                message.what = SCConstants.ZONE_LIST_RESULT;
                                handler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                                    context.getString(R.string.get_message_fail)).sendToTarget();
                        }
                    }
                });
    }

    /**
     * @param content      文本
     * @param type         说说类型 work 1,life 0
     * @param isSecret     是否隐私 1是 0否
     * @param sImages      图片URL
     * @param sAtGroup
     * @param sAtPerson
     * @param sMessageList
     * @param handler
     * @ 组ID
     * @ 人ID
     * @ 发送人ID
     */
    @Override
    public void say(final String content, final String type,
                    final String isSecret, final String sImages, final String sAtGroup,
                    final String sAtPerson, final String sMessageList,
                    final Handler handler) {

//        String sContent = "";
        String token = "";
        try {
//            sContent = URLEncoder.encode(content, "UTF-8");
            token = URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpUtils.postWithToken()
                .url(SCConstants.PUBLISH_ZONE_SAY)
                .addParams("userId", MFSPHelper.getString(CommConstants.USERID))
                .addParams("type", type)
                .addParams("isSecret", isSecret)
                .addParams("sContent", content)
                .addParams("sImages", sImages)
                .addParams("sAtGroup", sAtGroup)
                .addParams("sAtPerson", sAtPerson)
                .addParams("sMessageList", sMessageList)
                .addParams("Token", token)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(ZonePublishActivity.ZONE_SAY_RESULT,
                                response).sendToTarget();
                    }
                });
    }

    /**
     * @param cSayId   说说id
     * @param touserId 目标用户uid
     * @param handler
     */
    @Override
    public void nice(final String cSayId, final String touserId,
                     final String undo, final int postion, final Handler handler) {

        String token = "";
        try {
            token = URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("userId", MFSPHelper.getString(CommConstants.USERID));
        params.put("cSayId", cSayId);
        params.put("touserId", touserId);
        params.put("Token", token);
        if (StringUtils.notEmpty(undo)) {
            params.put("undo", "1");
        }

        OkHttpUtils.postWithToken()
                .url(SCConstants.ZONE_NICE)
                .params(params)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        int isNice = 0;
                        if (StringUtils.notEmpty(undo)) {
                            isNice = 1;
                        }
                        handler.obtainMessage(SCConstants.ZONE_NICE_RESULT,
                                postion, isNice, response).sendToTarget();
                    }
                });

    }

    /**
     * @param cSayId    说说id
     * @param userId    用户Id
     * @param touserId  发送对象用户Id
     * @param sContent  文本
     * @param cParentId 评论父节点ID
     * @param cRootId   评论根节点ID
     * @param postion
     * @param handler
     */
    @Override
    public void comment(final String cSayId, final String userId,
                        final String touserId, final String sContent,
                        final String cParentId, final String cRootId, final int postion,
                        final Handler handler) {

        String token = "";
        try {
            token = URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("userId", MFSPHelper.getString(CommConstants.USERID));
        params.put("cSayId", cSayId);
        params.put("touserId", touserId);
        params.put("sContent", sContent);
        params.put("cParentId", cParentId);
        params.put("cRootId", cRootId);
        params.put("Token", token);

        OkHttpUtils.postWithToken()
                .url(SCConstants.ZONE_COMMENT)
                .params(params)
                .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(SCConstants.ZONE_COMMENT_RESULT,
                                postion, 0, response).sendToTarget();
                    }
                });
    }

    /**
     * @param handler
     */
    @Override
    public void messages(final Handler handler) {

        String url = "";

        try {
            String sssString = "?userId="
                    + MFSPHelper.getString(CommConstants.USERID)
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            url = SCConstants.ZONE_MESSAGE + sssString;

        } catch (Exception e) {
            e.printStackTrace();
            handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                    context.getString(R.string.get_message_fail)).sendToTarget();
        }

        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        try {
                            ArrayList<ZoneMessage> messages = ZoneConvert
                                    .getZoneMessageData(response);
                            if (messages == null) {
                                handler.obtainMessage(
                                        SCConstants.ZONE_ERROR_RESULT, context.getString(R.string.get_message_fail))
                                        .sendToTarget();
                            } else {
                                handler.obtainMessage(
                                        SCConstants.ZONE_LIST_RESULT, messages)
                                        .sendToTarget();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                                    context.getString(R.string.get_message_fail)).sendToTarget();
                        }
                    }
                });
    }

    @Override
    public void messagecount(final Handler handler) {

        String url = "";

        try {
            String sssString = "?userId="
                    + MFSPHelper.getString(CommConstants.USERID)
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            url = SCConstants.ZONE_MESSAGE_COUNT + sssString;

        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(
                                SCConstants.ZONE_MESSAGE_COUNT_RESULT, response)
                                .sendToTarget();
                    }
                });
    }

    @Override
    public void messagedel(final Handler handler) {

        String token = "";
        try {
            token = URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("userId", MFSPHelper.getString(CommConstants.USERID));
        params.put("Token", token);

        OkHttpUtils.postWithToken()
                .url(SCConstants.ZONE_MESSAGE_DELETE)
                .params(params)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(SCConstants.ZONE_MSG_DEL_RESULT,
                                response).sendToTarget();
                    }
                });
    }

    /**
     * 获取说说的详情
     *
     * @param cSayId
     * @param handler
     */
    @Override
    public void getSay(final String cSayId, final Handler handler) {

        String url = "";

        try {
            String sssString = "?userId="
                    + MFSPHelper.getString(CommConstants.USERID)
                    + "&cSayId="
                    + cSayId
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            url = SCConstants.GET_ZONE_SAY + sssString;

        } catch (Exception e) {
            e.printStackTrace();
            handler.obtainMessage(SCConstants.ZONE_ERROR_NO_SAY)
                    .sendToTarget();
        }

        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.obtainMessage(SCConstants.ZONE_ERROR_NO_SAY)
                                .sendToTarget();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        try {
                            ArrayList<Zone> zoneList = ZoneConvert.getZoneDetailData(
                                    response, context);
                            if (zoneList == null) {
                                handler.obtainMessage(
                                        SCConstants.ZONE_ERROR_NO_SAY)
                                        .sendToTarget();
                            } else {
                                handler.obtainMessage(
                                        SCConstants.ZONE_GET_RESULT, zoneList)
                                        .sendToTarget();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.obtainMessage(SCConstants.ZONE_ERROR_NO_SAY)
                                    .sendToTarget();
                        }
                    }
                });
    }

    @Override
    public void mysaycount(final Handler handler) {

        String url = "";

        try {
            String sssString = "?userId="
                    + MFSPHelper.getString(CommConstants.USERID)
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            url = SCConstants.GET_MINE_SAY_COUNT + sssString;

        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(
                                SCConstants.ZONE_MY_SAY_COUNT_RESULT, response)
                                .sendToTarget();
                    }
                });

    }

    @Override
    public void havenew(final String officeId, final String dCreateTime, final Handler handler) {

        String url = "";

        try {
            String sssString = "?userId="
                    + MFSPHelper.getString(CommConstants.USERID)
                    + "&dCreateTime="
                    + URLEncoder.encode(dCreateTime, "UTF-8")
                    + "&officeId="
                    + officeId
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            url = SCConstants.ZONE_NEW_MESSAGE + sssString;
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(
                                SCConstants.ZONE_NEW_SAY_COUNT_RESULT, response)
                                .sendToTarget();
                    }
                });
    }

    /**
     * @param userId
     * @param refreshTime 刷新时间
     * @param tCreateTime 第一条时间
     * @param bCreateTime 最后一条时间
     * @param isAfter     默认1 0为时间之前消息
     * @param type        默认 all 消息类型：工作 1\生活 0
     * @param isSecret    默认 0 1隐私消息
     * @param handler
     */
    @Override
    public void getPersonalZoneList(final String userId,
                                    final String refreshTime, final String tCreateTime,
                                    final String bCreateTime, final String isAfter, final String type,
                                    final String isSecret, final Handler handler) {

        String url = "";
        try {
            url = SCConstants.GET_MINE_SAY_LIST
                    + "?userId="
                    + userId
                    + "&Token="
                    + URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
            if (!"".equals(refreshTime)) {
                url += "&refreshTime="
                        + URLEncoder.encode(refreshTime, "UTF-8");
            }
            if (!"".equals(tCreateTime)) {
                url += "&tCreateTime="
                        + URLEncoder.encode(tCreateTime, "UTF-8");
            }
            if (!"".equals(bCreateTime)) {
                url += "&bCreateTime="
                        + URLEncoder.encode(bCreateTime, "UTF-8");
            }
            if (!"".equals(isAfter)) {
                url += "&isAfter=" + isAfter;
            }
            if (!"".equals(type)) {
                url += "&type=" + type;
            }
            if (!"".equals(isSecret)) {
                url += "&isSecret=" + isSecret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpUtils.getWithToken()
                .url(url + "&iPageRowCount=15")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            ArrayList<String> delList = ZoneConvert.getZoneListDataDel(
                                    result);
                            ArrayList<Zone> newList = ZoneConvert.getZoneListDataNew(
                                    result, context);
                            ArrayList<Zone> oldList = ZoneConvert.getZoneListDataOld(
                                    result, context);
                            ArrayList<Zone> topList = ZoneConvert.getZoneListDataTop(
                                    result, context);

                            Message message = new Message();
                            Bundle data = new Bundle();
                            data.putStringArrayList("delList", delList);
                            data.putSerializable("newList", newList);
                            data.putSerializable("oldList", oldList);
                            data.putSerializable("topList", topList);
                            message.setData(data);

                            if ("1".equals(isAfter)) {
                                message.what = SCConstants.ZONE_MORE_RESULT;
                                handler.sendMessage(message);
                            } else {
                                message.what = SCConstants.ZONE_LIST_RESULT;
                                handler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                                    context.getString(R.string.get_message_fail)).sendToTarget();
                        }
                    }
                });
    }

    @Override
    public void saydel(final String sayId, final int postion,
                       final Handler handler) {

        String token = "";
        try {
            token = URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("userId", MFSPHelper.getString(CommConstants.USERID));
        params.put("cSayId", sayId);
        params.put("Token", token);

        OkHttpUtils.postWithToken()
                .url(SCConstants.DELETE_MINE_SAY)
                .params(params)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(SCConstants.ZONE_SAY_DEL_RESULT,
                                postion, 0, response).sendToTarget();
                    }
                });
    }

    @Override
    public void commentdel(final String commentId, final int postion,
                           final int delCommentLine, final Handler handler) {

        String token = "";
        try {
            token = URLEncoder.encode(
                    MFSPHelper.getString(CommConstants.TOKEN), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("userId", MFSPHelper.getString(CommConstants.USERID));
        params.put("commentId", commentId);
        params.put("Token", token);

        OkHttpUtils.postWithToken()
                .url(SCConstants.DELETE_COMMENT)
                .params(params)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.obtainMessage(SCConstants.ZONE_ERROR_RESULT,
                                context.getString(R.string.del_fail)).sendToTarget();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        handler.obtainMessage(
                                SCConstants.ZONE_COMMENT_DEL_RESULT, postion,
                                delCommentLine, response).sendToTarget();
                    }
                });
    }

}
