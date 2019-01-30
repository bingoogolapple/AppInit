package com.sankuai.erp.component.appinitmodule2.init.lazy

import com.sankuai.erp.component.appinit.api.SimpleAppInit
import com.sankuai.erp.component.appinit.common.AppInit
import com.sankuai.erp.component.appinit.common.AppInitLogger

/**
 * 作者:王浩
 * 创建时间:2019/1/30
 * 描述:
 */
@AppInit(priority = 70, description = "模块2LazyTwo的描述", lazyInit = true, aheadOf = "com.sankuai.erp.component:appinit-test-module3:Module3LazyOneInit")
class Module2LazyTwoInit : SimpleAppInit() {
    override fun onCreate() {
        AppInitLogger.demo("onCreate $TAG")
    }

    override fun needAsyncInit(): Boolean {
        return true
    }

    override fun asyncOnCreate() {
        AppInitLogger.demo("asyncOnCreate $TAG")
    }
}