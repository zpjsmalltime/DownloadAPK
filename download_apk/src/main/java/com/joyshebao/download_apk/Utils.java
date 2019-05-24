package com.joyshebao.download_apk;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by zhangpengju on 2019/5/23.
 */

public class Utils {

    public static void testUtils(){
        Log.e(Utils.class.getSimpleName(),"-------------------------------->");





    }



    public static void installApk(Context context, Intent installIntent, Uri uri){

        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setAction(Intent.ACTION_VIEW);

        installIntent.setDataAndType(uri,"application/vnd.android.package-archive");
        context.startActivity(installIntent);

    }





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
}
