package com.movit.platform.common.module.user.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Intents.Insert;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import okhttp3.Call;
import okhttp3.MediaType;

public class UserDetailActivity extends Activity {

    private ImageView avatar, gender, addAttention, callPhone, topLeft;
    private TextView name, subname, empid, objname, jobTitle, ofice_phone, mail, title, userCity;
    private Button send;
    private TextView nameText,timeText;

    private boolean isAdd = false;
    private UserInfo userVO = null;
    private AQuery aQuery;
    private LinearLayout toColleage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comm_activity_user_detail);
        aQuery = new AQuery(this);

        iniView();
        iniData();
    }

    private void iniView() {
        nameText = (TextView) findViewById(R.id.nameText);
        timeText = (TextView) findViewById(R.id.timeText);
        avatar = (ImageView) findViewById(R.id.user_avatar);
        name = (TextView) findViewById(R.id.user_name);
        subname = (TextView) findViewById(R.id.user_subname);
        gender = (ImageView) findViewById(R.id.user_gender);
        empid = (TextView) findViewById(R.id.user_empid);
        objname = (TextView) findViewById(R.id.user_objname);
        jobTitle = (TextView) findViewById(R.id.user_jobtitle);
        userCity = (TextView) findViewById(R.id.user_city);
        ofice_phone = (TextView) findViewById(R.id.user_office_phone);
        mail = (TextView) findViewById(R.id.user_mail);
        send = (Button) findViewById(R.id.user_send_msg_btn);
        addAttention = (ImageView) findViewById(R.id.user_add_friend);
        callPhone = (ImageView) findViewById(R.id.user_call_phone);
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        toColleage = (LinearLayout) findViewById(R.id.user_to_colleage);
        title.setText(R.string.user_detail);

        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
    }

    private void iniData() {
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Intent intent = getIntent();
        userVO = (UserInfo) intent.getSerializableExtra("userInfo");
        if (userVO != null) {

            String isOpen = userVO.getIsOpen();
            String userOpen = userVO.getUserOpen();
            String currentUserId = CommConstants.loginConfig.getmUserInfo().getId();
            if ("1".equals(isOpen) && !userOpen.contains(currentUserId)) {
                callPhone.setVisibility(View.GONE);
            }

            String uname = MFSPHelper.getString(CommConstants.AVATAR);
            String adname = MFSPHelper.getString(CommConstants.EMPADNAME);

            int resid = R.drawable.avatar_male;
            if (getString(R.string.boy).equals(userVO.getGender())) {
                gender.setImageResource(R.drawable.user_man);
                resid = R.drawable.avatar_male;
            } else if (getString(R.string.girl).equals(userVO.getGender())) {
                gender.setImageResource(R.drawable.user_woman);
                resid = R.drawable.avatar_female;
            }
            String avatarName = userVO.getAvatar();
            String picUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                picUrl = avatarName;
            }
            if (adname.equalsIgnoreCase(userVO.getEmpAdname())
                    && StringUtils.notEmpty(uname)) {
                picUrl = uname;
            }
            final String avatarUrl = picUrl;
            final int picId = resid;

            File file = null;
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                        .fileCache(true).targetWidth(128);
                aQuery.id(avatar).image(callback);
                file = aQuery.getCachedFile(CommConstants.URL_DOWN + avatarUrl);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(this, picId,
                        10);
                avatar.setImageBitmap(bitmap);

            }

            final File file2 = file;
            avatar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ArrayList<String> selectImagesList = new ArrayList<String>();
                    //TODO anna
//					ZonePublishActivity.selectImagesList.clear();
                    Intent intent = new Intent(UserDetailActivity.this,
                            ImageViewPagerActivity.class);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    intent.putExtra("locationX", location[0]);
                    intent.putExtra("locationY", location[1]);
                    intent.putExtra("width", v.getWidth());
                    intent.putExtra("height", v.getHeight());
                    intent.putExtra("postion", 0);

                    // TODO anna
                    if (file2 == null) {
                        intent.putExtra("defaultImage", true);
                        intent.putExtra("picid", picId);

                        selectImagesList.add(picId + "");
//						ZonePublishActivity.selectImagesList.add(picId + "");
                    } else {
//						ZonePublishActivity.selectImagesList.add(file2
//								.getAbsolutePath());
                        selectImagesList.add(file2.getAbsolutePath());
                    }

//					intent.putStringArrayListExtra("selectedImgs",
//							ZonePublishActivity.selectImagesList);

                    intent.putStringArrayListExtra("selectedImgs", selectImagesList);

                    startActivity(intent);
                    overridePendingTransition(0, 0);

                }
            });

            String[] nameStrings = userVO.getEmpCname().split("\\.");
            if (nameStrings != null && nameStrings.length > 0) {
                name.setText(nameStrings[0]);
            }
            if (nameStrings != null && nameStrings.length > 1) {
                subname.setText(nameStrings[1]);
            }

            empid.setText(userVO.getEmpId());

            subname.setText(userVO.getEmpAdname());
            UserDao dao = UserDao.getInstance(this);
            OrganizationTree org = dao.getOrganizationByOrgId(userVO.getOrgId());
            if (org != null) {
                objname.setText(org.getObjname());
            }
            jobTitle.setText(userVO.getEmpId());

            if (StringUtils.notEmpty(userVO.getCity())) {
                userCity.setText(userVO.getCity());
            }

            if (StringUtils.notEmpty(userVO.getPhone())) {
                ofice_phone.setText(userVO.getPhone());
                ofice_phone.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (PhoneNumberUtils.isGlobalPhoneNumber(userVO
                                .getPhone().replace(" ", ""))) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            i.setData(Uri.parse("tel:"
                                    + userVO.getPhone().replace(" ", "")));
                            startActivity(i);
                        }
                    }
                });
                ofice_phone.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        // 保存至现有联系人
                        String phone = userVO.getPhone();
                        String name = userVO.getEmpCname();
                        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        intent.setType("vnd.android.cursor.item/contact");
                        intent.putExtra(Insert.NAME, name);
                        intent.putExtra(Insert.PHONE, phone);
                        intent.putExtra(Insert.PHONE_TYPE, Phone.TYPE_WORK);
                        startActivity(intent);

                        return false;
                    }
                });
            }
            if (StringUtils.notEmpty(userVO.getMail())) {
                mail.setText(userVO.getMail());
//                mail.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        ArrayList<UserInfo> result = new ArrayList<>();
//                        result.add(userVO);
//                        Intent intent = new Intent();
//                        intent.putExtra("userInfos", result);
//                        BaseApplication application = (BaseApplication) getApplication();
//                        application.getUIController().onSendEmailClickListener(UserDetailActivity.this, intent);
//                        ActivityUtils.sendMail(UserDetailActivity.this, userVO.getMail());
//                    }
//                });
            }
            send.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", userVO);
                    intent.putExtras(bundle);

                    ((BaseApplication) UserDetailActivity.this.getApplication()).getUIController().onSendMessageClickListener(UserDetailActivity.this, intent);
                }
            });
            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                    .getAttentionPO();
            if (idStrings.contains(userVO.getId())) {
                isAdd = true;
            } else {
                isAdd = false;
            }
            if (isAdd) {
                addAttention.setImageResource(R.drawable.star_1);
            } else {
                addAttention.setImageResource(R.drawable.star_2);
            }
            addAttention.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!runAttention) {
                        if (isAdd) {
                            delAttentions();
                        } else {
                            addAttentions();
                        }
                    }
                }
            });

            callPhone.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String phoneNum = userVO.getMphone();
                    if (TextUtils.isEmpty(phoneNum)) {
                        ToastUtils.showToast(UserDetailActivity.this, getString(R.string.user_no_phone));
                        return;
                    }
                    boolean canCall;

                    int day = MFSPHelper.getInteger("day");
                    Set<String> photoSet = MFSPHelper.getStringSet("photoSet");

                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    int nowDay = c.get(Calendar.DAY_OF_MONTH);
                    if (day != nowDay) {
                        // 已经过了一天以上了
                        MFSPHelper.setInteger("day", nowDay);
                        photoSet.clear();
                        canCall = true;
                    } else {
                        // 当天
                        if (photoSet.contains(userVO.getEmpAdname())) {
                            canCall = true;
                        } else {
                            if (photoSet.size() >= CommConstants.loginConfig.getmUserInfo().getCallCount()) {
                                ToastUtils.showToast(UserDetailActivity.this,
                                        getString(R.string.over_time_call_phone));
                                canCall = false;
                            } else {
                                canCall = true;
                            }
                        }
                    }
                    if (canCall) {
                        if (userVO.getEmpAdname().equalsIgnoreCase(
                                "james.tong")) {
                            ToastUtils.showToast(
                                    UserDetailActivity.this, getString(R.string.no_phone));
                            return;
                        }
                        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNum
                                .replace(" ", ""))) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            i.setData(Uri.parse("tel:"
                                    + phoneNum.replace(" ", "")));
                            startActivity(i);

                            photoSet.add(userVO.getEmpAdname());
                            MFSPHelper.setStringSet("photoSet", photoSet);
                        }
                    }
                }
            });

            callPhone.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // 保存至现有联系人
                    String phone = userVO.getMphone();
                    String name = userVO.getEmpCname();
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType("vnd.android.cursor.item/contact");
                    intent.putExtra(Insert.NAME, name);
                    intent.putExtra(Insert.PHONE, phone);
                    intent.putExtra(Insert.PHONE_TYPE, Phone.TYPE_MOBILE);
                    startActivity(intent);

                    return false;
                }
            });

            if (adname.equalsIgnoreCase(userVO.getEmpAdname())) {
                send.setVisibility(View.GONE);
                addAttention.setVisibility(View.GONE);
            }else{
                send.setVisibility(View.VISIBLE);
                addAttention.setVisibility(View.GONE);
            }

            toColleage.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    String curUserId = MFSPHelper.getString(CommConstants.USERID);
                    try {
                        boolean gozone = getPackageManager().getApplicationInfo(
                                getPackageName(), PackageManager.GET_META_DATA).metaData
                                .getBoolean("CHANNEL_ATTENTION_SEEZONE", false);
                        if (gozone) {
                            if (isAdd || curUserId.trim().equalsIgnoreCase(userVO.getId().trim())) {
                                Intent intent = new Intent();
                                intent.putExtra("userId", userVO.getId());
                                ((BaseApplication) UserDetailActivity.this.getApplication()).getUIController().onZoneOwnClickListener(UserDetailActivity.this, intent, 0);
                            } else {
                                ToastUtils.showToast(UserDetailActivity.this, String.format(getString(R.string.attention_to_look), userVO.getEmpCname()));
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("userId", userVO.getId());
                            ((BaseApplication) UserDetailActivity.this.getApplication()).getUIController().onZoneOwnClickListener(UserDetailActivity.this, intent, 0);

                        }
                    } catch (PackageManager.NameNotFoundException e1) {
                        e1.printStackTrace();
                    }

                }
            });

            if(null!=CommConstants.loginConfig.getmUserInfo()){
                nameText.setText(CommConstants.loginConfig.getmUserInfo().getEmpCname());
                timeText.setText(DateUtils.getCurDateStr("yyyy-MM-dd"));
            }
        }
    }

    private boolean runAttention = false;
//    AsyncTask<Void, Void, String> userAttention;

    private void addAttentions() {
        if (runAttention) {
            return;
        }
        runAttention = true;
        JSONObject object = new JSONObject();
        try {
            object.put("userid", CommConstants.loginConfig
                    .getmUserInfo().getId());
            object.put("attentionid", userVO.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(CommConstants.URL_ADD_ATTENTION)
                .content(object.toString())
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        runAttention = false;
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has("ok")) {
                                boolean ok = jsonObject.getBoolean("ok");
                                if (ok) {
                                    addAttention.setImageResource(R.drawable.star_1);
                                    isAdd = true;
                                    ToastUtils.showToast(UserDetailActivity.this,
                                            getString(R.string.attention));
                                    ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                                            .getAttentionPO();
                                    if (!idStrings.contains(userVO.getId())) {
                                        idStrings.add(userVO.getId());
                                    }
                                } else {
                                    ToastUtils.showToast(UserDetailActivity.this,
                                            getString(R.string.attention_fail));
                                }
                            } else {
                                ToastUtils.showToast(UserDetailActivity.this,
                                        getString(R.string.attention_fail));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showToast(UserDetailActivity.this, getString(R.string.attention_fail));
                        }
                        runAttention = false;
                    }
                });

//        if (userAttention != null) {
//            userAttention.cancel(true);
//            userAttention = null;
//        }
//        userAttention = new AsyncTask<Void, Void, String>() {
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected String doInBackground(Void... params) {
//                JSONObject object = new JSONObject();
//                String response = null;
//                try {
//                    object.put("userid", CommConstants.loginConfig
//                            .getmUserInfo().getId());
//                    object.put("attentionid", userVO.getId());
//                    response = HttpClientUtils.post(CommConstants.URL_ADD_ATTENTION, object.toString(),
//                            Charset.forName("UTF-8"));
//                    Log.v("UserDetailActivity", "添加关注" + response);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                return response;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                super.onPostExecute(result);
//                // 添加关注{"objValue":null,"ok":true,"value":"增加关注成功"}
//
//                try {
//                    JSONObject jsonObject = new JSONObject(result);
//                    if (jsonObject.has("ok")) {
//                        boolean ok = jsonObject.getBoolean("ok");
//                        if (ok) {
//                            addAttention.setImageResource(R.drawable.star_1);
//                            isAdd = true;
//                            ToastUtils.showToast(UserDetailActivity.this,
//                                    "已关注");
//                            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
//                                    .getAttentionPO();
//                            if (!idStrings.contains(userVO.getId())) {
//                                idStrings.add(userVO.getId());
//                            }
//                        } else {
//                            ToastUtils.showToast(UserDetailActivity.this,
//                                    "关注失败！");
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    ToastUtils.showToast(UserDetailActivity.this, "关注失败！");
//                }
//                runAttention = false;
//            }
//
//        };
//        userAttention.execute(null, null, null);
    }

//    AsyncTask<Void, Void, String> delAttentionTask;

    private void delAttentions() {
        if (runAttention) {
            return;
        }
        runAttention = true;
        JSONObject object = new JSONObject();
        try {
            object.put("userid", CommConstants.loginConfig
                    .getmUserInfo().getId());
            object.put("attentionid", userVO.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(CommConstants.URL_DEL_ATTENTION)
                .content(object.toString())
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        runAttention = false;
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.has("ok")) {
                                boolean ok = jsonObject.getBoolean("ok");
                                if (ok) {
                                    ToastUtils.showToast(UserDetailActivity.this,
                                            getString(R.string.cancel));
                                    addAttention.setImageResource(R.drawable.star_2);
                                    isAdd = false;
                                    ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                                            .getAttentionPO();
                                    if (idStrings.contains(userVO.getId())) {
                                        idStrings.remove(userVO.getId());
                                    }

                                } else {
                                    ToastUtils.showToast(UserDetailActivity.this,
                                            getString(R.string.cancel_fail));
                                }
                            } else {
                                ToastUtils.showToast(UserDetailActivity.this,
                                        getString(R.string.cancel_fail));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showToast(UserDetailActivity.this, getString(R.string.cancel_fail));
                        }
                        runAttention = false;
                    }
                });


//        if (delAttentionTask != null) {
//            delAttentionTask.cancel(true);
//            delAttentionTask = null;
//        }
//        delAttentionTask = new AsyncTask<Void, Void, String>() {
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected String doInBackground(Void... params) {
//                JSONObject object = new JSONObject();
//                try {
//                    object.put("userid", CommConstants.loginConfig
//                            .getmUserInfo().getId());
//                    object.put("attentionid", userVO.getId());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                String response = HttpClientUtils.post(CommConstants.URL_DEL_ATTENTION, object.toString(),
//                        Charset.forName("UTF-8"));
//
//                Log.v("UserDetailActivity", "取消关注" + response);
//                return response;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                super.onPostExecute(result);
//                // 取消关注{"objValue":null,"ok":true,"value":"删除关注成功"}
//                try {
//                    JSONObject jsonObject = new JSONObject(result);
//                    if (jsonObject.has("ok")) {
//                        boolean ok = jsonObject.getBoolean("ok");
//                        if (ok) {
//                            ToastUtils.showToast(UserDetailActivity.this,
//                                    "已取消");
//                            addAttention.setImageResource(R.drawable.star_2);
//                            isAdd = false;
//                            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
//                                    .getAttentionPO();
//                            if (idStrings.contains(userVO.getId())) {
//                                idStrings.remove(userVO.getId());
//                            }
//
//                        } else {
//                            ToastUtils.showToast(UserDetailActivity.this,
//                                    "取消失败！");
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    ToastUtils.showToast(UserDetailActivity.this, "取消失败！");
//                }
//                runAttention = false;
//            }
//
//        };
//        delAttentionTask.execute(null, null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (userAttention != null) {
//            userAttention.cancel(true);
//            userAttention = null;
//        }
//        if (delAttentionTask != null) {
//            delAttentionTask.cancel(true);
//            delAttentionTask = null;
//        }
    }

}
