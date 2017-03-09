package com.movit.platform.im.constants;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.im.R;
import com.movit.platform.im.broadcast.SystemReceiver;
import com.movit.platform.im.module.group.entities.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMConstants {

	public static SystemReceiver.CallBack sysCallback;

	public static List<MessageBean> contactListDatas = new ArrayList<MessageBean>();

	public static String DOWNLOAD_MEETING_APK = CommConstants.URL_DOWNLOAD+"/eoopapp/android/IBM_Meeting.apk";;

	public static final String NUM_GET_GROUP_CHAT_MESSAGE_EVERY_TIME = "20";// 每次进入群聊界面从服务器拉取的数据条数
	public static final String NUM_GET_SINGLE_CHAT_MESSAGE_EVERY_TIME = "20";// 每次进入单聊界面从服务器拉取的数据条数

	public static final int REQUEST_CODE_MEMBER = 10001;
	public static final int REQUEST_CODE_RENAME_GROUP = 10002;
	public static final int REQUEST_CODE_RESEND_MES = 10003;

	public static final String KEY_MEMBER = "key_member";
	public static final String KEY_GROUP = "key_group";
	public static final String KEY_GROUP_NAME = "key_group_name";

	public static Map<String,String> ansGroupMembers = new HashMap<String,String>();

	public static Map<String,UserInfo> atMembers = new HashMap<String,UserInfo>();

	public static String CHATTING_ID = "";
	public static String CHATTING_TYPE = "";

	public static List<Group> groupListDatas = new ArrayList<Group>();
	//Key：groupName
	public static Map<String, Group> groupsMap = new HashMap<String, Group>();
	public static ArrayList<MessageBean> failedMsgList = new ArrayList<MessageBean>();
	public static ArrayList<MessageBean> sysMsgList = new ArrayList<>();

	private static long currentTime = 0;

	public static void Dingdong(Context context) {
		long now = System.currentTimeMillis();
		if (now - currentTime > 1000) {
			try {
				Vibrator v = (Vibrator) context
						.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(300);
				MediaPlayer.create(context, R.raw.office).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			currentTime = System.currentTimeMillis();
		}
	}

}
