package com.sankuai.erp.component.appinitmodule3;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(priority = 60, description = "初始化 xiaomi 的描述")
public class XiaomiInit extends SimpleAppInit {
    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
