package com.sankuai.erp.component.appinitmodule1.init

import com.sankuai.erp.component.appinit.api.SimpleAppInit
import com.sankuai.erp.component.appinit.common.AppInit
import com.sankuai.erp.component.appinit.common.AppInitLogger

/**
 * 作者:王浩
 * 创建时间:2018/11/28
 * 描述:
 */
@AppInit(priority = 300, description = "模块13的描述")
class Module1ThreeInit : SimpleAppInit() {

    override fun onCreate() {
        AppInitLogger.demo("onCreate $TAG")
    }
}