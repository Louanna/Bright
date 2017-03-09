package com.movit.platform.mail.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.CertificateValidationException;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.Store;
import com.fsck.k9.mail.Transport;
import com.fsck.k9.mail.store.webdav.WebDavStore;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.EmailListActivity;
import com.movit.platform.mail.activity.EmailLoginActivity;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.AccountCreator;
import com.movit.platform.mail.bean.CheckDirection;
import com.movit.platform.mail.bean.Recipient;
import com.movit.platform.mail.mailstore.LocalFolder;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.util.MyEmailBroadcast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Jamison on 2016/6/13.
 *
 */
public class MailboxEntry {

    private static class RefreshThread extends Thread {
        boolean flag = true;

        @Override
        public void run() {
            while (flag) {
                try {
                    sleep(60000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (flag) {
                    if(null != account){
                        MessageController.getInstance(context.getApplication())
                                .synchronizeMailbox(account, account.getInboxFolderName(), null, null);
                    }
                }
            }
        }

        void stopThread() {
            flag = false;
        }
    }


    private static class CheckEmailAccountTask extends AsyncTask<CheckDirection, Integer, Void> {

        private boolean canEnter = true;

        @Override
        protected Void doInBackground(CheckDirection... params) {
            Intent intent = new Intent(MailboxEntry.CHECKLOGIN);
            try {
                clearCertificateErrorNotifications(checkDirection);
                checkServerSettings(checkDirection);
                //checkDirection = CheckDirection.OUTGOING;
                //clearCertificateErrorNotifications(checkDirection);
                //checkServerSettings(checkDirection);
            } catch (CertificateValidationException cve) {
                handleCertificateValidationException(cve);
                canEnter = false;
                intent.putExtra("loginState", cve.getMessage());
                BaseApplication.getInstance().sendOrderedBroadcast(intent,null);
            }catch (MessagingException e){
                canEnter = false;
                Log.d("CheckEmailAccountTask", "doInBackground: "+e.getMessage());
                if(e.getMessage().contains("password error")){
                    intent.putExtra("loginState", "error");
                    BaseApplication.getInstance().sendOrderedBroadcast(intent,null);
//                    MFSPHelper.setString(CommConstants.EMAILPASSWORD,"");
//                    context.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(null != dialog){
//                                dialog.getErrorTex().setVisibility(View.VISIBLE);
//                            }
//                        }
//                    });
                }else {
                    intent.putExtra("loginState", e.getMessage());
                    BaseApplication.getInstance().sendOrderedBroadcast(intent,null);
                }
            }catch(Exception e) {
                e.printStackTrace();
                canEnter = false;
                showError();
                intent.putExtra("loginState", e.getMessage());
                BaseApplication.getInstance().sendOrderedBroadcast(intent,null);
            }
            return null;
        }

        private void clearCertificateErrorNotifications(CheckDirection direction) {
            final MessageController ctrl = MessageController.getInstance(context.getApplication());
            ctrl.clearCertificateErrorNotifications(account, direction);
        }

        private void checkServerSettings(CheckDirection direction) throws MessagingException {
            switch (direction) {
                case INCOMING: {
                    checkIncoming();
                    break;
                }
                case OUTGOING: {
                    checkOutgoing();
                    break;
                }
            }
        }

        private void checkIncoming() throws MessagingException {
            Store store = account.getRemoteStore();
            if (store instanceof WebDavStore) {
                publishProgress(R.string.account_setup_check_settings_authenticate);
            } else {
                publishProgress(R.string.account_setup_check_settings_check_incoming_msg);
            }

            store.checkSettings();
            if (store instanceof WebDavStore) {
                publishProgress(R.string.account_setup_check_settings_fetch);
            }
            MessageController.getInstance(context.getApplication()).listFoldersSynchronous(account, true, null);
            MessageController.getInstance(context.getApplication())
                    .synchronizeMailbox(account, account.getInboxFolderName(), null, null);

        }


        private void checkOutgoing() throws MessagingException {
//            if (!(account.getRemoteStore() instanceof WebDavStore)) {
//                publishProgress(R.string.account_setup_check_settings_check_outgoing_msg);
//            }
            Transport transport = Transport.getInstance(context.getApplicationContext(), account);
            transport.close();
            try {
                transport.open();
            } finally {
                transport.close();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(Void v) {
            isLoading = false;
            checkedState = checked;
            Intent intent = new Intent(MailboxEntry.CHECKLOGIN);
            if (canEnter) {
                intent.putExtra("loginState", "Success");
                BaseApplication.getInstance().sendOrderedBroadcast(intent,null);
                if(needSendBroadcast){
                    Intent intent2 = new Intent(MailboxEntry.CHECKLOGINMAIN);
                    intent2.putExtra("loginState", "Success");
                    BaseApplication.getInstance().sendOrderedBroadcast(intent2,null);
                }
                enterFirstTime = false;
                saveEnterFirstTime(false);
                saveOldName(name);
                afterCheckSuccessful();
            }else {
                enter = false;
                intent.putExtra("loginState", "");
                BaseApplication.getInstance().sendOrderedBroadcast(intent,null);
            }
        }

    }

    public static String CHECKLOGIN = "check_login";
    public static String CHECKLOGINMAIN = "check_login_main";
    public static int CHECKLOGIN_REQUESTCODE = 6008;
    private static final int noCheck = 0, checking = 1, checked = 2;
    private static Activity context;
    public static Account account;
    private static int checkedState;
    private static boolean enterFirstTime = true;
    public static boolean needSendBroadcast = false;
    private static String password, emailAddress, name;
    private static String sendUrl, sendPort, receiveUrl, receivePort;
    private static boolean isSSLSend = false, isSSLReceived = false;
    public static boolean enter = false;
    private static JSONArray EmailType;
    private static CheckDirection checkDirection = CheckDirection.INCOMING;
//    private static List<UserInfo> allUserInfos;
    private static RefreshThread refreshThread;

    public static void init(Activity activity) {
        context = activity;
        emailAddress = MFSPHelper.getString(CommConstants.EMAIL_ADDRESS);
        password = MFSPHelper.getString(CommConstants.EMAILPASSWORD);
        refreshThread = new RefreshThread();
        name = MFSPHelper.getString(CommConstants.EMPADNAME);
        String oldName = getOldName();
        if (oldName.equals(name)) {
            enterFirstTime = getEnterFirstTime();
        } else {
            enterFirstTime = true;
            saveEnterFirstTime(true);
        }
        checkedState = noCheck;
        MailboxController.init(context.getApplication());
        getAccountFromNet();
        // 刷新邮箱文件夹列表的策略：每次重新进入EOP刷新一次，若不退出不做文件夹列表的刷新
        saveNeedRefreshFolder(true);
    }



    public static UserInfo getUserInfo(String address,Activity activity) {
        UserDao dao = UserDao.getInstance(activity);
        return dao.getUserInfoByAddress(address);
    }

    public static List<Recipient> getAllRecipient(Activity activity) {
        UserDao dao = UserDao.getInstance(activity);

        String sqlStr = "SELECT User.mail,\n" +
                "       User.empAdname,\n" +
                "       User.avatar,\n" +
                "       User.empCname,\n" +
                "       User.gender,\n" +
                "       Orgunit.objName\n" +
                "  FROM User\n" +
                "       LEFT JOIN Orgunit\n" +
                "              ON User.orgId = Orgunit.orgId;";
        Cursor cursor = dao.execSQLForCursor(sqlStr);

        ArrayList<Recipient> arrayList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String email = cursor.getString(cursor.getColumnIndex("mail"));
            if(!TextUtils.isEmpty(email) && email.contains("@")){
                String personal = email.substring(0,email.lastIndexOf("@"));
                Address address = new Address(email, personal);
                Recipient recipient = new Recipient(address);
                recipient.avatar =  cursor.getString(cursor.getColumnIndex("avatar"));
                recipient.gender = cursor.getString(cursor.getColumnIndex("gender"));
                arrayList.add(recipient);
            }
        }
        cursor.close();
        dao.closeDb();

        return arrayList;
    }

    private static String getOldName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("entryState", Context.MODE_PRIVATE);
        return sharedPreferences.getString("oldName", "");
    }

    private static void saveOldName(String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("entryState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("oldName", name);
        editor.apply();
    }

    private static boolean getEnterFirstTime() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("entryState", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("firstTime", true);
    }

    private static void saveEnterFirstTime(boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("entryState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firstTime", b);
        editor.apply();
    }

    public static boolean getNeedRefreshFolder() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("entryState", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("refreshFolder", true);
    }

    public static void saveNeedRefreshFolder(boolean remoteRefresh) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("entryState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("refreshFolder", remoteRefresh);
        editor.apply();
    }


    private static void getAccountFromNet() {
        HttpManager.getJsonWithToken(CommConstants.URL_GET_EMAIL_TYPE, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    JSONObject jsonObject = new JSONObject(response);
                    EmailType = jsonObject.optJSONArray("objValue");
                    getAccountInfo();
                }
            }
        });

    }

    private static void getAccountInfo() {
        System.out.println("CommConstants.URL_GET_EMAIL_INFO=" + CommConstants.URL_GET_EMAIL_INFO);
        HttpManager.getJsonWithToken(CommConstants.URL_GET_EMAIL_INFO, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.optJSONArray("objValue");
                    if (array != null) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.optJSONObject(i);
                            if (obj != null) {
                                getDetail(obj);
                            }
                        }
                    }
                    createAccount();
                }
            }
        });
    }

    private static void getDetail(JSONObject obj) {
        String s = obj.optString("cfgKey");
        if (s != null) {
            switch (s) {
                case "email_send": {
                    sendUrl = obj.optString("cfgValue");
                    break;
                }
                case "email_receive": {
                    receiveUrl = obj.optString("cfgValue");
                    break;
                }
                case "email_send_port": {
                    sendPort = obj.optString("cfgValue");
                    break;
                }
                case "email_receive_port": {
                    receivePort = obj.optString("cfgValue");
                    break;
                }
                case "email_server_send_conn_type": {
                    int value = obj.optInt("cfgValue");
                    isSSLSend = isSSL(value);
                    break;
                }
                case "email_server_receive_conn_type": {
                    int value = obj.optInt("cfgValue");
                    isSSLReceived = isSSL(value);
                    break;
                }
            }
        }
    }

    private static boolean isSSL(int value) {
        if (EmailType != null) {
            for (int i = 0; i < EmailType.length(); i++) {
                JSONObject jsonObject = EmailType.optJSONObject(i);
                if (jsonObject != null) {
                    int id = jsonObject.optInt("id");
                    if (id == value) {
                        int j = jsonObject.optInt("name");
                        return j > 0;
                    }
                }
            }
        }
        return false;
    }

    private static void createAccount() {
        if (emailAddress != null && sendUrl != null && receiveUrl != null) {
            // only use IMAP here
            account = AccountCreator.createAccount(context, emailAddress, password, ServerSettings.Type.IMAP, receiveUrl, sendUrl, isSSLReceived, isSSLSend, receivePort, sendPort);
            if (account != null) {
                checkAccount();
            }
        }
    }


    private static void checkAccount() {
        if (enterFirstTime) {
            if (checkedState == noCheck) {
                checkedState = checking;
                CheckEmailAccountTask c = new CheckEmailAccountTask();
                c.execute();
            }
        } else {
            afterCheckSuccessful();
        }
    }

    private static boolean isLoading = false;

    public static void CheckEmailAccountTask() {
        if (!isLoading) {
            isLoading = true;
            password = MFSPHelper.getString(CommConstants.EMAILPASSWORD);

//            if(TextUtils.isEmpty(password)){
//                Intent intent = new Intent(context,EmailLoginActivity.class);
//                intent.putExtra("emailAddress",emailAddress);
//                context.startActivityForResult(intent, MailboxEntry.CHECKLOGIN_REQUESTCODE);
//            }else {
//                if (emailAddress != null && sendUrl != null && receiveUrl != null) {
//                    // only use IMAP here
//                    account = AccountCreator.createAccount(context, emailAddress, password, ServerSettings.Type.IMAP, receiveUrl, sendUrl, isSSLReceived, isSSLSend, receivePort, sendPort);
//                }
//                checkedState = checking;
//                CheckEmailAccountTask c = new CheckEmailAccountTask();
//                c.execute();
//            }

            if (emailAddress != null && sendUrl != null && receiveUrl != null) {
                // only use IMAP here
                account = AccountCreator.createAccount(context, emailAddress, password, ServerSettings.Type.IMAP, receiveUrl, sendUrl, isSSLReceived, isSSLSend, receivePort, sendPort);
            }

            CheckEmailAccountTask c = new CheckEmailAccountTask();
            c.execute();
        }


    }

    private static void afterCheckSuccessful() {
        enter = true;
//        getUnread();
        if(!refreshThread.isAlive()){
            refreshThread.start();
        }
    }


//    public static void getUnread() {
//        try {
//            LocalFolder localFolder = getFolder(Account.INBOX, account);
//            Intent intent = new Intent();
//            intent.setAction(MyEmailBroadcast.UNREAD_EMAIL);
//            intent.putExtra("unread", localFolder.getUnreadMessageCount());
//            context.sendBroadcast(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static LocalFolder getFolder(String folderName, Account account) throws MessagingException {
        LocalStore localStore = account.getLocalStore();
        LocalFolder localFolder = localStore.getFolder(folderName);
        localFolder.open(Folder.OPEN_MODE_RO);
        return localFolder;
    }

    public static void tryEnter() {
        if (!enter) {
            System.out.println("need check");
            if (checkedState == checked || null == account) {
                Intent intent = new Intent(context,EmailLoginActivity.class);
                intent.putExtra("emailAddress",emailAddress);
                context.startActivityForResult(intent, MailboxEntry.CHECKLOGIN_REQUESTCODE);
            } else {
                showCheckUI();
            }
        } else {
            System.out.println("not check");
            enter();
            needSendBroadcast = false;
            CheckEmailAccountTask();
        }

    }


    private static void showCheckUI() {
        Toast.makeText(context, R.string.try_into_mailbox, Toast.LENGTH_SHORT).show();
    }

    // get account's data from UI ,only for develop
    @SuppressWarnings("unused")
    public static void tryEnter(Account account) {
        MailboxEntry.account = account;
        if (enterFirstTime) {
            CheckEmailAccountTask c = new CheckEmailAccountTask();
            c.execute();
        } else {
            enter();
        }
    }

    private static void enter() {
        MailboxController.setServicesEnabled(context);
        Intent intent = new Intent(context, EmailListActivity.class);
        intent.putExtra("first", true);
        context.startActivity(intent);
    }

    private static void handleCertificateValidationException(CertificateValidationException cve) {
        X509Certificate[] chain = cve.getCertChain();
        // Avoid NullPointerException in acceptKey()
        if (chain != null) {
            acceptKey(cve);
        } else {
            showError();
        }
    }

    private static void acceptKey(CertificateValidationException cve) {
        final X509Certificate[] chain = cve.getCertChain();
        try {
            MailboxEntry.account.addCertificate(checkDirection, chain[0]);
        } catch (CertificateException e) {
            Toast.makeText(context, R.string.CA_error, Toast.LENGTH_SHORT).show();
        }
    }

    private static void showError() {
//        mHandler.post(new Runnable() {
//            public void run() {
//                Toast.makeText(context, R.string.account_setup_error, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public static void toOrgActivity(Activity activity, int code) {
        Intent intent = new Intent(activity, OrgActivity.class);
        intent.putExtra("TITLE", "选择收件人");
        intent.putExtra("IS_FROM_ORG", "N");
        intent.putExtra("ACTION", "NativeEmailAction");
        activity.startActivityForResult(intent, code);
    }

    public static void destroy() {
        if (refreshThread != null) {
            refreshThread.stopThread();
        }
    }

}
