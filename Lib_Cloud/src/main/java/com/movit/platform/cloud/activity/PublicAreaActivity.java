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
import android.widget.AdapterView;
import android.widget.Button;
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
import com.movit.platform.cloud.adapter.FileListAdapter;
import com.movit.platform.cloud.application.CloudApplication;
import com.movit.platform.cloud.model.Document;
import com.movit.platform.cloud.model.OkmFile;
import com.movit.platform.cloud.net.AsyncNetHandler;
import com.movit.platform.cloud.net.INetHandler;
import com.movit.platform.cloud.view.DonutProgress;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.ToastUtils;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by air on 16/3/16.
 *
 * 公共区域
 */
@SuppressLint("InflateParams")
@SuppressWarnings({"unchecked", "deprecation"})
public class PublicAreaActivity extends BaseActivity {

    private String TAG = PublicAreaActivity.class.getSimpleName();

    public final int NEW_FOLDER_CODE = 5222;
    public final int REQUEST_CODE_FILE = 5223;

    private RelativeLayout rlTop;
    private ImageView topRight;
    private ListView lvFile;

    private EditText searchText;
    private ImageView searchClear;

    private ImageView ivFileGuid;
    private ImageView ivNewFolder;
    private ImageButton ibUploadFile;

    private PopupWindow popupWindow;

    private FileListAdapter fileListAdapter;
    private ArrayList<Document> currentFolders = new ArrayList<>();
    private ArrayList<Document> searchFolders = new ArrayList<>();

    private InputMethodManager inputManger;

    INetHandler netHandler = null;

    String rootId;
    String superId;
    int folderCount;
    int documentCount;
    HashMap<String, Document> documents = new HashMap<>();
    private HashMap<Object,String> downloadFileNameMap = new HashMap<>();

    private String title;
    private String fileName;
    private String rootPath;
    private String personalPath;
    private String searchRootPath;

    private DownloadManager downloadManager;
    private CompleteReceiver completeReceiver;
    private DownloadChangeObserver downloadObserver;

    private ArrayList<UserInfo> atAllUserInfos = new ArrayList<>();
    private ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<>();
    private String shareUuId;


    private String rootFldId;
    private String isChat;

    private Document currFolder;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_area);

        inputManger = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        downloadObserver = new DownloadChangeObserver();

        netHandler = AsyncNetHandler.getInstance(this);

        title = getIntent().getStringExtra("title");
        fileName = title + getString(R.string.sky_drive_folder);

        rootFldId = getIntent().getStringExtra("rootFldId");

        isChat = getIntent().getStringExtra("chat");

        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        userId = userInfo.getId();

        findViews();

        initData();

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void findViews() {
        TextView tvTitle = (TextView) findViewById(R.id.common_top_radio1);
        tvTitle.setText(title);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackListener();
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

        ivFileGuid = (ImageView) findViewById(R.id.iv_file_guid);

        ivNewFolder = (ImageView) findViewById(R.id.iv_new_folder);

        ibUploadFile = (ImageButton) findViewById(R.id.ib_upload_file);
    }

    private void initData() {

        //=================Softlayer环境先隐藏=======================//
//        ivNewFolder.setVisibility(View.GONE);
//        ibUploadFile.setVisibility(View.GONE);
        //========================================//

        if (title.equals(getString(R.string.sky_drive_text1))) {
            fileListAdapter = new FileListAdapter(context, new ArrayList<Document>(), false);
            getRootFolder();
            ivNewFolder.setVisibility(View.GONE);
            ibUploadFile.setVisibility(View.GONE);
        } else if (title.equals(getString(R.string.sky_drive_text2))) {
            fileListAdapter = new FileListAdapter(context, new ArrayList<Document>(), true);
            fileListAdapter.setChat(isChat);
            getPersonalFolder();

            boolean isFirst = MFSPHelper.getBoolean(
                    CommConstants.FILE_CLOUD, true);
            if (isFirst) {
                ivFileGuid.setVisibility(View.VISIBLE);
            }
            ivFileGuid.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ivFileGuid.setVisibility(View.GONE);
                    MFSPHelper.setBoolean(CommConstants.FILE_CLOUD, false);
                }
            });
        }
        lvFile.setAdapter(fileListAdapter);

        topRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop();
            }
        });

        fileListAdapter.setOpenFileListener(new FileListAdapter.OpenFileListener() {
            @Override
            public void openFile(String path, String type) {
                //Toast.makeText(PublicAreaActivity.this, uuid+" : "+type, Toast.LENGTH_SHORT).show();
                //Uri uri = Uri.parse("file://"+file.getAbsolutePath());
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(path)), type);
                //intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
                startActivity(intent);
            }
        });

        fileListAdapter.setDownFileListener(new FileListAdapter.DownFileListener() {
            @Override
            public void downLoad(String listFileName,String docId, String fileName, String filePath,double totalSize) {
//                DownloadManager.Query query = new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING);
//                Cursor c = null;
//                try {
//                    c = downloadManager.query(query);
//                    if(c.getCount() >= 2){
//                        ToastUtils.showToast(context, "最多同时下载2个文件！");
//                    }else {
//                        downDocument(docId, fileName, filePath, totalSize);
//                    }
//                } finally {
//                    if (c != null) {
//                        c.close();
//                    }
//                }

//                if(EOPApplication.downloadIdMap.size() >= 2){
//                    HashMap<String,String> map = new HashMap<>();
//                    map.put("docId",docId);
//                    map.put("fileName",fileName);
//                    map.put("filePath",filePath);
//                    EOPApplication.waitList.add(map);
//                }else {
                    downDocument(listFileName,docId, fileName, filePath, totalSize);
//                }

            }
        });

        fileListAdapter.setShareFileListener(new FileListAdapter.ShareFileListener() {
            @Override
            public void shareFile(String uuid) {
                shareUuId = uuid;
                atAllUserInfos.clear();
                atOrgunitionLists.clear();
                Intent intent = new Intent();
                intent.putExtra("TITLE", "选择联系人");
                intent.putExtra("ACTION", "share");
                intent.putExtra("atUserInfos", atAllUserInfos);
                intent.putExtra("atOrgunitionLists", atOrgunitionLists);
                ((BaseApplication) PublicAreaActivity.this.getApplication()).getUIController().onIMOrgClickListener(PublicAreaActivity.this, intent, 2);

            }
        });

        searchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputManger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
                fileListAdapter.replaceAll(currentFolders);
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

        lvFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document document = (Document) fileListAdapter.getItem(position);
                if (null == document.getActualVersion()) {
                    currFolder = document;//当前目录
                    currentFolders.clear();
                    superId = document.getUuid();
                    getChildrenFolder(document);
                    fileListAdapter.clear();
                    fileName = document.getPath().substring(document.getPath().lastIndexOf("/") + 1);
                }else {
                    if(!TextUtils.isEmpty(isChat)){
                        Intent intent = new Intent();
                        intent.putExtra("uuid",document.getUuid());
                        intent.putExtra("fileSize",document.getActualVersion().getSize());
                        String fileName =  document.getPath().substring(document.getPath().lastIndexOf("/")+1);
                        intent.putExtra("name",fileName);
                        intent.putExtra("serverPath",document.getPath());
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }

            }
        });

        ivNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PublicAreaActivity.this,NewFolderActivity.class);
                intent.putExtra("path",currFolder.getPath());
                startActivityForResult(intent,NEW_FOLDER_CODE);
            }
        });

        ibUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUploadPop();
            }
        });

    }

    /**
     * 搜索文档
     */
    private void searchFile(String name){
        searchFolders.clear();
        progressDialogUtil.showLoadingDialog(context,"",true);
        netHandler.getFindAll(name,searchRootPath, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if(!TextUtils.isEmpty(response)){
                    OkmFile okmFile = JSON.parseObject(response, OkmFile.class);
                    if(null != okmFile.getFolders()){
                        searchFolders.addAll(okmFile.getFolders());
                    }
                    if(null != okmFile.getDocuments()){
                        searchFolders.addAll(okmFile.getDocuments());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileListAdapter.replaceAll(searchFolders);
                        }
                    });
                }
                progressDialogUtil.dismiss();
            }
        });
    }

    /**
     * 分享文档
     */
    private void shareDocument(String userIds) {
        progressDialogUtil.showLoadingDialog(context, "", true);
        netHandler.postShareDocument(shareUuId, userIds, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Log.d(TAG, "onError : " + e.getMessage());
                Toast.makeText(context, R.string.sky_drive_share_failed, Toast.LENGTH_LONG).show();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse :" + response);
                Toast.makeText(context, R.string.sky_drive_share_success, Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "downDocument --> downloadId : "+downloadId);
        CloudApplication.downloadIdMap.put(downloadId, docId);
        downloadFileNameMap.put(downloadId,listFileName);
//        //button.setBackgroundResource(R.color.white);
//        netHandler.downDocument(docId, new FileCallBack(CommConstants.SD_DOCUMENT+filePath,fileName) {
//            @Override
//            public void inProgress(float progress, long total) {
//                int pro = Math.abs((int)((progress/totalSize)*100));
//                //Log.d(TAG, "inProgress : " + pro);
//                button.setProgress(pro);
//            }
//
//            @Override
//            public void onError(Call call, Exception e) {
//                Log.d(TAG, "onError : " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(File response) {
//                Log.d(TAG, "onResponse :" + response.getAbsolutePath());
//                //button.setBackgroundResource(R.drawable.shape_stroke_green);
//                fileListAdapter.notifyDataSetChanged();
//                //progressDialogUtil.dismiss();
//            }
//        });

    }

    /**
     * 获取根目录
     */
    private void getRootFolder() {
        progressDialogUtil.showLoadingDialog(context, "", true);
        netHandler.getRootFolder(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    Document rootFolder = JSON.parseObject(response, Document.class);
                    rootId = rootFolder.getUuid();
                    documents.put(rootId, rootFolder);
                    searchRootPath = rootFolder.getPath();
                    rootPath = rootFolder.getPath().substring(rootFolder.getPath().lastIndexOf("/") + 1);
//                    if (rootFolder.isHasChildren()) {
//                        getChildrenFolder(rootFolder);
//                    } else {
//                        getChildrenDocument(rootFolder);
//                    }
                    getDeptDocument(rootFolder);
                } else {
                    progressDialogUtil.dismiss();
                }
            }
        });
    }

    /**
     * 获取个人根目录
     */
    private void getPersonalFolder() {
        progressDialogUtil.showLoadingDialog(context, "", true);
        netHandler.getPersonalFolder(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    Document rootFolder = JSON.parseObject(response, Document.class);
                    currFolder = rootFolder;//当前目录
                    rootId = rootFolder.getUuid();
                    searchRootPath = rootFolder.getPath();
                    documents.put(rootId, rootFolder);
                    personalPath = rootFolder.getPath().substring(rootFolder.getPath().lastIndexOf("/") + 1);
                    if (rootFolder.isHasChildren()) {
                        getChildrenFolder(rootFolder);
                    } else {
                        getChildrenDocument(rootFolder);
                    }
                } else {
                    progressDialogUtil.dismiss();
                }
            }
        });
    }


    /**
     * 获取目录
     *
     * @param superFolder 父目录
     */
    private void getChildrenFolder(final Document superFolder) {
        folderCount = 0;
        final String uuid = superFolder.getUuid();
        netHandler.getFolderChildren(uuid, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
                getChildrenDocument(superFolder);
            }

            @Override
            public void onResponse(String response) {

                if (!TextUtils.isEmpty(response)) {
                    final OkmFile okmFile = JSON.parseObject(response, OkmFile.class);
                    final ArrayList<Document> folders = okmFile.getFolder();
                    currentFolders.addAll(folders);

                    ArrayList<String> childrenIds = new ArrayList<>();
                    for (Document document : folders) {
                        childrenIds.add(document.getUuid());
                        document.setSupperId(uuid);
                        documents.put(document.getUuid(), document);

//                        if(document.isHasChildren()){//获取下一级目录
//                            getChildrenFolder(document);
//                        }
                        folderCount++;
                    }

                    documents.get(uuid).setChildrenIds(childrenIds);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileListAdapter.add(folders);
                        }
                    });

                } else {
                    progressDialogUtil.dismiss();
                }

                getChildrenDocument(superFolder);
            }
        });
    }

    /**
     * 获取文件
     *
     * @param superFolder 父目录
     */
    private void getChildrenDocument(final Document superFolder) {
        documentCount = 0;
        final String uuid = superFolder.getUuid();
        netHandler.getDocumentChildren(uuid, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    final OkmFile okmFile = JSON.parseObject(response, OkmFile.class);
                    final ArrayList<Document> childrenDocuments = okmFile.getDocument();
                    currentFolders.addAll(childrenDocuments);
                    ArrayList<String> childrenIds = superFolder.getChildrenIds();
                    if (null == childrenIds) {
                        childrenIds = new ArrayList<>();
                    }
                    for (Document document : childrenDocuments) {
                        childrenIds.add(document.getUuid());
                        document.setSupperId(uuid);
                        documents.put(document.getUuid(), document);
                        documentCount++;
                    }
                    documents.get(uuid).setChildrenIds(childrenIds);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileListAdapter.add(childrenDocuments);
                        }
                    });
                }
                progressDialogUtil.dismiss();
            }
        });
    }

    /**
     * 获取部门文件
     *
     * @param superFolder 父目录
     */
    private void getDeptDocument(final Document superFolder) {
        documentCount = 0;
        final String uuid = superFolder.getUuid();
        netHandler.getDeptFolder(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    final OkmFile okmFile = JSON.parseObject(response, OkmFile.class);
                    final ArrayList<Document> childrenDocuments = okmFile.getDocuments();
                    if(null == childrenDocuments){
                        progressDialogUtil.dismiss();
                        return;
                    }
                    currentFolders.addAll(childrenDocuments);
                    ArrayList<String> childrenIds = superFolder.getChildrenIds();
                    if (null == childrenIds) {
                        childrenIds = new ArrayList<>();
                    }
                    for (Document document : childrenDocuments) {
                        childrenIds.add(document.getUuid());
                        document.setSupperId(uuid);
                        documents.put(document.getUuid(), document);
                        documentCount++;
                    }
                    documents.get(uuid).setChildrenIds(childrenIds);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fileListAdapter.add(childrenDocuments);
                        }
                    });
                }
                progressDialogUtil.dismiss();
            }
        });
    }

    //显示文件夹属性
    private void showPop() {
        // 加载popupWindow的布局文件
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.layout_file_attribute, null);

        TextView tvName = (TextView) contentView.findViewById(R.id.tv_name);
        tvName.setText(String.format(getString(R.string.sky_drive_attribute1), fileName));
        TextView tvSize = (TextView) contentView.findViewById(R.id.tv_size);
        tvSize.setVisibility(View.GONE);
        TextView tvFolderCount = (TextView) contentView.findViewById(R.id.tv_folder_count);
        tvFolderCount.setText(String.format(getString(R.string.sky_drive_attribute3), folderCount));
        TextView tvDocumentCount = (TextView) contentView.findViewById(R.id.tv_document_count);
        tvDocumentCount.setText(String.format(getString(R.string.sky_drive_attribute4), documentCount));

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

    //显示选择上传类型入口
    private void showUploadPop() {
        // 加载popupWindow的布局文件
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.layout_upload_file, null);

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

        // 设置SelectPicPopupWindow弹出窗体动画效果
        popupWindow.setAnimationStyle(R.style.popwindown_animstyle);

        popupWindow.showAsDropDown(rlTop);

        // 点击自身popupWindow消失
        contentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Button btCancel = (Button) contentView.findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        //选择图片
        TextView tvUploadPic = (TextView)contentView.findViewById(R.id.tv_upload_pic);
        tvUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,REQUEST_CODE_FILE);
            }
        });

        //选择视频
        TextView tvUploadVideo = (TextView)contentView.findViewById(R.id.tv_upload_video);
        tvUploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,REQUEST_CODE_FILE);
            }
        });

        //选择文件
        TextView tvUploadFile = (TextView)contentView.findViewById(R.id.tv_upload_file);
        tvUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,REQUEST_CODE_FILE);
            }
        });
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackListener();
        }
        return true;
    }

    //返回监听
    private void onBackListener() {
        folderCount = 0;
        documentCount = 0;
        currentFolders.clear();
        if (TextUtils.isEmpty(superId) || rootId.equals(superId)) {
            onBackPressed();
        } else {
            OkHttpUtils.getInstance().cancelTag(superId);

            Document subDocument = documents.get(superId);
            String subSubId = subDocument.getSupperId();

            superId = subSubId;

            Document subSubDocument = documents.get(subSubId);
            ArrayList<String> childIds = subSubDocument.getChildrenIds();
            ArrayList<Document> subDocuments = new ArrayList<>();
            for (String childId : childIds) {
                Document document = documents.get(childId);
                subDocuments.add(document);
                if (null == document.getActualVersion()) {
                    folderCount++;
                } else {
                    documentCount++;
                }
            }
            fileListAdapter.replaceAll(subDocuments);
            currentFolders.addAll(subDocuments);

            fileName = subSubDocument.getPath().substring(subSubDocument.getPath().lastIndexOf("/") + 1);
            if (!TextUtils.isEmpty(personalPath) && personalPath.equals(fileName)) {
                fileName = title + getString(R.string.sky_drive_folder);
            }
            if (!TextUtils.isEmpty(rootPath) && rootPath.equals(fileName)) {
                fileName = title + getString(R.string.sky_drive_folder);
            }

            currFolder = subSubDocument;//当前目录
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String uuid = CloudApplication.downloadIdMap.get(msg.obj);
                    Log.d(TAG, "updateView --> uuid: " + uuid);
                    if (!TextUtils.isEmpty(uuid)) {
                        Document button = getPositionInAdapter(uuid);
                        if (null != button) {
                            int progress = msg.arg1;
                            double totalSize = button.getActualVersion().getSize();
                            int pro = Math.abs((int) ((progress / totalSize) * 100));
                            Log.d(TAG, "updateView --> pro: " + pro);
                            publishProgress(button.getPositionInAdapter(),pro);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Document getPositionInAdapter(String uuid) {
        ArrayList<Document> documents = fileListAdapter.getAll();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            if (uuid.equals(document.getUuid())) {
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
        if(positionInAdapter >= lvFile.getFirstVisiblePosition() &&
                positionInAdapter <= lvFile.getLastVisiblePosition()) {
            int positionInListView = positionInAdapter - lvFile.getFirstVisiblePosition();
            DonutProgress item = (DonutProgress) lvFile.getChildAt(positionInListView)
                    .findViewById(R.id.cloud_item_numberCircleProgressBar);
            item.setProgress(progress);
            if(progress >= 100){
                fileListAdapter.getAll().get(positionInAdapter).setDownload(1);
                fileListAdapter.notifyDataSetChanged();
            }
        }
    }

    //更新下载进度
    class DownloadChangeObserver extends ContentObserver {


        public DownloadChangeObserver(){
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

        public void updateView() {

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
                int[] bytesAndStatus = getBytesAndStatus((long)key);
                    handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[2],
                            key));

            }

            if(CloudApplication.downloadIdMap.isEmpty()){
                for (int i = 0; i < fileListAdapter.getAll().size(); i++) {
                    if(fileListAdapter.getAll().get(i).isDownload() == 2){
                        fileListAdapter.getAll().get(i).setDownload(1);
                    }
                }
                fileListAdapter.notifyDataSetChanged();
            }

        }


        public int[] getBytesAndStatus(long downloadId) {
            int[] bytesAndStatus = new int[] { -1, -1, 0 };
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
            Log.d(TAG, "onReceive: "+completeDownloadId);
            DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(completeDownloadId);
            Cursor c = null;
            try {
                c = downloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    int downloadStatus = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int fileNameIdx =c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    String fileName = c.getString(fileNameIdx);
                    // TODO Do something with the file.
                    Log.d(TAG, fileName);
                    Log.d(TAG, "onReceive: --> downloadStatus: "+downloadStatus);

                    if(downloadStatus == DownloadManager.STATUS_SUCCESSFUL){
                        //纪录下载文档日志
                        String uuid = CloudApplication.downloadIdMap.get(completeDownloadId);
                        String listFileName = downloadFileNameMap.get(completeDownloadId);
                        netHandler.saveDownloadFile(uuid,listFileName,userId);
                        // to do here
                        refreshAdapter(fileName);
                        CloudApplication.downloadIdMap.remove(completeDownloadId);
                        downloadNext();
                    }else if(downloadStatus == DownloadManager.STATUS_FAILED){
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

    private void downloadNext(){
        if(!CloudApplication.waitList.isEmpty()){
            HashMap<String,String> map = CloudApplication.waitList.get(0);
            downDocument(map.get("listFileName"),map.get("docId"),map.get("fileName"),map.get("filePath"),0);
            CloudApplication.waitList.remove(0);
        }
    }

    private void refreshAdapter(String fileName) {
        ArrayList<Document> documents = fileListAdapter.getAll();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String path = CommConstants.SD_DOCUMENT+document.getPath().substring(1).replace(":","");
            Log.d(TAG, "refreshAdapter -->  path: "+path);
            if (fileName.equals(path)) {
                fileListAdapter.getAll().get(i).setDownload(1);
                break;
            }
        }
        fileListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 2:
                if(null == data)
                    return;

                atAllUserInfos = (ArrayList<UserInfo>) data
                        .getSerializableExtra("atUserInfos");
                StringBuilder userIds = new StringBuilder();

                if(atAllUserInfos.size() == 1 && userId.equals(atAllUserInfos.get(0).getId())){
                    ToastUtils.showToast(context,getString(R.string.sky_drive_no_share_self));
                    return;
                }
                for (int i = 0; i < atAllUserInfos.size(); i++) {
                    String id = atAllUserInfos.get(i).getId();
                    if(!userId.equals(id)){
                        userIds.append(atAllUserInfos.get(i).getId());
                        if(i < atAllUserInfos.size()){
                            userIds.append(",");
                        }
                    }
                }
                shareDocument(userIds.toString());
                break;

            case NEW_FOLDER_CODE:
                currentFolders.clear();
                fileListAdapter.clear();
                getChildrenFolder(currFolder);
                break;
            case REQUEST_CODE_FILE:
                if(null == data)
                    return;
                Uri uri = data.getData();
                String filePath = FileUtils.getPath(PublicAreaActivity.this,uri);
                Log.i(TAG, "------->" + filePath);
                if(!TextUtils.isEmpty(filePath)){
                    String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                    uploadFile(filePath,fileName);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * upload file
     * @param filePath 文件路径
     */
    private void uploadFile(String filePath,String fileName){

        String destFileDir = filePath.replace(fileName,"");
        File file = new File(filePath);
        long size = file.length();
        if(size > 1024*1024*20){
            ToastUtils.showToast(getApplicationContext(),"上传的文件大小不能超过20M");
            return;
        }
        progressDialogUtil.showLoadingDialog(context,"",false);
        netHandler.uploadFile(currFolder.getPath(), fileName, file, new FileCallBack(destFileDir,fileName) {
            @Override
            public void inProgress(float progress, long total) {
                Log.d(TAG, "inProgress: "+progress);
            }

            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                popupWindow.dismiss();
                progressDialogUtil.dismiss();
                ToastUtils.showToast(context,getString(R.string.sky_drive_upload_failed));
            }

            @Override
            public void onResponse(File response) throws JSONException {
                popupWindow.dismiss();
                progressDialogUtil.dismiss();
                ToastUtils.showToast(context,getString(R.string.sky_drive_upload_success));

                currentFolders.clear();
                fileListAdapter.clear();
                getChildrenFolder(currFolder);

            }
        });
    }
}
