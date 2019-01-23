package com.sankuai.erp.component.appinitmodule2.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(priority = 10, description = "模块25的描述")
public class Module2FiveInit extends SimpleAppInit {

    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
