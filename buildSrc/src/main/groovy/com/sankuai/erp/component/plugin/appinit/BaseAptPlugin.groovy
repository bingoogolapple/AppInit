package com.sankuai.erp.component.plugin.appinit

import com.sankuai.erp.component.appinit.common.AppInitCommonUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 作者:王浩
 * 创建时间:2018/1/18
 * 描述:
 * 1.配置模块名称参数，用于在 apt 中读取
 * 2.添加依赖
 */
abstract class BaseAptPlugin implements Plugin<Project> {
    private static final String GROUP = "GROUP"
    private static final String POM_GROUP_ID = "POM_GROUP_ID"
    private static final String POM_ARTIFACT_ID = "POM_ARTIFACT_ID"
    protected Project mProject

    @Override
    void apply(Project project) {
        if (!hasAndroidPlugin(project)) {
            return
        }

        mProject = project
        configCompileOptions()
        addDependencies()

        if (hasAppPlugin(project)) {
            handleMasterModule()
        }
    }

    protected void configCompileOptions() {
        addConfigCompileOption(getAptModuleCoordinateKey(), getAptModuleCoordinate())

        String aptDependenciesKey = getAptDependenciesKey()
        if (aptDependenciesKey != null && aptDependenciesKey.length() > 0 && mProject.hasProperty(aptDependenciesKey)) {
            addConfigCompileOption(aptDependenciesKey, mProject.property(aptDependenciesKey))
        }
    }

    /**
     * 处理壳工程
     */
    protected abstract void handleMasterModule()

    /**
     * 添加编译选项
     */
    protected void addConfigCompileOption(String key, String value) {
        mProject.android.defaultConfig {
            javaCompileOptions {
                annotationProcessorOptions {
                    if (arguments == null) {
                        arguments = [(key): value]
                    } else {
                        arguments.put((key), value)
                    }
                }
            }
        }
    }

    protected abstract String getAptDebugKey()

    protected abstract String getApiPomArtifactId()

    protected abstract String getCompilerPomArtifactId()

    protected abstract String getPomVersionName()

    protected abstract String getAptModuleCoordinateKey()

    protected abstract String getAptDependenciesKey()

    /**
     * 添加依赖
     */
    private void addDependencies() {
        if (isDebugApt()) {
            info "调试 Apt"
            mProject.dependencies.add('implementation', mProject.project(':api'))
            mProject.dependencies.add('annotationProcessor', mProject.project(':compiler'))
            if (hasKotlinAndroidPlugin(mProject)) {
                mProject.dependencies.add('kapt', mProject.project(':compiler'))
            }
        } else {
            info "不调试 Apt"
            mProject.dependencies.add('implementation', "com.sankuai.erp.component:${getApiPomArtifactId()}:${getPomVersionName()}")
            mProject.dependencies.add('annotationProcessor', "com.sankuai.erp.component:${getCompilerPomArtifactId()}:${getPomVersionName()}")
            if (hasKotlinAndroidPlugin(mProject)) {
                mProject.dependencies.add('kapt', "com.sankuai.erp.component:${getCompilerPomArtifactId()}:${getPomVersionName()}")
            }
        }
    }

    private String getAptModuleCoordinate() {
        String pomCoordinate = getPomCoordinate()
        if (!AppInitCommonUtils.isEmpty(pomCoordinate)) {
            return pomCoordinate
        }
        return getAptModuleName()
    }

    private String getPomCoordinate() {
        String groupId = getProjectProperty(POM_GROUP_ID)
        String artifactId = getProjectProperty(POM_ARTIFACT_ID)
        // 兼容集团 CI 发布 maven
        if (AppInitCommonUtils.isEmpty(groupId)) {
            groupId = getProjectProperty(GROUP)
        }
        if (AppInitCommonUtils.isEmpty(groupId) || AppInitCommonUtils.isEmpty(artifactId)) {
            return null
        }
        return "${groupId}:${artifactId}"
    }

    private String getProjectProperty(String propertyName) {
        if (mProject.hasProperty(propertyName)) {
            return mProject.property(propertyName)
        }
        return null
    }

    /**
     * 获取 apt 需要的模块名称
     */
    private String getAptModuleName() {
        String groupId = mProject.projectDir.parentFile.name
        // CI 上构建时会多出「@并发构建编号」
        if (groupId.contains('@')) {
            println("rootProject 名称 ${mProject.rootProject.name}")
            groupId = groupId.substring(0, groupId.lastIndexOf('@'))
        }
        if ('0123456789'.contains(groupId.substring(0, 1))) {
            groupId = "Dummy$groupId"
        }
        return "${groupId}:${mProject.name}"
    }

    /**
     * 是否调试 apt
     */
    private boolean isDebugApt() {
        return mProject.hasProperty(getAptDebugKey()) && Boolean.valueOf(mProject.property(getAptDebugKey()))
    }

    private void info(String msg) {
        println "「${this.class.simpleName}」「${mProject.path}」「${getAptModuleName()}」=> ${msg}"
    }

    static boolean hasAndroidPlugin(Project project) {
        return hasAppPlugin(project) || hasLibraryPlugin(project)
    }

    static boolean hasAppPlugin(Project project) {
        return project.plugins.hasPlugin('com.android.application')
    }

    static boolean hasLibraryPlugin(Project project) {
        return project.plugins.hasPlugin('com.android.library')
    }

    static boolean hasKotlinAndroidPlugin(Project project) {
        return project.plugins.hasPlugin('kotlin-android')
    }
}