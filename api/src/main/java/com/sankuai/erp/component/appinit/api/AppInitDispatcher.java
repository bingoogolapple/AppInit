package com.sankuai.erp.component.appinit.api;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Looper;

import com.sankuai.erp.component.appinit.common.AppInitCommonUtils;
import com.sankuai.erp.component.appinit.common.AppInitItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 作者:王浩
 * 创建时间:2018/10/30
 * 描述:
 */
final class AppInitDispatcher {
    private static final int CONST_100 = 100;
    private List<AppInitItem> mAppInitItemList;
    private boolean mIsMainProcess;

    private BlockingQueue<AppInitItem> mAsyncOnCreateQueuedInit;
    private volatile boolean mAsyncOnCreateQueuedInitFinished;

    private BlockingQueue<AppInitItem> mLazyAsyncOnCreateQueuedInit;
    private volatile boolean mLazyAsyncOnCreateQueuedInitFinished;
    private Queue<AppInitItem> mLazyAppInitItemList;

    AppInitDispatcher(List<AppInitItem> appInitItemList) {
        mAppInitItemList = appInitItemList;
        mLazyAppInitItemList = new LinkedList<>();
        mIsMainProcess = AppInitApiUtils.isMainProcess();
    }

    /**
     * 懒初始化列表是否不为空
     */
    private boolean isLazyAppInitNotEmpty() {
        return mLazyAppInitItemList != null && !mLazyAppInitItemList.isEmpty();
    }

    /**
     * 初始化列表是否为空
     */
    private boolean isAppInitEmpty() {
        return mAppInitItemList == null || mAppInitItemList.isEmpty();
    }

    /**
     * 是否忽略该初始化
     */
    private boolean isIgnoreDispatch(AppInitItem appInitItem) {
        if (appInitItem.onlyForDebug && !AppInitManager.get().isDebug()) {
            return true;
        }
        boolean dispatch = (mIsMainProcess && appInitItem.isForMainProcess()) || (!mIsMainProcess && appInitItem.isNotForMainProcess());
        return !dispatch;
    }

    private boolean lazyOnCreate() {
        if (isLazyAppInitNotEmpty()) {
            if (mLazyAsyncOnCreateQueuedInit == null) {
                mLazyAsyncOnCreateQueuedInit = new ArrayBlockingQueue<>(mLazyAppInitItemList.size());
                AsyncTask.THREAD_POOL_EXECUTOR.execute(this::lazyAsyncOnCreate);
            }

            AppInitItem appInitItem = mLazyAppInitItemList.poll();
            if (appInitItem != null && !isIgnoreDispatch(appInitItem)) {
                appInitItem.time += AppInitCommonUtils.time(appInitItem.toString() + " lazyOnCreate ", () -> appInitItem.appInit.onCreate());
                if (appInitItem.appInit.needAsyncInit()) {
                    mLazyAsyncOnCreateQueuedInit.add(appInitItem);
                }
            }
        }
        if (isLazyAppInitNotEmpty()) {
            return true;
        } else {
            mLazyAsyncOnCreateQueuedInitFinished = true;
            return false;
        }
    }

    private void lazyAsyncOnCreate() {
        AppInitItem appInitItem;
        try {
            while (true) {
                appInitItem = mLazyAsyncOnCreateQueuedInit.poll(CONST_100, TimeUnit.MILLISECONDS);
                if (appInitItem == null) {
                    if (mLazyAsyncOnCreateQueuedInitFinished) {
                        break;
                    } else {
                        continue;
                    }
                }

                lazyDispatchAsyncOnCreate(appInitItem);

                if (mLazyAsyncOnCreateQueuedInitFinished && mLazyAsyncOnCreateQueuedInit.isEmpty()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void lazyDispatchAsyncOnCreate(AppInitItem appInitItem) {
        if (isIgnoreDispatch(appInitItem)) {
            return;
        }
        AppInitCommonUtils.time(appInitItem.toString() + " lazyAsyncOnCreate ", () -> appInitItem.appInit.asyncOnCreate());
    }

    /**
     * 在 {@link Application#onCreate()} 中调用
     */
    void onCreate() {
        if (isAppInitEmpty()) {
            return;
        }
        mAsyncOnCreateQueuedInit = new ArrayBlockingQueue<>(mAppInitItemList.size());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(this::asyncOnCreate);
        dispatch(appInitItem -> {
            if (appInitItem.lazyInit) {
                mLazyAppInitItemList.add(appInitItem);
            } else {
                appInitItem.time += AppInitCommonUtils.time(appInitItem.toString() + " onCreate ", () -> appInitItem.appInit.onCreate());
                if (appInitItem.appInit.needAsyncInit()) {
                    mAsyncOnCreateQueuedInit.add(appInitItem);
                }
            }
        });
        mAsyncOnCreateQueuedInitFinished = true;
    }

    private void asyncOnCreate() {
        AppInitItem appInitItem;
        try {
            while (true) {
                appInitItem = mAsyncOnCreateQueuedInit.poll(CONST_100, TimeUnit.MILLISECONDS);
                if (appInitItem == null) {
                    if (mAsyncOnCreateQueuedInitFinished) {
                        break;
                    } else {
                        continue;
                    }
                }

                dispatchAsyncOnCreate(appInitItem);

                if (mAsyncOnCreateQueuedInitFinished && mAsyncOnCreateQueuedInit.isEmpty()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dispatchAsyncOnCreate(AppInitItem appInitItem) {
        if (isIgnoreDispatch(appInitItem)) {
            return;
        }
        AppInitCommonUtils.time(appInitItem.toString() + " asyncOnCreate ", () -> appInitItem.appInit.asyncOnCreate());
    }

    /**
     * 在 {@link Application#onTerminate()} 中调用
     */
    void onTerminate() {
        dispatch(appInitItem -> AppInitCommonUtils.time(appInitItem.toString() + " onTerminate ", () -> appInitItem.appInit.onTerminate()));
    }

    /**
     * 在 {@link Application#onConfigurationChanged(Configuration)} 中调用
     *
     * @param newConfig The new device configuration.
     * @see ComponentCallbacks#onConfigurationChanged(Configuration)
     */
    void onConfigurationChanged(Configuration newConfig) {
        dispatch(appInitItem -> AppInitCommonUtils.time(appInitItem.toString() + " onConfigurationChanged ",
                () -> appInitItem.appInit.onConfigurationChanged(newConfig)));
    }

    /**
     * 在 {@link Application#onLowMemory()} 中调用
     *
     * @see ComponentCallbacks#onLowMemory()
     */
    void onLowMemory() {
        dispatch(appInitItem -> AppInitCommonUtils.time(appInitItem.toString() + " onLowMemory ", () -> appInitItem.appInit.onLowMemory()));
    }

    /**
     * 在 {@link Application#onTrimMemory(int)} 中调用
     *
     * @param level The context of the trim, giving a hint of the amount of trimming the application may like to perform.
     * @see ComponentCallbacks2#onTrimMemory(int)
     */
    void onTrimMemory(int level) {
        dispatch(appInitItem -> AppInitCommonUtils.time(appInitItem.toString() + " onTrimMemory ", () -> appInitItem.appInit.onTrimMemory(level)));
    }

    /**
     * 延迟初始化
     */
    void startLazyInit() {
        Looper.myQueue().addIdleHandler(this::lazyOnCreate);
    }

    private void dispatch(DispatchCallback dispatchCallback) {
        if (isAppInitEmpty()) {
            return;
        }
        for (AppInitItem appInitItem : mAppInitItemList) {
            if (isIgnoreDispatch(appInitItem)) {
                continue;
            }
            dispatchCallback.dispatch(appInitItem);
        }
    }

    private interface DispatchCallback {
        void dispatch(AppInitItem appInitItem);
    }
}
