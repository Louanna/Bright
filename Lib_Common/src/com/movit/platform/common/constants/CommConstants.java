package com.movit.platform.common.constants;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommConstants {

    //和OA对接需要用到的token
    public static String ACCESSTOKEN = "";
    public static int OAUNREADNUM = 0;
    public static int ERPUNREADNUM = 0;

    public static String OATODOTOTAL = "0";
    public static String OADONETOTAL = "0";

    public static Map<String,String> BPIURL = new HashMap<>();

    //是否登录EOP服务器
    public static boolean IS_LOGIN_EOP_SERVER;
    public static String URL_EOP_ADMIN;
    public static String URL_EOP_API;
    public static String URL_EOP_NEWS;
    public static String URL_EOP_DMS;
    public static String URL_EOP_IM;

    /**************Common****************/

    public static LoginInfo loginConfig;
    public static List<UserInfo> allUserInfos;
    public static List<OrganizationTree> allOrgunits;
    //标记获取完毕，在tab页中需要先判断是否获取完毕，获取完毕再显示页面内容，否则loading
    public static boolean GET_ATTENTION_FINISH = false;

    public static String URL_STUDIO;
    public static String URL_DOWN;
    public static String URL_DOWN_FILE;
    public static String URL_DOWN_PIC;

    public static String SD_DOWNLOAD;

    public static String CZ_URL;

    public static final String AVATAR = "avatar";
    public static final String EMPADNAME = "empAdname";
    public static final String ACTION_GROUP_LIST_RESPONSE = "action.group.list.response";//群组列表接口返回
    public static final String ACTION_GROUP_MEMBERS_CHANGES = "action.group.members.changes";
    public static final String ACTION_GROUP_DISPALYNAME_CHANGES = "action.group.displayname.changes";
    public static final String ACTION_GROUP_DISSOLVE_CHANGES = "action.group.dissolve.changes";

    /**************Common****************/

    /**************IM****************/
    public static Map<String, Integer> mFaceMap = new LinkedHashMap<String, Integer>();
    public static Map<String, Integer> mFaceGifMap = new LinkedHashMap<String, Integer>();
    public static int NUM = 20;// 每页20个表情,还有最后一个删除button
    public static final int NUM_PAGE = 7;// 总共有多少页

    /**************IM****************/

    public static final String SD_CARD = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    // 注：小米2s只能放在根目录下，其他手机没关系
    public static final String IMAGE_FILE_LOCATION = "file:///sdcard/temp.jpg";// temp
    // file

    public static String HOST_PATH = "/im/";
    public static String SD_CARD_MYPHOTOS = SD_CARD + "/盟拓相册/";
    public static String SD_CARD_IMPICTURES;
    public static String SD_CARD_IM_VIDEO;//视频

    public static String SD_CARD_IM;
    public static String SD_DOCUMENT;
    public static String SD_DATA;
    public static String SD_DATA_PIC;
    public static String SD_DATA_AUDIO;
    public static String SD_DATA_VIDEO;
    public static String SD_DATA_FILE;

    public static int PORT;

    public static String HTTP_API_URL;
    public static String URL;
    public static String BASE_URL = "";

    public static String CLOUD_URL = "";
    public static String OA_URL = "";
    public static String SSO_URL = "";

    public static String URL_INTERNALEA = "";
    public static String URL_PUNCHCARD = "";
    public static String URL_GET_PUNSH_RECORD = "";
    public static String URL_XMPP = "";
    public static String HOST_PORT = "";
    public static String HOST_CMS_PORT = "";
    public static String HOST_TYPE = "";
    public static boolean ORG_TREE = false;
    public static boolean IS_CAS = false;
    public static boolean CLEAR_MDM = false;
    public static boolean ALLOW_CONFIGURATION = false;

    public static String URL_UPLOAD;
    public static String URL_DOWNLOAD;
    public static String DOWNLOAD_URL;
    public static String APP_DOWNLOAD_URL;

    public static String URL_EOP_REGISTER;
    public static String URL_EOP_TIMESHEET;
    public static String URL_EOP_COMPANY;
    public static String URL_EOP_SUGGESTION;
    public static String URL_EOP_ATTENDANCE;

    public static String URL_GET_EMAIL_INFO;
    public static String URL_GET_EMAIL_TYPE;
    public static String URL_GET_KNOWLEDGE;
    public static String URL_SAVE_KNOWLEDGE_LIKE;
    public static String URL_DEL_KNOWLEDGE_LIKE;
    public static String URL_SAVE_KNOWLEDGE_COMMENT;

    public static String URL_BDODOWNLOAD;
    public static String URL_EVENTS;
    public static String URL_WORK_TABLE;

    public static String URL_LOGIN_VERIFY;//
    public static String URL_UPDATE_PASSWORD;//更新密码
    public static String URL_CHECK_LICENSE;//检查license是否过期
    public static String URL_DELTA_ORG_LIST;//通讯录增量更新
    public static String URL_FULL_ORG_LIST;//通讯录全量更新
    public static String URL_UPLOAD_AVATAR;//更新头像
    public static String URL_ATTENTIONS;//获取关注的人和被关注的人的信息
    public static String URL_ADD_ATTENTION;//添加关注
    public static String URL_DEL_ATTENTION;//取消关注
    public static String URL_UPDATE_USER_INFO;//更新用户信息
    public static String URL_USER_BG_IMAGE;//同时圈背景图
    public static String URL_EDIT_NAME;//修改群名片
    public static String URL_UPDATE_DEVICE;//更新设备信息
    public static String URL_LOGOUT;//登出
    public static String URL_BPI;//

    public static String URL_IM_ADD_MEMBERS;//添加Group成员
    public static String URL_IM_BOWOUT;//退出Group
    public static String URL_IM_DISSOLVE;//解散Group
    public static String URL_IM_GROUP_LIST;//Group List
    public static String URL_IM_CREATE;// 创建Group
    public static String URL_IM_GROUP;// Group message
    public static String URL_IM_DEL_MEMBERS;// 删除成员

    public static String URL_MDM;

    public static final String URL_APP_HELP_DOC = "https://p.bre600708.com/bright/光明通用户手册.htm";
    public static final String UPLOAD_BPM_PIC = "http://58.210.98.46:8008/Services/Mobile/k2MobileService.asmx/SaveAttach";

    public static String versionName = "";
    public static final String INNEREA_ID="6b624859d4844dcd9a22949e198c654e";
    public static void initHost(Context context) {

        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),0);
            versionName = info.versionName;

            CLOUD_URL = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_CLOUD_IP");

            OA_URL = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("OA_URL");

            SSO_URL = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("SSO_URL");

//            URL_INTERNALEA = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
//                    .getString("CHANNEL_INTERNALEA");
            URL_XMPP = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_XMPP");
            PORT = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getInt("CHANNEL_XMPP_PORT");

            BASE_URL = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_IP");
            HOST_PORT = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_PORT");
            HOST_CMS_PORT = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_CMS_PORT");

            IS_CAS = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("IS_CAS");
            ALLOW_CONFIGURATION = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("ALLOW_CONFIGURATION");

            CLEAR_MDM = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CLEAR_MDM");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        initApiUrl();
    }

    public static void initApiUrl(){
        URL_DOWNLOAD = BASE_URL+"/app-download";

        URL_BDODOWNLOAD = URL_DOWNLOAD+"/eoopapp/ios/v4.0.2/test/";
        DOWNLOAD_URL = URL_DOWNLOAD+"/file/ANDVersion.json";
        APP_DOWNLOAD_URL = URL_DOWNLOAD+"/index.html";

        //168服务器
//        URL_DOWNLOAD = "http://download.eop.movitech.cn";
//        URL_BDODOWNLOAD = URL_DOWNLOAD+"/eoopapp/ios/v4.0.2/test/";
//        DOWNLOAD_URL = URL_DOWNLOAD+"/eoopapp/android/v" + versionName + "/" + HOST_TYPE + "/ANDVersion.json";
//        APP_DOWNLOAD_URL = URL_DOWNLOAD+"/eoop_app_publish/?type=" + HOST_TYPE + "&version=v" + versionName;
        HOST_PATH = "/im" + HOST_TYPE + "/";

		/*---配置文件路径--*/
        SD_CARD_IM = SD_CARD + HOST_PATH;
        SD_CARD_IMPICTURES = SD_CARD_IM + "EoopPictures/";
        SD_CARD_IM_VIDEO = SD_CARD_IM + "EoopVideos/";
        SD_DOWNLOAD = SD_CARD_IM + "download/";
        SD_DOCUMENT = SD_CARD_IM + "document/";
        SD_DATA = SD_CARD_IM + "data/";
        SD_DATA_PIC = SD_CARD_IM + "data/pic/";
        SD_DATA_AUDIO = SD_CARD_IM + "data/audio/";
        SD_DATA_VIDEO = SD_CARD_IM + "data/video/";
        SD_DATA_FILE = SD_CARD_IM + "data/file/";

		/*---配置接口地址--*/

        //CMS文件上传地址
        URL_UPLOAD = BASE_URL + HOST_CMS_PORT + "/eoop-cms/file/upload";
        //CMS文件下载地址
        URL_DOWN = BASE_URL + HOST_CMS_PORT + "/cmsContent/";
//        URL_DOWN_PIC = BASE_URL + HOST_CMS_PORT + "/download/cmsContent/eopBrightRealEstate";
        HTTP_API_URL = BASE_URL + HOST_PORT;

        URL_EOP_IM = HTTP_API_URL + "/eop-im/";
        URL_EOP_DMS = HTTP_API_URL + "/eoop-dms/";

        URL_EOP_API = HTTP_API_URL + "/eoop-api/";
        URL_EOP_NEWS = HTTP_API_URL + "/eoop-news/";
        URL_EOP_ADMIN = HTTP_API_URL+ "/eoop-admin/";
        URL_EOP_TIMESHEET = HTTP_API_URL + "/eoop-ts/timesheet/index?";

        URL = URL_EOP_API + "rest/";
        URL_STUDIO = URL_EOP_API + "r/sys/rest/";
        URL_EOP_REGISTER = URL_EOP_API + "a/sys/user/register";
        URL_EOP_COMPANY = URL_EOP_API + "r/product?companyId=000000000000000000000000000000000000";
        URL_EOP_SUGGESTION = URL_EOP_API + "r/advice/addAdvice";
        URL_EOP_ATTENDANCE = URL_EOP_API + "r/eattendance/";
        URL_MDM = URL_EOP_API + "r/sys/rest/unbindDevice/";

        URL_INTERNALEA = URL_EOP_API + "r/sys/punchcard/GetAllPoint";
        URL_PUNCHCARD = URL_EOP_API + "r/sys/punchcard";
        URL_GET_PUNSH_RECORD = URL_EOP_API + "r/sys/punchcard/GetPunshRecord?";
        URL_GET_EMAIL_INFO = URL_EOP_API +"r/config/getEmailConfig";
        URL_GET_EMAIL_TYPE = URL_EOP_API +"r/config/type?cfgType=email_conn_type";
        URL_GET_KNOWLEDGE = URL_EOP_API +"r/knowledge/queryKnowledgeCommentSay";
        URL_SAVE_KNOWLEDGE_LIKE = URL_EOP_API +"r/knowledge/saveKnowledgeLike";
        URL_DEL_KNOWLEDGE_LIKE = URL_EOP_API +"r/knowledge/delKnowledgeLike";
        URL_SAVE_KNOWLEDGE_COMMENT = URL_EOP_API +"r/knowledge/saveKnowledgeComment";

        URL_DOWN_FILE = URL_EOP_DMS+"services/download/document";

        CZ_URL = HTTP_API_URL + "/eoop-sc/sc/rest/";

        URL_EVENTS = HTTP_API_URL+"/eoop-studio/r/draw/luckyDraw";

        URL_WORK_TABLE = URL_EOP_API+"r/sys/sso/login";

        URL_BPI = URL_EOP_API+"r/bpiMenuInfo/selectBpiMenuInfo";

        URL_LOGIN_VERIFY = URL_STUDIO+ "loginVerify";
        URL_UPDATE_PASSWORD = URL_STUDIO+ "updateUserInfo";
        URL_CHECK_LICENSE = URL_STUDIO+ "checkLicense";
        URL_DELTA_ORG_LIST = URL_STUDIO+ "org/getDeltaOrgList?lastUpdateTime=";
        URL_FULL_ORG_LIST = URL_STUDIO + "org/getFullOrgList";
        URL_UPLOAD_AVATAR = URL_STUDIO + "setUserAvatar?userId=%1$s&avatar=%2$s";
        URL_ATTENTIONS = URL_STUDIO + "getAttentions?userid=";
        URL_ADD_ATTENTION = URL_STUDIO + "addAttention";
        URL_DEL_ATTENTION = URL_STUDIO + "delAttention";
        URL_UPDATE_USER_INFO = URL_STUDIO + "updateUserInfo";
        URL_USER_BG_IMAGE = URL_STUDIO + "getUserBgImage?userId=";

        URL_LOGOUT =  URL_STUDIO+ "logout";
        URL_UPDATE_DEVICE =  URL+ "updateDevice";
        URL_EDIT_NAME =  URL+ "im/group/edit_name";
        URL_IM_ADD_MEMBERS =  URL+ "im/group/add_members";
        URL_IM_BOWOUT =  URL+ "im/group/bowout?userId=%1$s&groupId=%2$s";
        URL_IM_DISSOLVE =  URL+ "im/group/dissolve?userId=%1$s&groupId=%2$s";
        URL_IM_GROUP_LIST =  URL+ "im/group/list?userId=%1$s&type=-1";
        URL_IM_CREATE =  URL+ "im/group/create";
        URL_IM_GROUP =  URL+ "im/group/group?userId=%1$s&groupName=%2$s";
        URL_IM_DEL_MEMBERS =  URL+ "im/group/del_members?userId=%1$s&groupId=%2$s&memberIds=%3$s";

    }

    public static String roomServerName = "@conference.";

    public static String PHONEBRAND = "";
    public static String PHONEVERSION = "";

    public static String productName = "";
    public static String companyName = "";
    public static String companyLogo = "";

    public static boolean isExit = false;
    public static boolean isGestureOK = false;
    public static boolean isCome = false;

    public static final String KEY_GROUP_TYPE = "key_group_type";
    public static final int CHAT_TYPE_SINGLE = 0;
    public static final int CHAT_TYPE_GROUP = 1;
    public static final int CHAT_TYPE_PUBLIC = 2;
    public static final int CHAT_TYPE_SYSTEM = 3;


    public static final String MARK_UNREAD_IDS = "unReadIds";
    public static final String MARK_READ_IDS = "readIds";

    //TODO anna: 0管理员创建群组 1部门群组 2任务群组 3个人群组 4匿名群组
    public static final int CHAT_TYPE_GROUP_PERSON = 3;
    public static final int CHAT_TYPE_GROUP_ANS = 4;

    public static final int MSG_READ = 1;
    public static final int MSG_UNREAD = 0;

    public static final int MSG_SEND = 1;
    public static final int MSG_RECEIVE = 0;

    //新增MSG_TYPE时，注意不要与下面已定义的重复
    public static final String KEY_MSG_TYPE = "MSG_TYPE";
    public static final String MSG_TYPE_TEXT = "T";
    public static final String MSG_TYPE_AUDIO = "A";
    public static final String MSG_TYPE_VIDEO = "V";//视频
    public static final String MSG_TYPE_PIC = "P";
    public static final String MSG_TYPE_ADMIN = "Z";
    public static final String MSG_TYPE_METTING = "M";
    public static final String MSG_TYPE_FILE_1 = "F1";//手机文件
    public static final String MSG_TYPE_FILE_2 = "F2";//文档管理文件
    public static final String MSG_TYPE_LOCATION = "L";//位置

    public static final String MSG_TYPE_KICK = "K";
    public static final String MSG_TYPE_INVITE = "I";
    public static final String MSG_TYPE_MEMBERS_CHANGE = "MC";
    public static final String MSG_TYPE_DISSOLVE = "S";

    public static final int MSG_SEND_FAIL = 0;
    public static final int MSG_SEND_SUCCESS = 1;
    public static final int MSG_SEND_PROGRESS = 2;
    public static final int MSG_SEND_RESEND = 3;

    public static final String PIC_TEXT = "[图片]";
    public static final String VOICE_TEXT = "[语音]";
    public static final String MEETING_TEXT = "[会议]";
    public static final String VIDEO_TEXT = "[视频]";
    public static final String FILE_TEXT = "[文件]";
    public static final String LOCATION_TEXT = "[位置]";

    public static final String ACTION_ORGUNITION_DONE = "action.orgunition.done";
    public static final String ACTION_LOGIN_DONE = "action.login.done";

    public static final String ACTION_MY_KICKED = "action.my.kicked";
    public static final String ACTION_MY_INVITE = "action.my.invite";
    public static final String ACTION_CONTACT_LIST = "action.contact.list";

    public static final String ACTION_NEW_MESSAGE = "action.new.message";
    public static final String ACTION_HISTORY_MESSAGE_LIST = "action.history.message.list";
    public static final String ACTION_SESSION_MESSAGE_LIST = "action.session.message.list";

    public static final String ACTION_SET_REDPOINT = "action.set.redpoint";

    public static final String MSG_UPDATE_SEND_STATUS_ACTION = "message.update.send.status.receive";

    public static final String MSG_UPDATE_UNREAD_ACTION = "message.update.unread.receive";

    public static final String SIMPLE_LOGIN_ACTION = "simple.login.receive";

    public static final String TYPE_JUST_TIPS = "1";//给出提示信息,不强制退出APP
    public static final String TYPE_LOGINOUT = "2";//强制退出APP

    public static final String GROUP_ADMIN = "group-admin";
    public static String DEVICE_ID = "";
    public static boolean IS_RUNNING = false;
    public static String IGNORE_CHECK_VERSION_CODE = "ignore_check_version_code";

    /**
     * 服务器的配置
     */
    public static final String LOGIN_SET = "movitech_login_set";// 登录设置
    public static final String USERNAME = "username";// 账户
    public static final String PASSWORD = "password";// 密码
    public static final String EMAILPASSWORD = "emailPassWord";// 密码
    public static final String IS_AUTOLOGIN = "isAutoLogin";// 是否自动登录
    public static final String IS_REMEMBER = "isRemember";// 是否记住账户密码
    public static final String IS_FIRSTSTART = "isFirstStart";// 是否首次启动
    public static final String IS_SHOW_TOUR = "isshowtour";// 是否首次启动
    public static final String ORIGINAL_VERSION = "originalversion";// 是否首次启动
    public static final String FILE_CLOUD = "isFileCloud";// 是否首次启动
    public static final String USERID = "userId";
	public static final String CALL_COUNT = "callCount";
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String MINGYUANNO = "mingyuanNo";

    public static final String EMPCNAME = "empCname";
    public static final String EMPID = "empId";
    public static final String GENDER = "gender";
    public static final String TOKEN = "openFireToken";

    /**
     * 登录提示
     */
    public static final int LOGIN_SECCESS = 0;// 成功
    public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错
    public static final int SERVER_UNAVAILABLE = 4;// 无法连接到服务器
    public static final int LOGIN_ERROR = 5;// 连接失败
    public static final int LOGIN_FAILE_REASON = 6;// 自定义连接错误提

}
