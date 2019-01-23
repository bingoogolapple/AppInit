package com.sankuai.erp.component.appinitdemo;

import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.sankuai.erp.component.appinit.api.AppInitApiUtils;
import com.sankuai.erp.component.appinit.api.AppInitManager;
import com.sankuai.erp.component.appinit.api.SimpleAppInitCallback;
import com.sankuai.erp.component.appinit.common.AppInitItem;
import com.sankuai.erp.component.appinit.common.ChildInitTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends MultiDexApplication {
    private String mInitLogInfo;

    public String getInitLogInfo() {
        return mInitLogInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // appInit 新增 START
        AppInitManager.get().init(this, new SimpleAppInitCallback() {
            /**
             * 开始初始化
             *
             * @param isMainProcess 是否为主进程
             * @param processName   进程名称
             */
            @Override
            public void onInitStart(boolean isMainProcess, String processName) {
                // TODO 初始化 MtGuard，保证在所有网络请求之前
            }

            /*
             * 是否为 debug 模式
             */
            @Override
            public boolean isDebug() {
                return BuildConfig.DEBUG;
            }

            /**
             * 通过 coordinate 自定义依赖关系映射，键值都是 coordinate。「仅在需要发热补的情况下才自定义，否则返回 null」
             *
             * @return 如果返回的 map 不为空，则会在启动是检测依赖并重新排序
             */
            @Override
            public Map<String, String> getCoordinateAheadOfMap() {
                Map<String, String> coordinateAheadOfMap = new HashMap<>();
                coordinateAheadOfMap.put("AppInit:module2:Module2FiveInit", "com.sankuai.erp.component:appinit-test-module1:Module1FiveInit");
                return coordinateAheadOfMap;
//                return null;
            }

            /**
             * 同步初始化完成
             *
             * @param isMainProcess      是否为主进程
             * @param processName        进程名称
             * @param childInitTableList 初始化模块列表
             * @param appInitItemList    初始化列表
             */
            @Override
            public void onInitFinished(boolean isMainProcess, String processName, List<ChildInitTable> childInitTableList, List<AppInitItem> appInitItemList) {
                String initLogInfo = AppInitApiUtils.getInitOrderAndTimeLog(childInitTableList, appInitItemList);
                Log.d("statisticInitInfo", initLogInfo);
                mInitLogInfo = initLogInfo;
            }
        });
        // appInit 新增 END
    }

    // 没有在 appInit 扩展中配置 applicationCanonicalName 时，需要接入方手动写以下代码

    @Override
    public void onTerminate() {
        super.onTerminate();
        // appInit 新增 START
        AppInitManager.get().onTerminate();
        // appInit 新增 END
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // appInit 新增 START
        AppInitManager.get().onConfigurationChanged(newConfig);
        // appInit 新增 END
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // appInit 新增 START
        AppInitManager.get().onLowMemory();
        // appInit 新增 END
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // appInit 新增 START
        AppInitManager.get().onTrimMemory(level);
        // appInit 新增 END
    }
}
