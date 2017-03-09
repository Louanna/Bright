package com.movitech.EOP.module.workbench.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.SelectPicPopup;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.workbench.adapter.SuggestionsGridAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

public class SuggestionActivity extends EOPBaseActivity {
    private EditText userSuggestion;
    private GridView gridView;
    SelectPicPopup popWindow;

    Uri imageUri;// The Uri to store the big
    String currentTime;

    SuggestionsGridAdapter suggestionsGridAdapter = null;
    List<Object> images;
    boolean disableListener = false;
    private final int MAX_COUNT = 4;
    Map imageMap = new HashMap<String, String>();

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int position = 0;
            Object o = msg.obj;
            if (msg.obj != null) {
                position = (Integer) msg.obj;
            }
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    suggestionsGridAdapter = new SuggestionsGridAdapter(
                            SuggestionActivity.this, images, handler);
                    gridView.setAdapter(suggestionsGridAdapter);
                    break;
                case 2:
                    images.remove(position);
                    disableListener = false;
                    if (!images.contains(R.drawable.image_add)) {
                        images.add(R.drawable.image_add);
                    }
                    suggestionsGridAdapter = new SuggestionsGridAdapter(
                            SuggestionActivity.this, images, handler);
                    suggestionsGridAdapter.setDisableListener(disableListener);
                    suggestionsGridAdapter.setDeling(false);
                    gridView.setAdapter(suggestionsGridAdapter);
                    break;
                case 3:
                    int postion = (Integer) msg.obj;
                    if (!disableListener) {
                        if (postion == images.size() - 1) {
                            InputMethodManager imm = (InputMethodManager) SuggestionActivity.this
                                    .getSystemService(SuggestionActivity.this.INPUT_METHOD_SERVICE);
                            if (imm.isActive()) {
                                imm.hideSoftInputFromWindow(
                                        gridView.getWindowToken(), 0);
                            }
                            // 实例化SelectPicPopupWindow
                            popWindow = new SelectPicPopup(
                                    SuggestionActivity.this, itemsOnClick);
                            // 显示窗口
                            popWindow.showAtLocation(gridView, Gravity.BOTTOM
                                    | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                        } else {
                            suggestionsGridAdapter
                                    .setDeling(!suggestionsGridAdapter.isDeling());
                            suggestionsGridAdapter.notifyDataSetChanged();
                        }
                    } else {
                        suggestionsGridAdapter.setDeling(!suggestionsGridAdapter
                                .isDeling());
                        suggestionsGridAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_suggestion);
        userSuggestion = (EditText) findViewById(R.id.user_suggestion);
        TextView title = (TextView) findViewById(R.id.common_top_title);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        TextView topRight = (TextView) findViewById(R.id.common_top_img_right);
        gridView = (GridView) findViewById(R.id.chat_detail_gridview);
        topRight.setText(getString(R.string.commit));

        String titleString = getIntent().getStringExtra("title");
        title.setText(titleString);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final String userId = CommConstants.loginConfig.getmUserInfo().getEmpAdname();

        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String advice = userSuggestion.getText().toString();
                if (!StringUtils.empty(advice)) {
                    postSuggestion(userId, advice);
                } else {
                    EOPApplication.showToast(context,getString(R.string.please_input_your_suggestion));
                }
            }
        });

        setAdapter();
    }

    private void setAdapter() {
        images = new ArrayList<Object>();
        images.add(R.drawable.image_add);
        suggestionsGridAdapter = new SuggestionsGridAdapter(
                SuggestionActivity.this, images, handler);
        gridView.setAdapter(suggestionsGridAdapter);
    }

    AsyncTask<Void, Void, String> suggestionTask = null;

    private void postSuggestion(final String userId, final String suggestion) {
        progressDialogUtil.showLoadingDialog(SuggestionActivity.this,
                getString(R.string.waiting), false);
        JSONObject object = new JSONObject();
        try {
            String picurl = "";
            for (Object obj : images) {
                if (obj instanceof String) {
                    String localImage = (String) obj;
                    String[] localPath = localImage.split("/");
                    String namePath = localPath[localPath.length - 1];
                    picurl += imageMap.get(namePath) + ";";
                }

            }
            object.put("userid", userId);
            object.put("advicecontent", suggestion);
            object.put("picurl", picurl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken().url(CommConstants.URL_EOP_SUGGESTION)
                .mediaType(JSON)
                .content(object.toString())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                EOPApplication.showToast(context, getString(R.string.chat_send_fail));
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                EOPApplication.showToast(context, getString(R.string.chat_send_success));
                progressDialogUtil.dismiss();
                finish();
            }
        });
    }

    private OnClickListener itemsOnClick = new OnClickListener() {

        public void onClick(View v) {
            popWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_take_photo:
                    // 跳转相机拍照
                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                    if (imageUri == null) {
                        return;
                    }
                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(SuggestionActivity.this, getString(R.string.can_not_find_sd), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent2, 2);

                    break;
                case R.id.btn_pick_photo:
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, 1);
                    break;
                default:
                    break;
            }

        }

    };

    String takePicturePath = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SuggestionActivity.this.RESULT_OK) {
            switch (requestCode) {
                // 如果是直接从相册获取
                case 1:
                    // 从相册中直接获取文件的真是路径，然后上传
                    final String picPath = PicUtils.getPicturePath(data,
                            SuggestionActivity.this);
                    try {
                        takePicturePath = PicUtils
                                .getSmallImageFromFileAndRotaing(picPath);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    notifyGridView(picPath);
                    toUploadFile(takePicturePath);
                    break;
                // 如果是调用相机拍照时
                case 2:
                    if (imageUri != null) {
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            String pathString = CommConstants.SD_CARD_IMPICTURES
                                    + currentTime + ".jpg";
                            PicUtils.scanImages(context, pathString);
                            try {
                                takePicturePath = PicUtils
                                        .getSmallImageFromFileAndRotaing(pathString);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            notifyGridView(pathString);
                            toUploadFile(takePicturePath);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void toUploadFile(String filePath) {
        File file = new File(filePath);
//        HttpManager.uploadFile(CommConstants.URL_UPLOAD, null, file, "file", new ListCallback() {
//            @Override
//            public void onError(Call call, Exception e) {
//                progressDialogUtil.dismiss();
//                EOPApplication.showToast(context, getString(R.string.picture_upload_failed));
//            }
//
//            @Override
//            public void onResponse(JSONArray response) throws JSONException {
//                if (StringUtils.notEmpty(response)) {
//                    progressDialogUtil.dismiss();
//                    File file = new File(takePicturePath);
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                    String uname = response.getJSONObject(0).getString("uName");
//                    String oname = response.getJSONObject(0).getString("oName");
//
//                    imageMap.put(oname.replace("_temp", ""), uname);
//                }
//            }
//        });
    }

    private void notifyGridView(String takePicturePath) {
        if (images != null && images.size() == MAX_COUNT) {
            images.add(images.size() - 1, takePicturePath);
            int k = 0;
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i) instanceof Integer) {
                    k = i;
                    disableListener = true;
                    suggestionsGridAdapter.setDisableListener(disableListener);
                }
            }
            images.remove(k);
        } else {
            images.add(images.size() - 1, takePicturePath);
        }
        handler.sendEmptyMessage(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (suggestionTask != null) {
            suggestionTask.cancel(true);
            suggestionTask = null;
        }
    }
}
