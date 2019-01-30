package com.sankuai.erp.component.appinitmodule3.init.lazy

import com.sankuai.erp.component.appinit.api.SimpleAppInit
import com.sankuai.erp.component.appinit.common.AppInit
import com.sankuai.erp.component.appinit.common.AppInitLogger

/**
 * 作者:王浩
 * 创建时间:2019/1/30
 * 描述:
 */
@AppInit(priority = 60, description = "模块3LazyOne的描述", lazyInit = true)
class Module3LazyOneInit : SimpleAppInit() {
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