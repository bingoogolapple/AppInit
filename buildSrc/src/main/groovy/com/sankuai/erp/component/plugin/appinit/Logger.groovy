package com.sankuai.erp.component.plugin.appinit

import com.sankuai.erp.component.appinit.common.ILogger
import org.gradle.api.Project

/**
 * 作者:王浩
 * 创建时间:2018/11/2
 * 描述:AppInit 打印 Gradle 插件日志
 */
class Logger implements ILogger {
    private Project mProject
    Logger(Project project) {
        mProject = project
    }
    @Override
    boolean isDebug() {
        return true
    }

    @Override
    boolean isIsMainProcess() {
        return true
    }

    @Override
    void demo(String msg) {
        d(msg)
    }

    @Override
    void d(String msg) {
        println "「AppInitPlugin」「${mProject.path}」=> ${msg}"
    }


    @Override
    void e(String msg) {
        mProject.logger.error "「AppInitPlugin」「${mProject.path}」=> ${msg}"
    }
}
