package com.sankuai.erp.component.appinit.api;

import com.sankuai.erp.component.appinit.common.AppInitCallback;
import com.sankuai.erp.component.appinit.common.AppInitItem;
import com.sankuai.erp.component.appinit.common.ChildInitTable;

import java.util.List;
import java.util.Map;

/**
 * 作者:王浩
 * 创建时间:2018/11/15
 * 描述:
 */
public abstract class SimpleAppInitCallback implements AppInitCallback {
    @Override
    public void onInitStart(boolean isMainProcess, String processName) {
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public Map<String, String> getCoordinateAheadOfMap() {
        return null;
    }

    @Override
    public void onInitFinished(boolean isMainProcess, String processName, List<ChildInitTable> childInitTableList, List<AppInitItem> appInitItemList) {
    }
}
