package com.sankuai.erp.component.appinitmodule2.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;
import com.sankuai.erp.component.appinit.common.Process;

@AppInit(process = Process.ALL, priority = 80, description = "模块21的描述")
public class Module2OneInit extends SimpleAppInit {
    @Override
    public boolean needAsyncInit() {
        return true;
    }

    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }

    @Override
    public void asyncOnCreate() {
        AppInitLogger.demo("asyncOnCreate " + TAG);
    }
}
