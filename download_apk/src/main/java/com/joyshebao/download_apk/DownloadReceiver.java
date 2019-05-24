package com.joyshebao.download_apk;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 *
 * 下载完成后点击事件 跳转安装
 * Created by zhangpengju on 2019/5/23.
 */

public class DownloadReceiver extends BroadcastReceiver {


    private DownloadManager downloadManager;

    private long id;

    private String apkName;
    public DownloadReceiver(DownloadManager downloadManager,long id,String apkName){
        this.downloadManager = downloadManager;
        this.id = id;
        this.apkName = apkName;

    }
    @Override
    public void onReceive(Context context, Intent intent) {

        //获取数据
        long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);


        Intent installIntent = new Intent();
        Uri  uri = null;

        if(completeDownloadId == id){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){//6.0以下安装

                uri = downloadManager.getUriForDownloadedFile(id);
                Utils.installApk(context,installIntent,uri);

            }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){

                File file = Utils.queryDownloadedApk(context, completeDownloadId);
                uri = Uri.fromFile(file);
                Utils.installApk(context,installIntent,uri);
            }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),apkName);

                File file = new File(context.getExternalFilesDir(null),apkName);

                uri = FileProvider.getUriForFile(context,"com.joyshebao.download_apk.FileProvider",file);
                Utils.installApk(context,installIntent,uri);

            }else{
                installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),apkName);

                File file = new File(context.getExternalFilesDir(null),apkName);


                uri = FileProvider.getUriForFile(context,"com.joyshebao.download_apk.FileProvider",file);

//                Utils.installApk28(context,installIntent,uri);
                Utils.installApk(context,installIntent,uri);
            }

        }



    }
}
