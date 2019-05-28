package com.joyshebao.download_apk;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;


/**
 *
 * 下载管理者
 * Created by zhangpengju on 2019/5/23.
 */

public class DownloadAPKManager {


    private Context context;


    /**
     * 下载任务id
     */
    private long id;

    private String urlHttp;
    private String title;
    private boolean isOnlyWifi;
    private String apkName;

    private DownloadManager downloadManager;
    private DownloadManager.Request request;


    private DownloadChangeObserver downloadChangeObserver;

    private final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");

    private DownloadReceiver completeReceiver;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };



    private DownloadAPKManager(Context context){
        this.context = context;

    }


    public static DownloadAPKManager getManager(Context context) {
        return new DownloadAPKManager(context);
    }


    public void config(String urlHttp, String title, boolean isOnlyWifi, String apkName){
        this.urlHttp = urlHttp;
        this.title = title;
        this.isOnlyWifi = isOnlyWifi;
        this.apkName = apkName;

        initLoadManager();
    }

    @SuppressLint("ServiceCast")
    private void initLoadManager() {

        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);


        request = new DownloadManager.Request(Uri.parse(urlHttp));
        request.setTitle(title);

        if(!isOnlyWifi){
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        }else{
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        }


        request.setAllowedOverRoaming(false);

        request.setMimeType("application/vnd.android.package-archive");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


        request.allowScanningByMediaScanner();

//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);

        request.setDestinationInExternalFilesDir(context,null,apkName);


        Utils.cleanOldApk(context,this);

    }


    /**
     * 获取下载到的apk  不存在为null
     * @return
     */
    public String getApkPath(){

        File file = new File(context.getExternalFilesDir(null),apkName);
        if(file.exists()){
            return file.getAbsolutePath();
        }


        return null;
    }



    public boolean deleteApk(){

        File file = new File(context.getExternalFilesDir(null),apkName);
        if(file.exists()){
            return file.delete();
        }

        return true;

    }


    public void startDowload(){
        if(downloadManager == null || urlHttp == null)return;

        try {
            id = downloadManager.enqueue(request);
        }catch (Exception e){
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }finally {
            downloadChangeObserver = new DownloadChangeObserver(handler);
            context.getContentResolver().registerContentObserver(CONTENT_URI,true,downloadChangeObserver);


            completeReceiver = new DownloadReceiver(downloadManager,id,apkName);

            context.registerReceiver(completeReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        }
    }


    public void unregister(){
        if(downloadChangeObserver != null){
            context.getContentResolver().unregisterContentObserver(downloadChangeObserver);
            context.unregisterReceiver(completeReceiver);
        }
    }

}
