package com.joyshebao.download_apk;

import android.database.ContentObserver;
import android.os.Handler;

/**
 *
 * 下载监听
 * Created by zhangpengju on 2019/5/23.
 */

public class DownloadChangeObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DownloadChangeObserver(Handler handler) {
        super(handler);
    }


    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);


    }
}
