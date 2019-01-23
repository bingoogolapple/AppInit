package com.sankuai.erp.component.appinitmodule2.init

import com.sankuai.erp.component.appinit.api.SimpleAppInit
import com.sankuai.erp.component.appinit.common.AppInit
import com.sankuai.erp.component.appinit.common.AppInitLogger

/**
 * 作者:王浩
 * 创建时间:2018/11/28
 * 描述:
 */
@AppInit(priority = 70, description = "模块22的描述")
class Module2TwoInit : SimpleAppInit() {

    override fun onCreate() {
        AppInitLogger.demo("onCreate $TAG")
    }
}