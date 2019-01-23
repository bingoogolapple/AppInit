package com.sankuai.erp.component.appinitmodule3.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(priority = 10, description = "模块34的描述")
public class Module3FourInit extends SimpleAppInit {

    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
