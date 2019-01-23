package com.sankuai.erp.component.appinitmodule1.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(priority = 20, description = "模块15的描述")
public class Module1FiveInit extends SimpleAppInit {
    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
