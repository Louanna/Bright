package com.movit.platform.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.androidquery.util.AQUtility;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;

import java.io.File;

/**
 * Created by Louanna.Lu on 2015/10/10.
 */
public class ConfigUtils {

    public static void initConfigInfo(Context context) {

        try {
            String ip = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_IP");
            String port = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_PORT");
            String cmsPort = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_CMS_PORT");

            MFSPHelper.setString("ip", ip);
            MFSPHelper.setString("port", port);
            MFSPHelper.setString("cmsPort", cmsPort);
            String type = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_TYPE");

            CommConstants.HOST_TYPE = type;
            CommConstants.ORG_TREE = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_ORG_TREE");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommConstants.initHost(context);
        File cacheDir = new File(CommConstants.SD_DATA, "pic");
        AQUtility.setCacheDir(cacheDir);
        AQUtility.setDebug(true);
    }
}
