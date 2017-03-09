package com.movit.platform.sc.constants;

import com.movit.platform.common.constants.CommConstants;

/**
 * Created by Louanna.Lu on 2015/11/10.
 */
public class SCConstants {

    private static String URL_SC = CommConstants.HTTP_API_URL + "/eoop-sc/sc/rest/";
    public static String GET_ZONE_LIST_DATA = URL_SC + "getdata";
    public static String PUBLISH_ZONE_SAY = URL_SC + "say";
    public static String GET_ZONE_SAY = URL_SC + "getsay";
    public static String GET_MINE_SAY_COUNT = URL_SC + "mysaycount";
    public static String GET_MINE_SAY_LIST = URL_SC + "mysaylist";
    public static String DELETE_MINE_SAY = URL_SC + "saydel";
    public static String ZONE_NICE = URL_SC + "nice";
    public static String ZONE_COMMENT = URL_SC + "comment";
    public static String DELETE_COMMENT = URL_SC + "commentdel";
    public static String ZONE_NEW_MESSAGE = URL_SC + "havenew";
    public static String ZONE_MESSAGE = URL_SC + "messages";
    public static String ZONE_MESSAGE_COUNT = URL_SC + "messagecount";
    public static String ZONE_MESSAGE_DELETE = URL_SC + "messagedel";

    public static final String ZONE_PIC = "zonePic";

    public static final int ZONE_LIST_RESULT = 1;
    public static final int ZONE_MORE_RESULT = 2;
    public static final int ZONE_GET_RESULT = 1;
    public static final int ZONE_CLICK_AVATAR = 3;
    public static final int ZONE_NICE_RESULT = 4;
    public static final int ZONE_CLICK_COMMENT = 5;
    public static final int ZONE_COMMENT_RESULT = 6;
    public static final int ZONE_MESSAGE_COUNT_RESULT = 7;
    public static final int ZONE_MSG_DEL_RESULT = 8;
    public static final int ZONE_MY_SAY_COUNT_RESULT = 9;
    public static final int ZONE_SAY_DEL_RESULT = 10;
    public static final int ZONE_NEW_SAY_COUNT_RESULT = 11;
    public static final int ZONE_CLICK_COMMENT_TO_DEL = 12;
    public static final int ZONE_COMMENT_DEL_RESULT = 13;
    public static final int ZONE_ERROR_RESULT = 99;
    public static final int ZONE_ERROR_NO_SAY = 98;

    public static final int REQUEST_CODE_DELETE_VIDEO_IMAGE = 100;

    public static void initSCApiUrl() {
        URL_SC = CommConstants.HTTP_API_URL + "/eoop-sc/sc/rest/";
        GET_ZONE_LIST_DATA = URL_SC + "getdata";
        PUBLISH_ZONE_SAY = URL_SC + "say";
        GET_ZONE_SAY = URL_SC + "getsay";
        GET_MINE_SAY_COUNT = URL_SC + "mysaycount";
        GET_MINE_SAY_LIST = URL_SC + "mysaylist";
        DELETE_MINE_SAY = URL_SC + "saydel";
        ZONE_NICE = URL_SC + "nice";
        ZONE_COMMENT = URL_SC + "comment";
        DELETE_COMMENT = URL_SC + "commentdel";
        ZONE_NEW_MESSAGE = URL_SC + "havenew";
        ZONE_MESSAGE = URL_SC + "messages";
        ZONE_MESSAGE_COUNT = URL_SC + "messagecount";
        ZONE_MESSAGE_DELETE = URL_SC + "messagedel";
    }
}
