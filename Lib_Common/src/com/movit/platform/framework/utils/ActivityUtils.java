package com.movit.platform.framework.utils;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.util.List;

public class ActivityUtils {

    public static boolean openThirdApplicationWithPackageName(Context context,
                                                              String packagename) {
        try {
            PackageInfo packageinfo = context.getPackageManager().getPackageInfo(
                    packagename, 0);

            // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(packageinfo.packageName);

            // 通过getPackageManager()的queryIntentActivities方法遍历
            List<ResolveInfo> resolveinfoList = context.getPackageManager()
                    .queryIntentActivities(resolveIntent, 0);

            ResolveInfo resolveinfo = resolveinfoList.iterator().next();
            if (resolveinfo != null) {
                // packagename = 参数packname
                String packageName = resolveinfo.activityInfo.packageName;
                // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
                String className = resolveinfo.activityInfo.name;
                // LAUNCHER Intent
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // 设置ComponentName参数1:packagename参数2:MainActivity路径
                ComponentName cn = new ComponentName(packageName, className);

                intent.setComponent(cn);
                context.startActivity(intent);
                return true;
            } else {
//                Toast.makeText(context, "当前应用未安装！", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(context, "当前应用未安装！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void sendMail(Context context, String user) {
        try {
            Uri uri = Uri.parse("mailto:" + user);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
            // intent.putExtra(Intent.EXTRA_SUBJECT, "这是邮件的主题部分"); // 主题
            // intent.putExtra(Intent.EXTRA_TEXT, "这是邮件的正文部分"); // 正文
            context.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMails(Context context, String[] users) {

        try {

            Uri uri = Uri.parse("mailto:");
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra(Intent.EXTRA_EMAIL, users);
            // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
            // intent.putExtra(Intent.EXTRA_SUBJECT, "这是邮件的主题部分"); // 主题
            // intent.putExtra(Intent.EXTRA_TEXT, "这是邮件的正文部分"); // 正文
            context.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否具有网络连接
     *
     * @param context
     * @return
     */
    public static final boolean hasNetWorkConection(Context context) {
        // 获取连接活动管理器
        final ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取连接的网络信息
        final NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isAvailable());
    }

    public static void openWirelessSet(final Context context) {
        // 没有网络做些什么
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder
                .setTitle("提示")
                .setMessage("无可用网络连接，请检查网络设置！")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        dialogBuilder.show();
    }

    /**
     * 检查时候有sd卡
     */
    public static void checkMemoryCard(final Context context) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("请检查内存卡")
                    .setPositiveButton("设置",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                    Intent intent = new Intent(
                                            Settings.ACTION_SETTINGS);
                                    context.startActivity(intent);
                                }
                            })
                    .setNegativeButton("退出",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            }).create().show();
        }
    }

    /**
     * 判断GPS是否开启
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGPSOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
