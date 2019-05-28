package com.joyshebao.download_apk;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by zhangpengju on 2019/5/23.
 */

public class DownloadApkUtil {

    public static void testUtils(){
        Log.e(DownloadApkUtil.class.getSimpleName(),"-------------------------------->");





    }




    public static void installApk(Context context, Intent installIntent, Uri uri){

        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setAction(Intent.ACTION_VIEW);

        installIntent.setDataAndType(uri,"application/vnd.android.package-archive");
        context.startActivity(installIntent);

    }


    /**
     * 适配8.0 安装权限适配
     * @param context
     * @param installIntent
     * @param uri
     */
    public static void installApk28(Context context, Intent installIntent, Uri uri) {

        boolean isInstallPermission = false;//是否有8.0安装权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if (isInstallPermission) {
                installApk(context, installIntent, uri);
            } else {
                Uri packageURI = Uri.parse("package:" + context.getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        } else {
            installApk(context, installIntent, uri);
        }


    }


    //通过downLoadId查询下载的apk，解决6.0以后安装的问题
    public static File queryDownloadedApk(Context context, long downloadId) {
        File targetApkFile = null;
        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloader.query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetApkFile;
    }


    /**
     * 获取未安装的apk  版本名称
     * @param context
     * @param apkPath
     * @return
     */
    public static String getApkVersion(Context context,String apkPath){

        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo archiveInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            return archiveInfo.versionName == null ? "0": archiveInfo.versionName;
        }catch (Exception e){

        }

        return "0";



    }


    private static String getCurrentVersion(Context context){

        PackageManager packageManager = context.getPackageManager();
        String version = null;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }



    public static void startAutoUpdate(Context context,DownloadAPKManager downloadAPKManager,String targetApkVersion){

        String apkPath = downloadAPKManager.getApkPath();

        if(apkPath == null){
            downloadAPKManager.startDowload();
        }else {

            String apkVersion = getApkVersion(context, apkPath);


            if (apkVersion.equals(getCurrentVersion(context))  || !apkVersion.equals(targetApkVersion)) {
                downloadAPKManager.deleteApk();
                downloadAPKManager.startDowload();
                return;
            }


            //执行安装

            File file = new File(apkPath);
            Uri uri = null;
            Intent installIntent = new Intent();

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){//6.0以下安装
                uri = Uri.fromFile(file);
            }else {
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(context,"com.joyshebao.download_apk.FileProvider",file);
                DownloadApkUtil.installApk(context,installIntent,uri);
            }

            installApk(context,installIntent,uri);

        }

    }


    public static void cleanOldApk(Context context,DownloadAPKManager downloadAPKManager){

        String apkPath = downloadAPKManager.getApkPath();
        if(apkPath == null)return;

        String apkVersion = getApkVersion(context, apkPath);

        if (apkVersion.equals(getCurrentVersion(context))) {
            downloadAPKManager.deleteApk();
        }
    }


}
