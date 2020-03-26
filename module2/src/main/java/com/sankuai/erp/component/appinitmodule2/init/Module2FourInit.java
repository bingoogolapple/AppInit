package com.sankuai.erp.component.appinitmodule2.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;

@AppInit(priority = 40, aheadOf = "cn.bingoogolapple:appinit-test-module3:Module3FourInit", description = "模块24的描述")
public class Module2FourInit extends SimpleAppInit {

    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
    }
}
