package com.movitech.EOP.module.mine.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.manager.CommManager;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.widget.SelectPicPopup;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.activity.LoginActivity;
import com.movitech.EOP.activity.MainActivity;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.BaseFragment;
import com.movitech.EOP.module.mine.activity.ClipImageActivity;
import com.movitech.EOP.module.mine.activity.RenameGroupActivity;
import com.movitech.EOP.module.workbench.activity.WebViewActivity;
import com.movitech.EOP.module.workbench.constants.LanguageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

import static android.app.Activity.RESULT_OK;

public class MyFragemnt extends BaseFragment {
//    ImageView back;
//    ImageView topRight;
//    TextView title;

    TextView help;
    ImageView avatar;
    TextView name;
    TextView objname;
    TextView tvUserDep;
    TextView jobTitle;
    TextView userCity;
    TextView phone;
    TextView officePhone;
    TextView mail;

    LinearLayout clearMDMLayout;

    private int position;

    // 自定义的弹出框类
    SelectPicPopup popWindow;
    // 用来标识请求照相功能的activity

    Uri imageUri;// The Uri to store the big
    String takePicturePath = "";

    private Button userLogout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.eop_fragment_mine, null, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initViews() {
        userLogout = (Button) findViewById(R.id.user_logout);


        help = (TextView) findViewById(R.id.user_clear_cache);

        avatar = (ImageView) findViewById(R.id.user_avatar);
        name = (TextView) findViewById(R.id.tv_user_name);
        objname = (TextView) findViewById(R.id.user_objname);
        tvUserDep = (TextView) findViewById(R.id.tv_user_dep);
        jobTitle = (TextView) findViewById(R.id.user_jobtitle);
        phone = (TextView) findViewById(R.id.user_phone);
        officePhone = (TextView) findViewById(R.id.user_office_phone);
        mail = (TextView) findViewById(R.id.user_mail);
        userCity = (TextView) findViewById(R.id.user_city);
        clearMDMLayout = (LinearLayout)findViewById(R.id.user_clear_mdm_layout);

        help.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyFragemnt.this.getActivity(),
                        WebViewActivity.class);
                intent.putExtra("URL", CommConstants.URL_APP_HELP_DOC);
                intent.putExtra("TITLE", "光明通用户手册");
                startActivity(intent);
            }
        });

        avatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(avatar.getWindowToken(), 0);
                }
                // 实例化SelectPicPopupWindow
                popWindow = new SelectPicPopup(getActivity(),
                        itemsOnClick);
                // 显示窗口
                popWindow.showAtLocation(
                        getActivity().findViewById(R.id.user_avatar),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

            }
        });
        userLogout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                MFSPHelper.setString("GestureCode", "");


                LoginInfo loginConfig = CommConstants.loginConfig;
                new CommonHelper(getActivity()).saveLoginConfig(loginConfig);
                MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, false);
                MFSPHelper.setString(CommConstants.PASSWORD, "");
                MFSPHelper.setString(CommConstants.EMAILPASSWORD, "");
                EOPApplication.exit();
                clearDeviceType();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
        });


        clearMDMLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final CusDialog dialogUtil = CusDialog.getInstance();
                dialogUtil.showCustomDialog(getActivity());
                dialogUtil
                        .setWebDialog(getString(R.string.after_unband));
                dialogUtil.setConfirmClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialogUtil.dismiss();
                        progressDialogUtil.showLoadingDialog(getActivity(),getString(R.string.waiting), false);

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("token",
                                MFSPHelper.getString(CommConstants.TOKEN));
                        params.put("deviceType", "2");

                        TelephonyManager tm = (TelephonyManager) getActivity()
                                .getSystemService(
                                        Context.TELEPHONY_SERVICE);
                        String deviceId = tm.getDeviceId();
                        params.put("device", deviceId);

                        String json = Obj2JsonUtils.map2json(params);
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        OkHttpUtils.postStringWithToken()
                                .url(CommConstants.URL_MDM)
                                .content(json)
                                .mediaType(JSON)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        e.printStackTrace();
                                        mHandler.sendEmptyMessage(99);
                                    }

                                    @Override
                                    public void onResponse(String responseStr) throws JSONException {
                                        JSONObject object = new JSONObject(
                                                responseStr);
                                        boolean ok = object.getBoolean("ok");
                                        if (ok) {
                                            mHandler.sendEmptyMessage(98);
                                        } else {
                                            mHandler.sendEmptyMessage(99);
                                        }
                                    }
                                });
                    }
                });
                dialogUtil.setCancleClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialogUtil.dismiss();
                    }
                });

            }
        });
    }

    private void clearDeviceType() {

        String deviceType = "2";
        JSONObject object = new JSONObject();
        try {
            object.put("userId", MFSPHelper.getString(CommConstants.USERID));
            object.put("deviceType", deviceType);
            object.put("device", "");
            object.put("mobilemodel", CommConstants.PHONEBRAND);
            object.put("mobileversion", CommConstants.PHONEVERSION);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(CommConstants.URL_UPDATE_DEVICE)
                .content(object.toString())
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) throws JSONException {

                    }
                });

        OkHttpUtils.getWithToken()
                .url(CommConstants.URL_LOGOUT)
                .addParams("userId",MFSPHelper.getString(CommConstants.USERID))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {

                    }
                });
    }

    @Override
    protected void initDatas() {
    }

    public class MyAttentionCallback implements CommManager.AttentionCallback{
        @Override
        public void onAfter(){
            String uname = MFSPHelper.getString(CommConstants.AVATAR);
            UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
            UserDao dao = UserDao.getInstance(getActivity());
            UserInfo userInfo2 = dao.getUserInfoById(userInfo.getId());

            if (userInfo2 != null) {
                String avatarName = userInfo2.getAvatar();
                String gender = userInfo2.getGender();
                int picId = R.drawable.avatar_male;
                if (!TextUtils.isEmpty(gender) && "女".equals(gender)) {
                    picId = R.drawable.avatar_female;
                }
                String avatarUrl = "";
                if (StringUtils.notEmpty(avatarName)) {
                    avatarUrl = avatarName;
                }
                if (StringUtils.notEmpty(uname)) {
                    avatarUrl = uname;
                }

                if (StringUtils.notEmpty(avatarUrl)) {
                    MFAQueryHelper.setImageView(avatar,CommConstants.URL_DOWN + avatarUrl,picId);
                } else {
                    Bitmap bitmap = PicUtils.getRoundedCornerBitmap(getActivity(),
                            picId, 10);
                    avatar.setImageBitmap(bitmap);
                }

                if (StringUtils.notEmpty(userInfo.getMphone())) {
                    phone.setText(userInfo.getMphone());
                }
                if (StringUtils.notEmpty(userInfo.getPhone())) {
                    officePhone.setText(userInfo.getPhone());
                }

                phone.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivityForResult(
                                new Intent(getActivity(), RenameGroupActivity.class)
                                        .putExtra("type", "mphone").putExtra(
                                        "text", phone.getText().toString()),
                                10);

                    }
                });
                officePhone.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        startActivityForResult(
                                new Intent(getActivity(), RenameGroupActivity.class)
                                        .putExtra("type", "phone").putExtra("text",
                                        officePhone.getText().toString()),
                                10);
                    }
                });
                if (StringUtils.notEmpty(userInfo.getMail())) {
                    mail.setText(userInfo.getMail());
                }
                String[] nameStrings = userInfo2.getEmpCname().split("\\.");
                if (nameStrings != null && nameStrings.length > 0) {
                    name.setText(nameStrings[0]);
                }
                if (StringUtils.notEmpty(userInfo.getPosition())) {
                    objname.setText(userInfo.getPosition());
                }

                dao = UserDao.getInstance(getActivity());
                OrganizationTree org = dao.getOrganizationByOrgId(userInfo2
                        .getOrgId());
                if (org != null) {
                    tvUserDep.setText(org.getObjname());
                }

                if (StringUtils.notEmpty(userInfo2.getCity())) {
                    userCity.setText(userInfo2.getCity());
                }
                jobTitle.setText(userInfo2.getEmpId());

            }

            try {
                ArrayList<String> temp = new ArrayList<String>();
                ArrayList<String> idStrings = userInfo.getAttentionPO();
                temp.addAll(idStrings);
                dao = UserDao.getInstance(getActivity());
                for (int i = 0; i < temp.size(); i++) {
                    UserInfo user = dao.getUserInfoById(temp.get(i));
                    if (user == null) {
                        idStrings.remove(temp.get(i));
                    }
                }
                temp.clear();
                ArrayList<String> toIdStrings = userInfo.getToBeAttentionPO();

                temp.addAll(toIdStrings);
                for (int i = 0; i < temp.size(); i++) {
                    UserInfo user = dao.getUserInfoById(temp.get(i));
                    if (user == null) {
                        toIdStrings.remove(temp.get(i));
                    }
                }

                if (!CommConstants.GET_ATTENTION_FINISH) {
                    DialogUtils.getInstants().showLoadingDialog(getActivity(), getString(R.string.waiting), true);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (CommConstants.GET_ATTENTION_FINISH) {
                                DialogUtils.getInstants().dismiss();
                                resumeDatas();
                            } else {
                                mHandler.postDelayed(this, 1500);
                            }
                        }
                    }, 1500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void resumeDatas() {
        CommManager.getAttentionData(this.getActivity(),new MyAttentionCallback());
    }

    private OnClickListener itemsOnClick = new OnClickListener() {

        public void onClick(View v) {
            popWindow.dismiss();

            switch (v.getId()) {
                case R.id.btn_take_photo:
                    // 跳转相机拍照
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                    if (imageUri == null) {
                        return;
                    }
                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(getActivity(), getString(R.string.can_not_find_sd), Toast.LENGTH_LONG).show();
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

    String currentTime;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                if (resultCode == 1) {
                    phone.setText(data.getStringExtra("mphone"));
                } else if (resultCode == 2) {
                    officePhone.setText(data.getStringExtra("phone"));
                }

            }
        } else if (requestCode == 3) {
            // 取得裁剪后的图片
            if (data != null) {
                takePicturePath = data.getStringExtra("takePicturePath");
                if (StringUtils.notEmpty(takePicturePath)) {
                    progressDialogUtil.showLoadingDialog(getActivity(),getString(R.string.uploading), false);

                    // 上传图片
                    toUploadFile(takePicturePath);
                }
            }
        } else {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    // 如果是直接从相册获取
                    case 1:
                        // 从相册中直接获取文件的真是路径，然后上传
                        if(null == data)
                            return;
                        Uri uri = data.getData();
                        String picPath = FileUtils.getPath(getActivity(),uri);
                        startActivityForResult(new Intent(getActivity(),
                                ClipImageActivity.class).putExtra(
                                "takePicturePath", picPath), 3);
                        break;
                    // 如果是调用相机拍照时
                    case 2:
                        currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                        if (StringUtils.empty(currentTime)) {
                            return;
                        }
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            String path = CommConstants.SD_CARD_IMPICTURES + currentTime
                                    + ".jpg";
                            startActivityForResult(new Intent(getActivity(),
                                    ClipImageActivity.class).putExtra(
                                    "takePicturePath", path), 3);
                            PicUtils.scanImages(getActivity(), path);
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
                }
            }
        }
    }

    private void toUploadFile(String filePath) {
        File file = new File(filePath);
        HttpManager.uploadFile(CommConstants.URL_UPLOAD, null, file, "file", new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    com.alibaba.fastjson.JSONObject object = JSON.parseObject(response);
                    com.alibaba.fastjson.JSONObject object1 = object.getJSONObject("response");
                    JSONArray array = object1.getJSONArray("message");
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = UPLOAD_SUCCESS_CODE;
                    msg.obj = array.getJSONObject(0).getString("uname");
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private static final int UPLOAD_SUCCESS_CODE = 1;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_SUCCESS_CODE:
                    // 上传成功，更新头像名称
                    String message = (String) msg.obj;
                    try {
                        updateAvatarName(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialogUtil.dismiss();
                        EOPApplication.showToast(getActivity(), getString(R.string.upload_avatar_error));
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
                    MFSPHelper.setString(CommConstants.AVATAR, uname);
                    MFAQueryHelper.setImageView(avatar,CommConstants.URL_DOWN + uname,R.drawable.avatar_male);
                    break;
                case 5:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), getString(R.string.avatar_name_error));
                    break;
                case 11:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), getString(R.string.clear_succeed));
                    break;
                case 98:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), getString(R.string.unband_succeed));
                    MFSPHelper.setString("GestureCode", "");
                    LoginInfo loginConfig = CommConstants.loginConfig;
                    new CommonHelper(getActivity()).saveLoginConfig(loginConfig);
                    MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                    MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, false);
                    MFSPHelper.setString(CommConstants.PASSWORD, "");
                    EOPApplication.exit();
                    clearDeviceType();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    getActivity().startActivity(
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    break;
                case 99:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(),  getString(R.string.unband_failed));
                    break;
                default:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), getString(R.string.upload_avatar_error));
                    // 删除拍照旋转的图片；
                    File f = new File(takePicturePath);
                    if (f.exists()) {
                        f.delete();
                    }
                    break;
            }
        }
    };

    private void updateAvatarName(final String uname) {
        String url = String.format(CommConstants.URL_UPLOAD_AVATAR
                , MFSPHelper.getString(CommConstants.USERID), uname);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(url)
                .content("")
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
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean ok = jsonObject.getBoolean("ok");
                            if (ok) {
                                mHandler.obtainMessage(4, uname).sendToTarget();
                            } else {
                                mHandler.sendEmptyMessage(5);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mHandler.sendEmptyMessage(5);
                        }
                    }
                });
    }

    // select language dialog
    private void languageDialog() {
        int checkedItem = 0;
        if (EOPApplication.LOCALE == Locale.SIMPLIFIED_CHINESE) {
            checkedItem = 0;
        } else if (EOPApplication.LOCALE == Locale.ENGLISH) {
            checkedItem = 1;
        }
        position = checkedItem;

        int theme = Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1 ? AlertDialog.THEME_HOLO_LIGHT
                : 0;
        new AlertDialog.Builder(getActivity(),theme)
                .setTitle(R.string.user_setting_language)
                .setSingleChoiceItems(
                        new String[] { getString(R.string.str_chinese),
                                getString(R.string.str_english) },
                        checkedItem, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                position = which;
                            }
                        })
                .setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                tipChangeLan();
                            }
                        }).setNegativeButton(R.string.cancle, null).show();

    }


    private void tipChangeLan() {

//        int theme = Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1 ? AlertDialog.THEME_HOLO_LIGHT
//                : 0;
//        Dialog alertDialog = new AlertDialog.Builder(getActivity(), theme)
//                .setTitle(R.string.dialog_title)
//                .setMessage(R.string.str_change_language)
//                .setNegativeButton(R.string.cancle,
//                        new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//
//                            }
//                        })
//                .setNeutralButton(R.string.confirm,
//                        new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//
//                                switch (position) {
//                                    case 0:
//                                        EOPApplication.LOCALE = Locale.SIMPLIFIED_CHINESE;
//                                        MFSPHelper.setString("language", LanguageType.CHINESE);
//                                        break;
//                                    case 1:
//                                        EOPApplication.LOCALE = Locale.ENGLISH;
//                                        MFSPHelper.setString("language",LanguageType.ENGLISH);
//                                        break;
//                                    default:
//                                        break;
//                                }
//
//                                Resources resources = getResources();// 获得res资源对象
//                                Configuration config = resources.getConfiguration();// 获得设置对象
//                                DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
//                                config.locale = EOPApplication.LOCALE;
//
//                                resources.updateConfiguration(config, dm);
//
//                                EOPApplication.exit();
//                                Intent intent = new Intent(getActivity(), MainActivity.class);
//                                getActivity().startActivity(
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
//
//                            }
//                        }).create();
//
//        alertDialog.show();

        switch (position) {
            case 0:
                EOPApplication.LOCALE = Locale.SIMPLIFIED_CHINESE;
                MFSPHelper.setString("language", LanguageType.CHINESE);
                break;
            case 1:
                EOPApplication.LOCALE = Locale.ENGLISH;
                MFSPHelper.setString("language",LanguageType.ENGLISH);
                break;
            default:
                break;
        }

        Resources resources = getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = EOPApplication.LOCALE;

        resources.updateConfiguration(config, dm);

        getActivity().finish();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);


    }
}
