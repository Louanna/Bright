package com.movit.platform.sc.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.sc.entities.Comment;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.entities.ZoneMessage;

public class ZoneConvert {

    public static ArrayList<String> getZoneListDataDel(String result) throws Exception {
        JSONObject jsonObject = new JSONObject(result);
        int code = jsonObject.getInt("code");
        ArrayList<String> delList = new ArrayList<String>();
        if (code == 0) {
            JSONObject list = jsonObject.getJSONObject("item");
            if (list.has("delSays")) {
                JSONArray delArray = list.getJSONArray("delSays");
                for (int i = 0; i < delArray.length(); i++) {
                    String cid = delArray.getString(i);
                    delList.add(cid);
                }
            }
            return delList;
        } else {
            return null;
        }
    }

    public static ArrayList<Zone> getZoneListDataNew(String result,
                                                     Context context) throws Exception {
        JSONObject jsonObject = new JSONObject(result);
        int code = jsonObject.getInt("code");
        ArrayList<Zone> zoneList = new ArrayList<Zone>();
        if (code == 0) {
            JSONObject list = jsonObject.getJSONObject("item");
            if (list.has("newSays")) {
                JSONArray newArray = list.getJSONArray("newSays");
                convertZone(newArray, zoneList, context);
            }
            return zoneList;
        } else {
            return null;
        }
    }

    public static ArrayList<Zone> getZoneListDataOld(String result,
                                                     Context context) throws Exception {
        JSONObject jsonObject = new JSONObject(result);
        int code = jsonObject.getInt("code");
        ArrayList<Zone> zoneList = new ArrayList<Zone>();
        if (code == 0) {
            JSONObject list = jsonObject.getJSONObject("item");
            if (list.has("oldSays")) {
                JSONArray newArray = list.getJSONArray("oldSays");
                convertZone(newArray, zoneList, context);
            }
            return zoneList;
        } else {
            return null;
        }
    }

    public static ArrayList<Zone> getZoneListDataTop(String result,
                                                     Context context) throws Exception {
        JSONObject jsonObject = new JSONObject(result);
        int code = jsonObject.getInt("code");
        ArrayList<Zone> zoneList = new ArrayList<Zone>();
        if (code == 0) {
            JSONObject list = jsonObject.getJSONObject("item");
            if (list.has("topSays")) {
                JSONArray newArray = list.getJSONArray("topSays");
                convertZone(newArray, zoneList, context);
            }
            return zoneList;
        } else {
            return null;
        }
    }

    private static void convertZone(JSONArray array, ArrayList<Zone> zoneList,
                                    Context context) throws Exception {
        UserDao dao = UserDao.getInstance(context);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            Zone zone = new Zone();
            if (object.has("cId")) {
                String cId = object.getString("cId");
                zone.setcId(cId);
            }
            if (object.has("sContent")) {
                String sContent = object.getString("sContent");
                zone.setContent(sContent);
            }
            if (object.has("cUserId")) {
                String cUserId = object.getString("cUserId");
                zone.setcUserId(cUserId);
                UserInfo user = dao.getUserInfoById(cUserId);
                if (user == null) {
                    LogUtils.v("result", "没有这个人" + cUserId);
                    continue;
                }
            }
            if (object.has("dCreateTime")) {
                String dCreateTime = object.getString("dCreateTime");
                zone.setdCreateTime(dCreateTime);
            }
            if (object.has("iIsSecret")) {
                int iIsSecret = object.getInt("iIsSecret");
                zone.setiIsSecret(iIsSecret);
            }
            if (object.has("iSayType")) {
                int iSayType = object.getInt("iSayType");
                zone.setiSayType(iSayType);
            }
            if (object.has("iDel")) {
                int iDel = object.getInt("iDel");
                zone.setiSayType(iDel);
            }

            if (object.has("iTop")) {
                String iTop = object.getString("iTop");
                zone.setiTop(iTop);
            }

            if (object.has("sImages")) {
                String sImages = object.getString("sImages");
                zone.setsImages(sImages);
                if (!"".equals(sImages)) {
                    JSONObject imageJsonObject = null;
                    try {
                        imageJsonObject = new JSONObject(sImages);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        continue;
                    }
                    if (imageJsonObject.has("image")
                            && !imageJsonObject.isNull("image")) {
                        JSONArray imageArray = imageJsonObject
                                .getJSONArray("image");
                        ArrayList<String> imageNames = new ArrayList<String>();
                        ArrayList<String> imageSizes = new ArrayList<String>();
                        for (int k = 0; k < imageArray.length(); k++) {
                            try {
                                JSONObject itemObject = (JSONObject) imageArray
                                        .get(k);
                                imageNames.add(itemObject.getString("name"));
                                imageSizes.add(itemObject.getString("size"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        zone.setImageNames(imageNames);
                        zone.setImageSizes(imageSizes);
                    }else if(imageJsonObject.has("video")
                            && !imageJsonObject.isNull("video")){

                        zone.setVideoPath(imageJsonObject.getJSONArray("video").getJSONObject(0).getString("name"));
                        zone.setVideoPicPath(imageJsonObject.getJSONArray("video").getJSONObject(0).getString("defaultImage"));
                    }
                }
            }

            if (object.has("niceList")) {
                JSONArray nicedataArray = object.getJSONArray("niceList");
                ArrayList<String> likers = new ArrayList<String>();
                for (int j = 0; j < nicedataArray.length(); j++) {
                    JSONObject niceJsonObject = nicedataArray.getJSONObject(j);
                    if (niceJsonObject.has("cUserId")) {
                        String cUserId = niceJsonObject.getString("cUserId");
                        UserInfo user = dao.getUserInfoById(cUserId);
                        if (user == null) {
                            LogUtils.v("result", "没有这个人" + cUserId);
                            continue;
                        }
                        likers.add(cUserId);
                    }
                }
                zone.setLikers(likers);
            }

            if (object.has("commentList")) {

                JSONArray commentdataArray = object.getJSONArray("commentList");
                ArrayList<Comment> comments = new ArrayList<Comment>();
                for (int j = 0; j < commentdataArray.length(); j++) {
                    JSONObject commentJsonObject = commentdataArray
                            .getJSONObject(j);
                    Comment comment = new Comment();
                    if (commentJsonObject.has("cUserId")) {
                        String cUserId = commentJsonObject.getString("cUserId");
                        UserInfo user = dao.getUserInfoById(cUserId);
                        if (user == null) {
                            LogUtils.v("result", "没有这个人" + cUserId);
                            continue;
                        }
                        comment.setUserId(cUserId);
                    }
                    if (commentJsonObject.has("cToUserId")) {
                        String cToUserId = commentJsonObject
                                .getString("cToUserId");
                        comment.setTouserId(cToUserId);
                    }
                    if (commentJsonObject.has("sContent")) {
                        String sContent = commentJsonObject
                                .getString("sContent");
                        comment.setContent(sContent);
                    }
                    if (commentJsonObject.has("cSayId")) {
                        String cSayId = commentJsonObject.getString("cSayId");
                        comment.setSayId(cSayId);
                    }
                    if (commentJsonObject.has("cParentId")) {
                        String cParentId = commentJsonObject
                                .getString("cParentId");
                        comment.setParnetId(cParentId);
                    }
                    if (commentJsonObject.has("cRootId")) {
                        String cRootId = commentJsonObject.getString("cRootId");
                        comment.setRootId(cRootId);
                    }
                    if (commentJsonObject.has("cId")) {
                        String cId = commentJsonObject.getString("cId");
                        comment.setcId(cId);
                    }
                    comments.add(comment);
                }
                zone.setComments(comments);

            }
            System.out.println(zone);
            zoneList.add(zone);
        }
    }

    public static ArrayList<Zone> getZoneDetailData(String result,
                                                    Context context) throws Exception {
        JSONObject jsonObject = new JSONObject(result);
        int code = jsonObject.getInt("code");
        ArrayList<Zone> zoneList = new ArrayList<Zone>();
        if (code == 0) {
            JSONObject list = jsonObject.getJSONObject("list");
            if (!list.has("header")) {
                return zoneList;
            }

            UserDao dao = UserDao.getInstance(context);
            JSONArray heardArray = list.getJSONArray("header");
            List<String> heardList = new ArrayList<String>();
            for (int i = 0; i < heardArray.length(); i++) {
                heardList.add(heardArray.getString(i));
            }
            JSONArray dataArray = list.getJSONArray("data");
            nextRow:
            for (int i = 0; i < dataArray.length(); i++) {
                Zone zone = new Zone();
                JSONArray data = (JSONArray) dataArray.get(i);
                for (int j = 0; j < data.length(); j++) {
                    if (heardList.contains("cId")) {
                        String cId = data.getString(heardList.indexOf("cId"));
                        zone.setcId(cId);
                    }
                    if (heardList.contains("sContent")) {
                        String sContent = data.getString(heardList
                                .indexOf("sContent"));
                        zone.setContent(sContent);
                    }
                    if (heardList.contains("cUserId")) {
                        String cUserId = data.getString(heardList
                                .indexOf("cUserId"));
                        zone.setcUserId(cUserId);
                        UserInfo user = dao.getUserInfoById(cUserId);
                        if (user == null) {
                            continue nextRow;
                        }
                    }
                    if (heardList.contains("dCreateTime")) {
                        String dCreateTime = data.getString(heardList
                                .indexOf("dCreateTime"));
                        zone.setdCreateTime(dCreateTime);
                    }
                    if (heardList.contains("iIsSecret")) {
                        int iIsSecret = data.getInt(heardList
                                .indexOf("iIsSecret"));
                        zone.setiIsSecret(iIsSecret);
                    }
                    if (heardList.contains("iSayType")) {
                        int iSayType = data.getInt(heardList
                                .indexOf("iSayType"));
                        zone.setiSayType(iSayType);
                    }
                    if (heardList.contains("iDel")) {
                        int iDel = data.getInt(heardList.indexOf("iDel"));
                        zone.setiSayType(iDel);
                    }

                    if (heardList.contains("iTop")) {
                        String iTop = data.getString(heardList.indexOf("iTop"));
                        zone.setiTop(iTop);
                    }

                    if (heardList.contains("sImages")) {
                        String sImages = data.getString(heardList
                                .indexOf("sImages"));
                        zone.setsImages(sImages);
                        if (!"".equals(sImages)) {
                            JSONObject imageJsonObject = new JSONObject(sImages);
                            if (imageJsonObject.has("image")
                                    && !imageJsonObject.isNull("image")) {
                                JSONArray imageArray = imageJsonObject
                                        .getJSONArray("image");
                                ArrayList<String> imageNames = new ArrayList<String>();
                                ArrayList<String> imageSizes = new ArrayList<String>();
                                for (int k = 0; k < imageArray.length(); k++) {
                                    try {
                                        JSONObject itemObject = (JSONObject) imageArray
                                                .get(k);
                                        imageNames.add(itemObject
                                                .getString("name"));
                                        imageSizes.add(itemObject
                                                .getString("size"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                zone.setImageNames(imageNames);
                                zone.setImageSizes(imageSizes);
                            }else if(imageJsonObject.has("video")
                                    && !imageJsonObject.isNull("video")){

                                zone.setVideoPath(imageJsonObject.getJSONArray("video").getJSONObject(0).getString("name"));
                                zone.setVideoPicPath(imageJsonObject.getJSONArray("video").getJSONObject(0).getString("defaultImage"));
                            }
                        }
                    }

                    if (heardList.contains("niceList")) {
                        String niceList = data.getString(heardList
                                .indexOf("niceList"));
                        if (!"".equals(niceList)) {
                            JSONObject niceJsonObject = new JSONObject(niceList);
                            if (niceJsonObject.has("header")) {
                                JSONArray niceHeardArray = niceJsonObject
                                        .getJSONArray("header");
                                List<String> niceHeardList = new ArrayList<String>();
                                for (int k = 0; k < niceHeardArray.length(); k++) {
                                    niceHeardList.add(niceHeardArray
                                            .getString(k));
                                }
                                ArrayList<String> likers = new ArrayList<String>();
                                JSONArray nicedataArray = niceJsonObject
                                        .getJSONArray("data");
                                for (int k = 0; k < nicedataArray.length(); k++) {
                                    JSONArray niceArray = nicedataArray
                                            .getJSONArray(k);
                                    for (int l = 0; l < niceArray.length(); l++) {
                                        if (niceHeardList.contains("cUserId")) {
                                            String cUserId = niceArray
                                                    .getString(niceHeardList
                                                            .indexOf("cUserId"));
                                            likers.add(cUserId);
                                            break;
                                        }
                                    }
                                }
                                zone.setLikers(likers);
                            }
                        }
                    }

                    if (heardList.contains("commentList")) {
                        String nicecommentList = data.getString(heardList
                                .indexOf("commentList"));
                        if (!"".equals(nicecommentList)) {
                            JSONObject commentJsonObject = new JSONObject(
                                    nicecommentList);
                            if (commentJsonObject.has("header")) {
                                JSONArray commentHeardArray = commentJsonObject
                                        .getJSONArray("header");
                                List<String> commentHeardList = new ArrayList<String>();
                                for (int k = 0; k < commentHeardArray.length(); k++) {
                                    commentHeardList.add(commentHeardArray
                                            .getString(k));
                                }
                                ArrayList<Comment> comments = new ArrayList<Comment>();
                                JSONArray commentdataArray = commentJsonObject
                                        .getJSONArray("data");
                                for (int k = 0; k < commentdataArray.length(); k++) {
                                    JSONArray commentArray = commentdataArray
                                            .getJSONArray(k);
                                    for (int l = 0; l < commentArray.length(); l++) {
                                        Comment comment = new Comment();
                                        if (commentHeardList
                                                .contains("cUserId")) {
                                            String cUserId = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("cUserId"));
                                            comment.setUserId(cUserId);
                                        }
                                        if (commentHeardList
                                                .contains("cToUserId")) {
                                            String cToUserId = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("cToUserId"));
                                            comment.setTouserId(cToUserId);
                                        }
                                        if (commentHeardList
                                                .contains("sContent")) {
                                            String sContent = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("sContent"));
                                            comment.setContent(sContent);
                                        }
                                        if (commentHeardList.contains("cSayId")) {
                                            String cSayId = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("cSayId"));
                                            comment.setSayId(cSayId);
                                        }
                                        if (commentHeardList
                                                .contains("cParentId")) {
                                            String cParentId = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("cParentId"));
                                            comment.setParnetId(cParentId);
                                        }
                                        if (commentHeardList
                                                .contains("cRootId")) {
                                            String cRootId = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("cRootId"));
                                            comment.setRootId(cRootId);
                                        }
                                        if (commentHeardList.contains("cId")) {
                                            String cId = commentArray
                                                    .getString(commentHeardList
                                                            .indexOf("cId"));
                                            comment.setcId(cId);
                                        }
                                        comments.add(comment);
                                        break;
                                    }
                                }
                                zone.setComments(comments);
                            }
                        }
                    }
                }
                zoneList.add(zone);
            }

            return zoneList;
        } else {
            return null;
        }
    }

    public static ArrayList<ZoneMessage> getZoneMessageData(String json)
            throws Exception {

        JSONObject jsonObject = new JSONObject(json);
        int code = jsonObject.getInt("code");
        ArrayList<ZoneMessage> zoneList = new ArrayList<ZoneMessage>();
        if (code == 0) {
            JSONObject list = jsonObject.getJSONObject("list");
            if (!list.has("header")) {
                return zoneList;
            }
            JSONArray heardArray = list.getJSONArray("header");
            List<String> heardList = new ArrayList<String>();
            for (int i = 0; i < heardArray.length(); i++) {
                heardList.add(heardArray.getString(i));
            }
            JSONArray dataArray = list.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                ZoneMessage message = new ZoneMessage();
                JSONArray data = (JSONArray) dataArray.get(i);
                for (int j = 0; j < data.length(); j++) {
                    if (heardList.contains("cId")) {
                        String cId = data.getString(heardList.indexOf("cId"));
                        message.setcId(cId);
                    }
                    if (heardList.contains("cUserId")) {
                        String cUserId = data.getString(heardList
                                .indexOf("cUserId"));
                        message.setcUserId(cUserId);
                    }
                    if (heardList.contains("cToUserId")) {
                        String cToUserId = data.getString(heardList
                                .indexOf("cToUserId"));
                        message.setcToUserId(cToUserId);
                    }
                    if (heardList.contains("cSayId")) {
                        String cSayId = data.getString(heardList
                                .indexOf("cSayId"));
                        message.setcSayId(cSayId);
                    }
                    if (heardList.contains("dCreateTime")) {
                        String dCreateTime = data.getString(heardList
                                .indexOf("dCreateTime"));
                        message.setdCreateTime(dCreateTime);
                    }
                    if (heardList.contains("iType")) {
                        String iType = data.getString(heardList
                                .indexOf("iType"));
                        message.setiType(iType);
                    }
                    if (heardList.contains("iHasRead")) {
                        int iHasRead = data.getInt(heardList
                                .indexOf("iHasRead"));
                        message.setiHasRead(iHasRead);
                    }
                    if (heardList.contains("sMessage")) {
                        String sMessage = data.getString(heardList
                                .indexOf("sMessage"));
                        message.setsMessage(sMessage);
                    }
                    break;
                }
                zoneList.add(message);
            }
            return zoneList;
        } else {
            return null;
        }
    }

}
