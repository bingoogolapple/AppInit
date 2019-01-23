package com.sankuai.erp.component.appinitdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sankuai.erp.component.appinit.common.AppInitLogger;

/**
 * 作者:王浩
 * 创建时间:2018/10/25
 * 描述:
 */
public class AppService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        AppInitLogger.demo("AppService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppInitLogger.demo("AppService onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
