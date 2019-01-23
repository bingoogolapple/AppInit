package com.sankuai.erp.component.appinitmodule1.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(priority = 300, description = "模块14的描述")
public class Module1FourInit extends SimpleAppInit {
    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
