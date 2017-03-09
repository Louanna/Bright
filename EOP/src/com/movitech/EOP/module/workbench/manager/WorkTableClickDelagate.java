package com.movitech.EOP.module.workbench.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.movit.platform.cloud.activity.SkyDriveActivity;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.mail.controller.MailboxEntry;
import com.movit.platform.sc.activity.SCActivity;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.manager.OACallback;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.events.LuckyDrawActivity;
import com.movitech.EOP.module.events.activities.ActivitiesListActivity;
import com.movitech.EOP.module.qrcode.ScanActivity;
import com.movitech.EOP.module.workbench.activity.CompanyNewsActivity;
import com.movitech.EOP.module.workbench.activity.ProcessActivity;
import com.movitech.EOP.module.workbench.activity.SuggestionActivity;
import com.movitech.EOP.module.workbench.activity.WatingActivity;
import com.movitech.EOP.module.workbench.activity.WebViewActivity;
import com.movitech.EOP.module.workbench.activity.WorkTableActivity;
import com.movitech.EOP.module.workbench.attendance.activity.AttendanceListActivity;
import com.movitech.EOP.module.workbench.constants.Constants;
import com.movitech.EOP.module.workbench.meeting.activity.MeetingActivity;
import com.movitech.EOP.module.workbench.model.News;
import com.movitech.EOP.module.workbench.model.WorkTable;
import com.movitech.EOP.utils.DESUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.Call;


/**
 * Created by Administrator on 2016/1/28.
 *
 */
public class WorkTableClickDelagate {

    Context context;
    String token;
    private DialogUtils progressDialogUtil;
    public static final String EMAIL = "enterpriseEmail";
    public static final String OA = "oa";
    public static final String ERP = "erp";
    public static final String TPH = "service";//第三方服务
    public static final String ADDRESS = "adress";//知识地图
    public static final String COMPANYNEWS = "News";//公司新闻

    private List<WorkTable> oaWorkTables;
    private List<WorkTable> tphWorkTables;

    public WorkTableClickDelagate(Context context) {
        this.context = context;
        token = MFSPHelper.getString(CommConstants.TOKEN);
        progressDialogUtil = DialogUtils.getInstants();
    }

    public Handler tableHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    String[] arr = (String[]) msg.obj;
                    String urlString = "";
                    if (arr[1].contains("?")) {
                        urlString = arr[1] + "&ticket=" + arr[0];
                    } else {
                        urlString = arr[1] + "?ticket=" + arr[0];
                    }
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    context.startActivity(intent);
                    break;
            }
        }
    };

    public void setOaWorkTables(List<WorkTable> oaWorkTables){
        this.oaWorkTables = oaWorkTables;
    }
    public void setTphWorkTables(List<WorkTable> tphWorkTables){
        this.tphWorkTables = tphWorkTables;
    }

    public void onClickWorkTable(List<WorkTable> myWorkTables, int position) {
        WorkTable table = myWorkTables.get(position);
        if (Constants.STATUS_UNAVAILABLE.equals(table.getStatus())) {
            EOPApplication.showToast(context, "建设中...");
            return;
        }
        System.out.println("table.getType()=" + table.getType());
        System.out.println("table.getAndroid_access_url()=" + table.getAndroid_access_url());

        OAManager.addUserActionLog(table.getAndroid_access_url(),1);

        if (Constants.TYPE_INTERNAL_HTML5.equals(table.getType())) {//内部H5应用
            getWorkTableUrl(table.getId());
        } else if (Constants.TYPE_NATIVE_APP.equals(table.getType())) {//内部native应用
            if ("suggest".equals(table.getAndroid_access_url())) {
                String title = table.getName();
                context.startActivity(new Intent(context,
                        SuggestionActivity.class).putExtra(
                        "title", title));
            } else if ("meeting".equals(table
                    .getAndroid_access_url())) {
                Intent intent = new Intent(context,
                        MeetingActivity.class);
                context.startActivity(intent);
            } else if ("workStream".equals(table
                    .getAndroid_access_url())) {// 流程
                context.startActivity(new Intent(context,
                        WatingActivity.class));
            } else if ("ea".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        AttendanceListActivity.class));
            } else if ("mail".equals(table
                    .getAndroid_access_url())) {
                ActivityUtils.sendMail(context, "");
            } else if ("sign".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        ScanActivity.class).putExtra(
                        "type", "sign"));
            } else if ("document".equals(table
                    .getAndroid_access_url())) {
                context.startActivity(new Intent(context,
                        SkyDriveActivity.class));
            } else if ("innerea".equals(table
                    .getAndroid_access_url())) {
            } else if ("luckyDraw".equals(table.getAndroid_access_url())) {
                //活动列表界面
                context.startActivity(new Intent(context, ActivitiesListActivity.class));
            } else if ("drawMan".equals(table.getAndroid_access_url())) {
                //抽奖人界面
                context.startActivity(new Intent(context, LuckyDrawActivity.class));
            } else if (EMAIL.equals(table.getAndroid_access_url())) {
                MailboxEntry.tryEnter();
            } else if (OA.equals(table.getAndroid_access_url())) {
                Intent intent = new Intent(context,WorkTableActivity.class);
                intent.putExtra("topTitle",table.getName());
                intent.putExtra("moduleId",table.getAndroid_access_url());
                intent.putExtra("workTable",(Serializable)oaWorkTables);
                context.startActivity(intent);
            } else if (ERP.equals(table.getAndroid_access_url())) {
                UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();

                if(StringUtils.notEmpty(userInfo) && !StringUtils.empty(userInfo.getMingyuanNo())){
//                    Intent intent = new Intent(context,ProcessActivity.class);
//                    intent.putExtra("topTitle",table.getName());
//                    intent.putExtra("systemCode",OAManager.ERP);
//                    intent.putExtra("moduleId",table.getAndroid_access_url());
//                    context.startActivity(intent);

                    Intent intent = new Intent(context,WebViewActivity.class);

                    //ERP 需要url加密
                    try {
                        String param = DESUtils.encryptDES(userInfo.getEmpAdname());

                        String urlString = "http://www.fdccloud.com/workflow-micro/my56e650e3e1721/lists/process-list/index?__from=mentor&kindType=5&Userticket=" + param;
                        intent.putExtra("URL", urlString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    intent.putExtra("isOA",false);
                    intent.putExtra("title",table.getName());
                    context.startActivity(intent);

                }else {
                    ToastUtils.showToast(context,context.getString(R.string.process_no_commission));
                }

            } else if (TPH.equals(table.getAndroid_access_url())) {
                Intent intent = new Intent(context,WorkTableActivity.class);
                intent.putExtra("moduleId",table.getAndroid_access_url());
                intent.putExtra("topTitle",table.getName());
                intent.putExtra("workTable",(Serializable)tphWorkTables);
                context.startActivity(intent);
            } else if(ADDRESS.equals(table.getAndroid_access_url())){
                Intent intent = new Intent(context,SCActivity.class);
                intent.putExtra("topTitle",table.getName());
                intent.putExtra("moduleId",table.getAndroid_access_url());
                context.startActivity(intent);
            } else if(COMPANYNEWS.equals(table.getAndroid_access_url())){
                Intent intent = new Intent(context,CompanyNewsActivity.class);
                intent.putExtra("topTitle",table.getName());
                intent.putExtra("moduleId",table.getAndroid_access_url());
                intent.putExtra("code","xw");
                context.startActivity(intent);
            }
        } else if (Constants.TYPE_THIRDPARTY_APP.equals(table.getType())) {//外部app应用
            boolean flag = ActivityUtils.openThirdApplicationWithPackageName(
                    context,
                    table.getAndroid_access_url());
            if (!flag) {
                if (StringUtils.notEmpty(table.getRemarks())) {
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", table.getRemarks());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "当前应用未安装！", Toast.LENGTH_SHORT).show();
                }

            }
        } else if (Constants.TYPE_WEB_HTML5.equals(table.getType())) {//外部H5应用
            if ("timeSheet".equals(table
                    .getAndroid_access_url())) {
                try {
                    String urlString = CommConstants.URL_EOP_TIMESHEET
                            + "Token="
                            + URLEncoder.encode(MFSPHelper
                                    .getString(CommConstants.TOKEN),
                            "UTF-8");

                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    intent.putExtra("TITLE", "TimeSheet");
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if("sheet".equals(table.getAndroid_access_url())){
                stepToSheet(table.getAndroid_access_url(),table.getName());
            } else if ("duty".equals(table
                    .getAndroid_access_url())) {// 考勤
                try {
                    String urlString = Constants.URL_OFFICE_ATTENDANCE
                            + "Token="
                            + URLEncoder.encode(MFSPHelper
                                    .getString(CommConstants.TOKEN),
                            "UTF-8");
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    intent.putExtra("goChat", true);
                    context.startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if ("hr".equals(table
                    .getAndroid_access_url())) {
                try {
                    String urlString = Constants.URL_OFFICE_HR
                            + "Token="
                            + URLEncoder.encode(MFSPHelper
                                    .getString(CommConstants.TOKEN),
                            "UTF-8");
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    intent.putExtra("goChat", true);
                    context.startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if ("task".equals(table
                    .getAndroid_access_url())) {
                try {
                    String urlString = Constants.URL_OFFICE_TASK
                            + "Token="
                            + URLEncoder.encode(MFSPHelper
                                    .getString(CommConstants.TOKEN),
                            "UTF-8");
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("excel".equals(table
                    .getAndroid_access_url())) {// 报表
                EOPApplication
                        .showToast(context, "建设中...");
            } else if ("notice".equals(table
                    .getAndroid_access_url())) {
                try {
                    String urlString = Constants.URL_OFFICE_NEWS
                            + "Token="
                            + URLEncoder.encode(MFSPHelper
                                    .getString(CommConstants.TOKEN),
                            "UTF-8");
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    intent.putExtra("TITLE", "新闻公告");
                    intent.putExtra("cookies", "Token=" + URLEncoder.encode(MFSPHelper.getString(CommConstants.TOKEN)));
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("shop".equals(table
                    .getAndroid_access_url())) {
                try {
                    String urlString = Constants.URL_OFFICE_SHOP
                            + "Token="
                            + URLEncoder.encode(MFSPHelper
                                    .getString(CommConstants.TOKEN),
                            "UTF-8");
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    context.startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (table.getName().startsWith("BPM")
                    || table.getName().startsWith("EIP")
                    || table.getName().equals("盟拓BPM")) {
                try {
                    // cas认证
                    boolean isCas;
                    isCas = context
                            .getPackageManager()
                            .getApplicationInfo(
                                    context
                                            .getPackageName(),
                                    PackageManager.GET_META_DATA).metaData
                            .getBoolean("CHANNEL_CAS");
                    String ticket = MFSPHelper.getString("ticket");
                    if (isCas) {
                        casTickets(ticket,
                                table.getAndroid_access_url());
                    } else {
                        String str;
                        if (CommConstants.IS_CAS) {
                            str = "&UserID=";
                        } else {
                            str = "?UserID=";
                        }
                        String urlString = table
                                .getAndroid_access_url()
                                + str
                                + MFSPHelper.getString(CommConstants.EMPADNAME);

                        Intent intent = new Intent(context,
                                WebViewActivity.class);
                        intent.putExtra("URL", urlString);
                        intent.putExtra("BPM", true);
                        context.startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("movithomepage".equals(table
                    .getAndroid_access_url())) {// 云拓首页

                String urlString = "http://eop.movitech.cn";

                Intent intent = new Intent(context,
                        WebViewActivity.class);
                intent.putExtra("URL", urlString);
                intent.putExtra("movithomepage", true);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context,
                        WebViewActivity.class);
                intent.putExtra("URL",
                        table.getAndroid_access_url());
                context.startActivity(intent);
            }
        } else if ("cas_html".equals(table.getType())) {// Deprecated ，现已不使用
            try {
                // cas认证
                boolean isCas = false;
                isCas = context.getPackageManager()
                        .getApplicationInfo(
                                context.getPackageName(),
                                PackageManager.GET_META_DATA).metaData
                        .getBoolean("CHANNEL_CAS");
                String ticket = MFSPHelper.getString("ticket");
                if (isCas) {
                    casTickets(ticket,
                            table.getAndroid_access_url());
                } else {
                    String urlString = table
                            .getAndroid_access_url()
                            + "?UserID="
                            + MFSPHelper.getString(CommConstants.EMPADNAME);
                    Intent intent = new Intent(context,
                            WebViewActivity.class);
                    intent.putExtra("URL", urlString);
                    context.startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stepToSheet(final String moduleId,final String moduleName){
        OAManager.validateToken(new OACallback() {
            @Override
            public void getOAData() {
                StringBuilder sb = new StringBuilder();
//                String url = "http://10.1.2.27:8080/WebReport/oaouth.jsp?access_token=";
                String url = "http://180.168.26.251:8080/WebReport/oaouth2.jsp?access_token=";
                sb.append(url).append(CommConstants.ACCESSTOKEN);
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("moduleId",moduleId);
                intent.putExtra("title", moduleName);
                intent.putExtra("URL", sb.toString());
                context.startActivity(intent);
            }

            @Override
            public void showDataList(List<News> newsList) {

            }

            @Override
            public void showUnreadCount(int unreadNum) {

            }

            @Override
            public void onError() {

            }
        });
    }

    public void casTickets(final String value, final String url) {

        com.alibaba.fastjson.JSONObject paramsObj = new com.alibaba.fastjson.JSONObject();
        paramsObj.put("service", url);
        HttpManager.postJson(value, paramsObj.toString(), new StringCallback() {

            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {
                tableHandle.obtainMessage(2, new String[]{response, url})
                        .sendToTarget();
            }
        });
    }

    boolean isLoading = false;

    private void getWorkTableUrl(String moduleId) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        progressDialogUtil.showLoadingDialog(context, context.getString(R.string.waiting), false);

        String url = CommConstants.URL_WORK_TABLE;

        JSONObject object = new JSONObject();
        try {
            object.put("token", token);
            object.put("moduleId", moduleId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpManager.postJson(url, object.toString(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                isLoading = false;
                progressDialogUtil.dismiss();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                isLoading = false;
                progressDialogUtil.dismiss();
                if (StringUtils.notEmpty(response)) {
                    Log.d("WorkTableClickDelagate", "onResponse: " + response);
                    JSONObject jsonObject = new JSONObject(response);
                    boolean ok = jsonObject.getBoolean("ok");
                    if (ok) {
                        JSONObject objValue = jsonObject.getJSONObject("objValue");
                        String targetUrl = objValue.getString("targetUrl");

                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("URL", targetUrl);

                        if (objValue.has("headers")) {
                            JSONObject headers = objValue.getJSONObject("headers");
                            intent.putExtra("headers", headers.toString());
//                            if(headers.has("goChat")){
//                                boolean goChat = headers.getBoolean("goChat");
//                                intent.putExtra("goChat", goChat);
//                            }
//                            if(headers.has("BPM")){
//                                boolean BPM = headers.getBoolean("BPM");
//                                intent.putExtra("BPM", BPM);
//                            }
//                            if(headers.has("needTitle")){
//                                boolean needTitle = headers.getBoolean("needTitle");
//                                intent.putExtra("needTitle", needTitle);
//                            }
//                            if(headers.has("moduleId")){
//                                String moduleId = headers.getString("moduleId");
//                                intent.putExtra("moduleId", moduleId);
//                            }
//                            if(headers.has("title")){
//                                String title = headers.getString("title");
//                                intent.putExtra("title", title);
//                            }
                        }

                        if (objValue.has("cookies")) {
                            JSONObject cookies = objValue.getJSONObject("cookies");
                            intent.putExtra("cookies", cookies.toString());
                        }
                        context.startActivity(intent);
                    }
                }

            }
        });
    }
}
