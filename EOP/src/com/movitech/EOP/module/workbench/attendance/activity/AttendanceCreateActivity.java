package com.movitech.EOP.module.workbench.attendance.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.workbench.attendance.adapter.AttendancePicGridAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

public class AttendanceCreateActivity extends EOPBaseActivity {
    GridView gridView;
    TextView title;
    ImageView topLeft;
    ImageView topRight;
    EditText reason;
    TextView address;
    TextView time;
    Button create;

    AttendancePicGridAdapter gridAdapter;
    ArrayList<String> picPaths = new ArrayList<String>();

    Uri imageUri;// The Uri to store the big
    String currentTime;

    private LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;

    public static final int COMPRESS_RESULT = 5;
    public static final int COMPRESS_FAILED = 6;
    boolean isDestory = false;
    public ArrayList<String> uploadImages = new ArrayList<String>();
    int uploadCount = 0;// 当前上传的数量
    int count = 0; // 检测上传响应的计数
    boolean isAllCompressed = false;
    boolean hasUploadFailed = false;
    List<String> picUnames = new ArrayList<String>();
    ArrayList<String> deleteList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_create);

        InitLocation();
        iniView();
        initData();

        setAdapter();
    }

    private void iniView() {
        gridView = (GridView) findViewById(R.id.attendance_gridview);
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        title.setText(getString(R.string.create));
        topRight.setVisibility(View.GONE);

        reason = (EditText) findViewById(R.id.attendance_reason_txt);
        address = (TextView) findViewById(R.id.attendance_location_txt);
        time = (TextView) findViewById(R.id.attendance_time_txt);
        create = (Button) findViewById(R.id.attendance_create);

        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        String url = CommConstants.URL_EOP_ATTENDANCE + "getTime";
        OkHttpUtils.getWithToken().url(url).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {
                Log.v("json", "--" + response);
                JSONObject object = new JSONObject(response);
                boolean ok = object.getBoolean("ok");
                if (ok) {
                    String timeString = object.getString("objValue");
                    handler.obtainMessage(4, timeString).sendToTarget();
                }
            }
        });

        create.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String cause = reason.getText().toString().trim();
                if (StringUtils.empty(cause)) {
                    EOPApplication.showToast(context, getString(R.string.please_input_reason));
                    return;
                }
                if (cause.length() > 50) {
                    EOPApplication.showToast(context, getString(R.string.can_not_more_than_fifty));
                    return;
                }
                if (picPaths.isEmpty()) {
                    EOPApplication.showToast(context, getString(R.string.please_upload_one_pic_at_least));
                    return;
                }
                progressDialogUtil.showLoadingDialog(context,getString(R.string.upload_innear_info),
                        false);
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (isAllCompressed) {
                            uploadCount = uploadImages.size();
                            if (uploadCount == 0) {
                                try {
                                    toSave2();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            ArrayList<String> tempList = new ArrayList<String>();
                            tempList.addAll(uploadImages);

                            for (int i = 0; i < tempList.size(); i++) {
                                String upImage = tempList.get(i);
                                String oImage = upImage.replace("_temp", "");
                                MessageBean bean = new MessageBean();
                                bean.setContent(oImage);
                                toUploadFile(upImage, bean);
                            }
                            tempList = null;
                        } else {
                            handler.postDelayed(this, 500);
                        }
                    }
                });
            }
        });
    }

    public void toSave2() throws UnsupportedEncodingException {

        String picture = "";
        for (String uname : picUnames) {
            picture += uname + ",";
        }
        String sssString = "userId="
                + MFSPHelper.getString(CommConstants.USERID)
                + "&cause="
                + URLEncoder.encode(reason.getText().toString()
                .trim(), "UTF-8") + "&position="
                + address.getText() + "&picture=" + picture;

        String url = CommConstants.URL_EOP_ATTENDANCE + "save?" + sssString;
        OkHttpUtils
                .postWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e) {
                        handler.sendEmptyMessage(99);
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.v("json", "--" + response);
                        try {
                            JSONObject object = new JSONObject(response);
                            boolean ok = object.getBoolean("ok");
                            if (ok) {
                                handler.sendEmptyMessage(7);
                            } else {
                                handler.sendEmptyMessage(99);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void setAdapter() {
        gridAdapter = new AttendancePicGridAdapter(context, picPaths);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (picPaths.size() > position
                        && picPaths.get(position) != null) {
                    Intent intent = new Intent(AttendanceCreateActivity.this,
                            ImageViewPagerActivity.class);
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);
                    intent.putExtra("locationY", location[1]);
                    intent.putExtra("width", view.getWidth());
                    intent.putExtra("height", view.getHeight());
                    intent.putStringArrayListExtra("selectedImgs", picPaths);
                    intent.putExtra("postion", position);
                    startActivityForResult(intent.putExtra("FromBucket", true)
                                    .putExtra("CanDelete", true),
                            ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE);
                    overridePendingTransition(0, 0);
                } else {
                    // 跳转相机拍照
                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                    if (imageUri == null) {
                        return;
                    }
                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(AttendanceCreateActivity.this, getString(R.string.can_not_find_sd),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent2, 2);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        imageUri = null;
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            picPaths.add(CommConstants.SD_CARD_IMPICTURES + currentTime
                                    + ".jpg");
                            PicUtils.scanImages(AttendanceCreateActivity.this,
                                    CommConstants.SD_CARD_IMPICTURES + currentTime
                                            + ".jpg");

                            isAllCompressed = false;

                            if (uploadImages.size() == picPaths.size()) {
                                isAllCompressed = true;
                            }

                            for (int i = 0; i < picPaths.size(); i++) {
                                // 已经有压缩过的不需要再次压缩
                                String tempPath = PicUtils.getTempPicPath(picPaths
                                        .get(i));
                                if (!uploadImages.contains(tempPath)) {
                                    handler.post(new CompressImageRunnale(picPaths
                                            .get(i)));
                                }
                            }
                            gridAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
            case ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE:
                if (resultCode == ImageViewPagerActivity.IMAGEVIEWPAGE_DELETE) {
                    Log.v("result", "IMAGEVIEWPAGE_DELETE");
                    deleteList = data
                            .getStringArrayListExtra("IMAGEVIEWPAGE_DELETE");

                    for (int i = 0; i < deleteList.size(); i++) {
                        if (picPaths.contains(deleteList.get(i))) {
                            picPaths.remove(deleteList.get(i));
                        }
                    }
                    ArrayList<String> tempList = new ArrayList<String>();
                    tempList.addAll(uploadImages);
                    for (int i = 0; i < tempList.size(); i++) {
                        if (!picPaths
                                .contains(tempList.get(i).replace("_temp", ""))) {
                            uploadImages.remove(tempList.get(i));
                        }
                    }
                    tempList = null;

                    for (int i = 0; i < deleteList.size(); i++) {
                        String newPath = PicUtils.getTempPicPath(deleteList.get(i));
                        File f = new File(newPath);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    gridAdapter.notifyDataSetChanged();
                }
            default:
                break;
        }
    }

    class CompressImageRunnale implements Runnable {
        String filePath;

        public CompressImageRunnale(String filePath) {
            super();
            this.filePath = filePath;
        }

        @Override
        public void run() {
            String path = "";
            try {
                path = PicUtils.getSmallImageFromFileAndRotaing(filePath);
                Message message = new Message();
                MessageBean bean = new MessageBean();
                bean.setContent(filePath);
                Bundle data = new Bundle();
                data.putString("filePath", path);
                data.putSerializable("MessageBean", bean);
                message.what = COMPRESS_RESULT;
                message.setData(data);
                handler.sendMessage(message);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                handler.obtainMessage(COMPRESS_FAILED, filePath).sendToTarget();
            }
        }
    }

    private void toUploadFile(String filePath, final MessageBean bean) {

        File file = new File(filePath);
//        HttpManager.uploadFile(CommConstants.URL_UPLOAD, null, file, "file", new ListCallback() {
//            @Override
//            public void onError(Call call, Exception e) {
//            }
//
//            @Override
//            public void onResponse(JSONArray response) throws JSONException {
//                if (StringUtils.notEmpty(response)) {
//                    count++;
//                    android.os.Message msg = android.os.Message.obtain();
//                    msg.what = UPLOAD_SUCCESS_CODE;
//                    Bundle data = new Bundle();
//                    data.putString("message", response.getJSONObject(0).getString("uName"));
//                    data.putString("uploadPath", bean.getContent());
//                    msg.setData(data);
//                    handler.sendMessage(msg);
//                }
//            }
//        });
    }

    private static final int UPLOAD_SUCCESS_CODE = 1;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 4:
                    String sysTime = (String) msg.obj;
                    time.setText(sysTime);
                    break;
                case COMPRESS_RESULT:
                    // 压缩后的temp图片路径
                    Bundle data = msg.getData();
                    String filePath = data.getString("filePath");
                    if (isDestory) {
                        File f = new File(filePath);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    MessageBean bean = (MessageBean) data
                            .getSerializable("MessageBean");
                    // 判断这次的压缩图是否是已选择中的
                    if (picPaths.contains(bean.getContent())) {
                        if (!uploadImages.contains(filePath)) {
                            uploadImages.add(filePath);
                            if (uploadImages.size() == picPaths.size()) {
                                isAllCompressed = true;
                            }
                        }
                    }
                    break;
                case COMPRESS_FAILED:
                    String str = (String) msg.obj;
                    EOPApplication.showToast(context,
                            getString(R.string.you_take_picture_num) + picPaths.indexOf(str) + getString(R.string.picture_error));
                    break;
                case UPLOAD_SUCCESS_CODE:
                    Bundle bundle = msg.getData();
                    String message = bundle.getString("message");
                    String path = bundle.getString("uploadPath");
                    // 上传成功，发送消息
                    try {
                        picUnames.add(message);
                        // 删除拍照旋转的图片；
                        String newPathString = PicUtils.getTempPicPath(path);
                        File f = new File(newPathString);
                        if (f.exists()) {
                            f.delete();
                        }
                        // 删除已上传了的
                        uploadImages.remove(newPathString);
                        // 判断是否全部上传完
                        if (!uploadImages.isEmpty()) {
                            return;
                        }
                        toSave2();
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }
                    break;
                case 2 | 3:
                    if (!hasUploadFailed) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                hasUploadFailed = true;
                                while (true) {
                                    if (count >= uploadCount) {// 上传动作做完后
                                        handler.sendEmptyMessage(999);
                                        break;
                                    }
                                }
                            }
                        }).start();
                    }
                    break;
                case 7:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, getString(R.string.upload_succeed));
                    setResult(1);
                    finish();
                    break;
                case 99:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, getString(R.string.upload_failed));
                    break;
                case 999:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, uploadImages.size()
                            + getString(R.string.picture_failed));
                    topRight.setEnabled(true);
                    hasUploadFailed = false;
                    count = 0;
                    break;
                default:
                    break;
            }
        }
    };

    private void InitLocation() {

        mLocationClient = new LocationClient(context);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        String tempcoor = "gcj02";
        option.setOpenGps(true);
        option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
        int span = 3000;
        option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 是否要反地理编码
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // Receive Location
            String addr = location.getAddrStr();
            float direction = location.getDirection();
            String street = location.getStreet() + " "
                    + location.getStreetNumber();
            address.setText(addr);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }

    @Override
    protected void onStart() {
        mLocationClient.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (!uploadImages.isEmpty()) {
            for (int i = 0; i < uploadImages.size(); i++) {
                // 删除拍照旋转的图片；
                File f = new File(uploadImages.get(i));
                if (f.exists()) {
                    f.delete();
                }
            }
        }
        isDestory = true;
        BitmapAjaxCallback.clearCache();
        System.gc();
        mLocationClient.unRegisterLocationListener(mMyLocationListener);
        mLocationClient = null;
        super.onDestroy();
    }

}
