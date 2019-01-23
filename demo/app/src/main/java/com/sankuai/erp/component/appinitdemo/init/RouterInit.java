package com.sankuai.erp.component.appinitdemo.init;

import com.sankuai.erp.component.appinit.api.SimpleAppInit;
import com.sankuai.erp.component.appinit.common.AppInit;
import com.sankuai.erp.component.appinit.common.AppInitLogger;
import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.common.DefaultRootUriHandler;

@AppInit(priority = 40, description = "初始化路由")
public class RouterInit extends SimpleAppInit {

    @Override
    public void onCreate() {
        AppInitLogger.demo("onCreate " + TAG);
        // SimpleAppInit 中包含了 mApplication 和 mIsDebug 属性，可以直接在子类中使用
        Router.init(new DefaultRootUriHandler(mApplication));
    }
}
