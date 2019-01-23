package com.sankuai.erp.component.appinitmodule1.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.Process;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(process = Process.OTHER, priority = 90, description = "模块12的描述")
public class Module1TwoInit extends SimpleAppInit {

    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
