package com.movit.platform.cloud.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.movit.platform.cloud.R;
import com.movit.platform.cloud.adapter.ShareFileListAdapter;
import com.movit.platform.cloud.application.CloudApplication;
import com.movit.platform.cloud.model.Share;
import com.movit.platform.cloud.net.AsyncNetHandler;
import com.movit.platform.cloud.net.INetHandler;
import com.movit.platform.cloud.view.DonutProgress;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.callback.CloudListCallback;
import com.movit.platform.framework.helper.MFSPHelper;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by air on 16/3/23.
 * <p/>
 * personal share area
 */
@SuppressWarnings("deprecation")
public class ShareAreaActivity extends BaseActivity {

    private String TAG = ShareAreaActivity.class.getSimpleName();

    private RelativeLayout rlTop;
    private ImageView topRight;
    private ListView lvFile;
    private EditText searchText;
    private ImageView searchClear;
    private PopupWindow popupWindow;

    private ShareFileListAdapter fileListAdapter;
    private ArrayList<Share> shares = new ArrayList<>();
    private ArrayList<Share> searchShares = new ArrayList<>();

    private DownloadManager downloadManager;
    private InputMethodManager inputManger;

    INetHandler netHandler = null;

    private CompleteReceiver completeReceiver;
    private DownloadChangeObserver downloadObserver;

    private HashMap<Object,String> downloadFileNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_area);

        inputManger = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadObserver = new DownloadChangeObserver();

        netHandler = AsyncNetHandler.getInstance(this);

        findViews();

        initData();

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void findViews() {
        TextView tvTitle = (TextView) findViewById(R.id.common_top_radio1);
        tvTitle.setText(R.string.sky_drive_text3);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight = (ImageView) findViewById(R.id.common_top_right);
        topRight.setImageResource(R.drawable.ico_shuxing);

        lvFile = (ListView) findViewById(R.id.lv_file);
        rlTop = (RelativeLayout) findViewById(R.id.rl_top);
        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            rlTop.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        searchText = (EditText) findViewById(R.id.search_key);
        searchClear = (ImageView) findViewById(R.id.search_clear);

        ImageView ivNewFolder = (ImageView) findViewById(R.id.iv_new_folder);
        ImageButton ibUploadFile = (ImageButton) findViewById(R.id.ib_upload_file);
        ivNewFolder.setVisibility(View.GONE);
        ibUploadFile.setVisibility(View.GONE);
    }

    private void initData() {

        topRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop();
            }
        });

        fileListAdapter = new ShareFileListAdapter(context, new ArrayList<Share>());
        lvFile.setAdapter(fileListAdapter);

        fileListAdapter.setOpenFileListener(new ShareFileListAdapter.OpenFileListener() {
            @Override
            public void openFile(String path, String type) {
                //Toast.makeText(PublicAreaActivity.this, uuid+" : "+type, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(path)), type);
                startActivity(intent);
            }
        });

        fileListAdapter.setDownFileListener(new ShareFileListAdapter.DownFileListener() {
            @Override
            public void downLoad(String listFileName,String docId, String fileName, String filePath, double totalSize) {
                downDocument(listFileName,docId, fileName, filePath, totalSize);
            }
        });

        searchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputManger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
                fileListAdapter.replaceAll(shares);
            }
        });
        searchText.setOnKeyListener(new View.OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = searchText.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
                        searchClear.setVisibility(View.VISIBLE);
                        searchFile(content);
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                    }
                    inputManger.hideSoftInputFromWindow(
                            searchText.getWindowToken(), 0);
                }
                return false;
            }
        });

        getShareFolder();
    }

    /**
     * 搜索文档
     */
    private void searchFile(String name) {
        progressDialogUtil.showLoadingDialog(context, "", true);
        netHandler.getShareByName(name, new CloudListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(com.alibaba.fastjson.JSONArray response) throws JSONException {
                searchShares.clear();
                for (int i = 0; i < response.size(); i++) {
                    Share shareFile = JSON.parseObject(response.getJSONObject(i).toString(), Share.class);
                    searchShares.add(shareFile);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileListAdapter.replaceAll(searchShares);
                    }
                });
                progressDialogUtil.dismiss();
            }
        });
    }

    /**
     * 下载文档
     */
    private void downDocument(String listFileName,String docId, String fileName, String filePath, final double totalSize) {

        String destFileDir = CommConstants.SD_DOCUMENT + filePath;
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }


        long downloadId = netHandler.downDocument(docId, destFileDir.replace(CommConstants.SD_CARD, ""), fileName);

        CloudApplication.downloadIdMap.put(downloadId, docId);
        downloadFileNameMap.put(downloadId,listFileName);

//        progressDialogUtil.showLoadingDialog(context,"",true);
//        netHandler.downDocument(docId, new FileCallBack(CommConstants.SD_DOCUMENT+filePath,fileName) {
//            @Override
//            public void inProgress(float progress, long total) {
//                int pro = Math.abs((int)((progress/totalSize)*100));
//                //Log.d(TAG, "inProgress : " + pro);
//                //button.setProgress(pro);
//            }
//
//            @Override
//            public void onError(Call call, Exception e) {
//                Log.d(TAG, "onError : " + e.getMessage());
//                progressDialogUtil.dismiss();
//            }
//
//            @Override
//            public void onResponse(File response) {
//                Log.d(TAG, "onResponse :" + response.getAbsolutePath());
//                fileListAdapter.notifyDataSetChanged();
//                progressDialogUtil.dismiss();
//            }
//        });

    }


    /**
     * 获取共享目录
     */
    private void getShareFolder() {
        progressDialogUtil.showLoadingDialog(context, "", true);
        netHandler.getShareFolder(new CloudListCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(com.alibaba.fastjson.JSONArray response) throws JSONException {
                shares.clear();
                if(null != response){
                    for (int i = 0; i < response.size(); i++) {
                        Share shareFile = JSON.parseObject(response.getJSONObject(i).toString(), Share.class);
                        shares.add(shareFile);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileListAdapter.add(shares);
                        }
                    });
                }
                progressDialogUtil.dismiss();
            }
        });
    }

    //按照部门或生日排序
    @SuppressLint("InflateParams")
    private void showPop() {
        // 加载popupWindow的布局文件
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.layout_file_attribute, null);

        TextView tvName = (TextView) contentView.findViewById(R.id.tv_name);
        tvName.setText(String.format(getString(R.string.sky_drive_attribute1), getString(R.string.sky_drive_text3) + getString(R.string.sky_drive_folder)));
        TextView tvSize = (TextView) contentView.findViewById(R.id.tv_size);
        tvSize.setVisibility(View.GONE);
        TextView tvFolderCount = (TextView) contentView.findViewById(R.id.tv_folder_count);
        tvFolderCount.setText(String.format(getString(R.string.sky_drive_attribute3), 0));
        TextView tvDocumentCount = (TextView) contentView.findViewById(R.id.tv_document_count);
        tvDocumentCount.setText(String.format(getString(R.string.sky_drive_attribute4), fileListAdapter.getCount()));

        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);
        // 为弹出框设定自定义的布局
        popupWindow.setContentView(contentView);
        // 设置点击其他地方 popupWindow消失
        popupWindow.setOutsideTouchable(true);
    /*
     * 必须设置背景 响应返回键必须的语句。设置 BackgroundDrawable 并不会改变你在配置文件中设置的背景颜色或图像 ，未知原因
     */
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAsDropDown(rlTop);

        // 点击自身popupWindow消失
        contentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String uuid = CloudApplication.downloadIdMap.get(msg.obj);
                    Log.d(TAG, "updateView --> uuid: " + uuid);
                    if (!TextUtils.isEmpty(uuid)) {
                        Share button = getPositionInAdapter(uuid);
                        if (null != button) {
                            int progress = msg.arg1;
                            double totalSize = button.getSize();
                            int pro = Math.abs((int) ((progress / totalSize) * 100));
                            Log.d(TAG, "updateView --> pro: " + pro);
                            publishProgress(button.getPositionInAdapter(), pro);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Share getPositionInAdapter(String uuid) {
        ArrayList<Share> documents = fileListAdapter.getAll();
        for (int i = 0; i < documents.size(); i++) {
            Share document = documents.get(i);
            if (uuid.equals(document.getShareUuid())) {
                fileListAdapter.getAll().get(i).setDownload(2);
                fileListAdapter.getAll().get(i).setPositionInAdapter(i);
                fileListAdapter.notifyDataSetChanged();
                return document;
            }
        }
        return null;
    }


    public void publishProgress(int positionInAdapter, int progress) {
        fileListAdapter.getAll().get(positionInAdapter).setProgress(progress);
        if (positionInAdapter >= lvFile.getFirstVisiblePosition() &&
                positionInAdapter <= lvFile.getLastVisiblePosition()) {
            int positionInListView = positionInAdapter - lvFile.getFirstVisiblePosition();
            DonutProgress item = (DonutProgress) lvFile.getChildAt(positionInListView)
                    .findViewById(R.id.cloud_item_numberCircleProgressBar);
            item.setProgress(progress);
            if (progress >= 100) {
                fileListAdapter.getAll().get(positionInAdapter).setDownload(1);
                fileListAdapter.notifyDataSetChanged();
            }
        }
    }

    //更新下载进度
    class DownloadChangeObserver extends ContentObserver {


        private DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

        private void updateView() {

//            DownloadManager.Query query = new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING);
//            Cursor c = null;
//            try {
//                c = downloadManager.query(query);
//                while (c != null && c.moveToNext()){
//                    long downloadId = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
//                    //Log.d(TAG, "updateView --> downloadId : "+downloadId);
//                    String uuid = CloudApplication.downloadIdMap.get(downloadId);
//                    int[] bytesAndStatus = getBytesAndStatus(downloadId);
//                    handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[2],
//                            uuid));
//                }
//            } finally {
//                if (c != null) {
//                    c.close();
//                }
//            }

            for (Object key : CloudApplication.downloadIdMap.keySet()) {
                int[] bytesAndStatus = getBytesAndStatus((long) key);
                handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[2],
                        key));

            }

            if (CloudApplication.downloadIdMap.isEmpty()) {
                for (int i = 0; i < fileListAdapter.getAll().size(); i++) {
                    if (fileListAdapter.getAll().get(i).isDownload() == 2) {
                        fileListAdapter.getAll().get(i).setDownload(1);
                    }
                }
                fileListAdapter.notifyDataSetChanged();
            }

        }


        private int[] getBytesAndStatus(long downloadId) {
            int[] bytesAndStatus = new int[]{-1, -1, 0};
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
            Cursor c = null;
            try {
                c = downloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return bytesAndStatus;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** observer download change **/
        getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true,
                downloadObserver);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get complete download id
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d(TAG, "onReceive: " + completeDownloadId);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(completeDownloadId);
            Cursor c = null;
            try {
                c = downloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    int downloadStatus = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int fileNameIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    String fileName = c.getString(fileNameIdx);
                    // TODO Do something with the file.
                    Log.d(TAG, fileName);
                    Log.d(TAG, "onReceive: --> downloadStatus: " + downloadStatus);

                    if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                        //纪录下载文档日志
                        String uuid = CloudApplication.downloadIdMap.get(completeDownloadId);
                        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
                        String listFileName = downloadFileNameMap.get(completeDownloadId);
                        netHandler.saveDownloadFile(uuid,listFileName,userInfo.getId());
                        // to do here
                        refreshAdapter(fileName);
                        CloudApplication.downloadIdMap.remove(completeDownloadId);
                        downloadNext();
                    } else if (downloadStatus == DownloadManager.STATUS_FAILED) {
                        // to do here
                        refreshAdapter(fileName);

                        Toast.makeText(context, R.string.sky_drive_download_failed, Toast.LENGTH_LONG).show();
                        //down failed
                        File file = new File(fileName);
                        if (file.exists()) {
                            file.delete();
                        }

                        downloadNext();
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    private void downloadNext() {
        if (!CloudApplication.waitList.isEmpty()) {
            HashMap<String, String> map = CloudApplication.waitList.get(0);
            downDocument(map.get("listFileName"),map.get("docId"), map.get("fileName"), map.get("filePath"), 0);
            CloudApplication.waitList.remove(0);
        }
    }

    private void refreshAdapter(String fileName) {
        ArrayList<Share> documents = fileListAdapter.getAll();
        for (int i = 0; i < documents.size(); i++) {
            Share document = documents.get(i);
            String path = CommConstants.SD_DOCUMENT + "okmshare/" + document.getShareUuid();
            Log.d(TAG, "refreshAdapter -->  path: " + path);
            if (fileName.contains(path)) {
                fileListAdapter.getAll().get(i).setDownload(1);
                break;
            }
        }
        fileListAdapter.notifyDataSetChanged();
    }

}
