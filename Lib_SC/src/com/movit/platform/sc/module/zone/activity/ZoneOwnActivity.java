package com.movit.platform.sc.module.zone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.sc.R;
import com.movit.platform.sc.base.ZoneBaseActivity;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.entities.Zone;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

public class ZoneOwnActivity extends ZoneBaseActivity implements OnScrollListener {
    String userid;
    boolean isDelOK = false;
    ArrayList<Zone> currentTop = new ArrayList<Zone>();
    protected DialogUtils progressDialogUtil;

    @Override
    protected void initTopView() {
        Intent intent = getIntent();
        userid = intent.getStringExtra("userId");
        if (MFSPHelper.getString(CommConstants.USERID).equals(userid)) {
            title.setText(getString(R.string.my_home_page));
        } else {
            title.setText(getString(R.string.home_page));
        }
        topRight.setVisibility(View.GONE);
    }

    @Override
    protected void initDatas() {

        progressDialogUtil = DialogUtils.getInstants();

        DialogUtils.getInstants().showLoadingDialog(ZoneOwnActivity.this, getString(R.string.xlistview_header_hint_loading),
                false);
        zoneList.clear();
        zoneManager.getPersonalZoneList(userid,
                MFSPHelper.getString("refreshTime"), "", "", "", "", "", handler);
    }

    @Override
    protected void setAdapter() {
//        adapter = new ZoneAdapter(ZoneOwnActivity.this, zoneList,
//                handler, ZoneAdapter.TYPE_OTHER, userid,
//                DialogUtils.getInstants(), zoneManager);
//        zoneListView.setAdapter(adapter);
//        if (zoneList.isEmpty()) {
//            zoneListView.setPullLoadEnable(false);
//        } else {
//            zoneListView.setPullLoadEnable(true);
//        }
//        zoneListView.setPullRefreshEnable(true);
//        zoneListView.setXListViewListener(this);
//        zoneListView.setOnScrollListener(this);
//        zoneListView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//            }
//        });
    }

    public void refreshData() {
        // if (adapter != null) {
        // progressDialogUtil.showLoadingDialog(ZoneOwnActivity.this,
        // "正在加载...", false);
        // handler.postDelayed(new Runnable() {
        //
        // @Override
        // public void run() {
        // if (zoneListView.ismPullLoading()
        // || zoneListView.ismPullRefreshing()) {
        // handler.postDelayed(this, 500);
        // } else {
        // zoneList.clear();
        // ZoneManager zoneManager = new ZoneManager(
        // ZoneOwnActivity.this);
        // zoneManager.getPersonalZoneList(userid,
        // MFSPHelper.getString("refreshTime"), "", "", "",
        // "", "", handler);
        // }
        // }
        // }, 500);
        // }
    }

    @Override
    public void onRefresh() {
        if (zoneList.isEmpty()) {
            zoneManager.getPersonalZoneList(userid,
                    MFSPHelper.getString("refreshTime"), "", "", "", "", "",
                    handler);

        } else {
            String tCreateTime = "";
            for (int i = 0; i < zoneList.size(); i++) {
                if (zoneList.get(i).getiTop() != null
                        && zoneList.get(i).getiTop().equals("0")) {
                    tCreateTime = zoneList.get(i).getdCreateTime();
                    break;
                }
            }
            zoneManager.getPersonalZoneList(userid,
                    MFSPHelper.getString("refreshTime"), tCreateTime,
                    zoneList.get(zoneList.size() - 1).getdCreateTime(), "0",
                    "", "", handler);

        }
    }

    @Override
    public void onLoadMore() {
        String tCreateTime = "";
        for (int i = 0; i < zoneList.size(); i++) {
            if (zoneList.get(i).getiTop() != null
                    && zoneList.get(i).getiTop().equals("0")) {
                tCreateTime = zoneList.get(i).getdCreateTime();
                break;
            }
        }
        if (!zoneList.isEmpty()) {
            zoneManager.getPersonalZoneList(userid,
                    MFSPHelper.getString("refreshTime"), tCreateTime,
                    zoneList.get(zoneList.size() - 1).getdCreateTime(), "1",
                    "", "", handler);
        } else {
            zoneListView.stopLoadMore();
        }

    }

    @Override
    protected void dealHandlers(Message msg) {
        Bundle data = msg.getData();
        ArrayList<String> delList = data.getStringArrayList("delList");
        ArrayList<Zone> newList = (ArrayList<Zone>) data
                .getSerializable("newList");
        ArrayList<Zone> oldList = (ArrayList<Zone>) data
                .getSerializable("oldList");
        ArrayList<Zone> topList = (ArrayList<Zone>) data
                .getSerializable("topList");

        switch (msg.what) {
            case SCConstants.ZONE_LIST_RESULT:
                DialogUtils.getInstants().dismiss();
                ArrayList<Zone> temp = new ArrayList<Zone>();
                temp.addAll(zoneList);
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).getiTop() != null
                            && temp.get(i).getiTop().equals("1")) {
                        zoneList.remove(temp.get(i));
                    }
                }
                temp.clear();
                temp.addAll(zoneList);
                for (int i = 0; i < currentTop.size(); i++) {
                    long currTime = DateUtils.str2Date(
                            currentTop.get(i).getdCreateTime()).getTime();
                    for (int j = 0; j < temp.size(); j++) {
                        if (j != 0) {
                            long time1 = DateUtils.str2Date(
                                    temp.get(j).getdCreateTime()).getTime();
                            long time2 = DateUtils.str2Date(
                                    temp.get(j - 1).getdCreateTime()).getTime();
                            if (currTime < time2 && currTime > time1) {
                                Log.v("top", "插入" + j);
                                currentTop.get(i).setiTop("0");
                                zoneList.add(j, currentTop.get(i));
                            }
                        }

                    }
                }
                temp.clear();
                temp.addAll(zoneList);
                // 去除当前删除的，然后去除当前置顶的
                for (int i = 0; i < temp.size(); i++) {
                    if (delList.contains(temp.get(i).getcId())) {
                        zoneList.remove(temp.get(i));
                    }
                    if (topList.contains(temp.get(i))) {
                        zoneList.remove(temp.get(i));
                    }
                }
                temp = null;
                for (int j = 0; j < zoneList.size(); j++) {
                    for (int j2 = 0; j2 < oldList.size(); j2++) {
                        if (zoneList.get(j).equals(oldList.get(j2))) {
                            zoneList.remove(j);
                            zoneList.add(j, oldList.get(j2));
                        }
                    }
                }
                zoneList.addAll(0, newList);
                zoneList.addAll(0, topList);
                currentTop.clear();
                currentTop.addAll(topList);
                zoneListView.stopRefresh();
                zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    setAdapter();
                }

                break;
            case SCConstants.ZONE_MORE_RESULT:
                ArrayList<Zone> temp2 = new ArrayList<Zone>();
                temp2.addAll(zoneList);
                for (int i = 0; i < temp2.size(); i++) {
                    if (temp2.get(i).getiTop() != null
                            && temp2.get(i).getiTop().equals("1")) {
                        zoneList.remove(temp2.get(i));
                    }
                }
                temp2.clear();
                temp2.addAll(zoneList);
                for (int i = 0; i < currentTop.size(); i++) {
                    long currTime = DateUtils.str2Date(
                            currentTop.get(i).getdCreateTime()).getTime();
                    for (int j = 0; j < temp2.size(); j++) {
                        if (j != 0) {
                            long time1 = DateUtils.str2Date(
                                    temp2.get(j).getdCreateTime()).getTime();
                            long time2 = DateUtils.str2Date(
                                    temp2.get(j - 1).getdCreateTime()).getTime();
                            if (currTime < time2 && currTime > time1) {
                                currentTop.get(i).setiTop("0");
                                zoneList.add(j, currentTop.get(i));
                            }
                        }

                    }
                }
                temp2.clear();
                temp2.addAll(zoneList);
                // 去除当前删除的，然后去除当前置顶的
                for (int i = 0; i < temp2.size(); i++) {
                    if (delList.contains(temp2.get(i).getcId())) {
                        zoneList.remove(temp2.get(i));
                    }
                    if (topList.contains(temp2.get(i))) {
                        zoneList.remove(temp2.get(i));
                    }
                }
                temp2 = null;
                for (int j = 0; j < zoneList.size(); j++) {
                    for (int j2 = 0; j2 < oldList.size(); j2++) {
                        if (zoneList.get(j).equals(oldList.get(j2))) {
                            zoneList.remove(j);
                            zoneList.add(j, oldList.get(j2));
                        }
                    }
                }
                zoneList.addAll(newList);
                zoneList.addAll(0, topList);
                currentTop.clear();
                currentTop.addAll(topList);
                zoneListView.stopLoadMore();
                adapter.notifyDataSetChanged();
                break;
            case SCConstants.ZONE_SAY_DEL_RESULT:
                DialogUtils.getInstants().dismiss();
                try {
                    String result = (String) msg.obj;
                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (0 == code) {
                        int postion = msg.arg1;
                        zoneList.remove(postion);
                        adapter.notifyDataSetChanged();
                        isDelOK = true;
                    } else {
                        ToastUtils.showToast(ZoneOwnActivity.this, getString(R.string.del_fail));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showToast(ZoneOwnActivity.this, getString(R.string.del_fail));
                }
                break;
            default:
                break;
        }
    }

    private String takePicturePath = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCConstants.ZONE_CLICK_AVATAR) {
            if (resultCode == SCConstants.ZONE_SAY_DEL_RESULT) {// 删除成功
                onRefresh();
                isDelOK = true;
            } else {
                // 取得裁剪后的图片
                if (data != null) {
                    takePicturePath = data.getStringExtra("takePicturePath");
                    if (StringUtils.notEmpty(takePicturePath)) {
                        progressDialogUtil.showDownLoadingDialog(this,
                                getString(R.string.uploading), false);
                        // 上传图片
                        zoneManager.uploadFile(takePicturePath, mHandler);
                    }
                }
            }
        } else {
            String currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    // 如果是直接从相册获取
                    case 1:
                        // 从相册中直接获取文件的真是路径，然后上传
                        String picPath = PicUtils
                                .getPicturePath(data, this);
                        startActivityForResult(new Intent(this,
                                ClipImageActivity.class).putExtra(
                                "takePicturePath", picPath), 3);
                        break;
                    // 如果是调用相机拍照时
                    case 2:
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            String path = CommConstants.SD_CARD_IMPICTURES + currentTime + ".jpg";
                            startActivityForResult(new Intent(this,
                                    ClipImageActivity.class).putExtra(
                                    "takePicturePath", path), 3);
                            PicUtils.scanImages(this, path);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                if (StringUtils.notEmpty(currentTime)) {
                    String path = CommConstants.SD_CARD_IMPICTURES + currentTime
                            + ".jpg";
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    String newPathString = PicUtils.getTempPicPath(path);
                    File f = new File(newPathString);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isDelOK) {
            setResult(SCConstants.ZONE_SAY_DEL_RESULT);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex + 2; // 加上底部的loadMoreView项 和顶部刷新的
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex) {
            // 如果是自动加载,可以在这里放置异步加载数据的代码
            zoneListView.startLoadMore();
        }
    }

    int visibleLastIndex;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    // 上传成功，发送消息
                    String message = (String) msg.obj;
                    try {
                        //去更新同事圈背景图名称
                        toUploadZonePic(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialogUtil.dismiss();
                    } finally {
                        // 删除拍照旋转的图片；
                        File f = new File(takePicturePath);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    break;
                case 4:
                    progressDialogUtil.dismiss();
                    String uname = (String) msg.obj;
                    MFSPHelper.setString(SCConstants.ZONE_PIC, uname);
                    //更新UI
                    adapter.stopMediaPlay();
                    adapter.notifyDataSetChanged();
                    break;
                case 5:
                    progressDialogUtil.dismiss();
                    ToastUtils.showToast(ZoneOwnActivity.this, getString(R.string.bg_sync_fail));
                    break;
            }
        }
    };

    private void toUploadZonePic(final String picName) {

        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("id", MFSPHelper.getString(CommConstants.USERID));
        paramsMap.put("backgroundImage", picName);
        String json = Obj2JsonUtils.map2json(paramsMap);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(CommConstants.URL_UPDATE_USER_INFO)
                .content(json)
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(5);
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean ok = jsonObject.getBoolean("ok");
                        if (ok) {
                            mHandler.obtainMessage(4, picName).sendToTarget();
                        } else {
                            mHandler.sendEmptyMessage(5);
                        }
                    }
                });
    }
}
