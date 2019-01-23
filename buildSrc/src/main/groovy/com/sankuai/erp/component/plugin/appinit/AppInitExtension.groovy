package com.sankuai.erp.component.plugin.appinit

import com.sankuai.erp.component.appinit.common.AppInitCommonUtils
/**
 * 作者:王浩
 * 创建时间:2018/11/2
 * 描述:
 */
class AppInitExtension {
    private boolean mAbortOnNotExist = true
    /**
     * AndroidManifest.xml 中配置的 Application 的类全名，配置后注入字节码自动回调 AppInitManager 对应的 Application 生命周期方法
     */
    private String mApplicationCanonicalName
    /**
     * 编译期自定义模块间初始化的依赖关系，键是 coordinate，值是 dependencyCoordinate
     */
    final Map<String, Collection<String>> dependencyMap = new HashMap<>()

    /**
     * AndroidManifest.xml 中配置的 Application 的类全名，配置后注入字节码自动回调 AppInitManager 对应的 Application 生命周期方法
     */
    void applicationCanonicalName(String applicationCanonicalName) {
        mApplicationCanonicalName = applicationCanonicalName
    }

    String getApplicationCanonicalName() {
        return mApplicationCanonicalName
    }

    boolean getAbortOnNotExist() {
        return mAbortOnNotExist
    }

    /**
     * 依赖的模块或 aheadOf 指定的初始化不存在时是否中断编译
     */
    void abortOnNotExist(boolean abortOnNotExist) {
        mAbortOnNotExist = abortOnNotExist
    }

    /**
     * 自定义模块间初始化的依赖关系，键是 coordinate，值是被依赖的 coordinate
     */
    void dependency(String coordinate, String dependencyCoordinate) {
        if (AppInitCommonUtils.isEmpty(coordinate)) {
            throw new IllegalArgumentException("appInit {\n    dependency 「第一个参数 coordinate 不能为空」\n}")
        }
        if (AppInitCommonUtils.isEmpty(dependencyCoordinate)) {
            throw new IllegalArgumentException("appInit {\n    dependency 「第二个参数 dependencyCoordinate 不能为空」\n}")
        }
        getDependencyCoordinateSet(coordinate).add(dependencyCoordinate)
    }

    /**
     * 自定义模块间初始化的依赖关系，键是 coordinate，值是被依赖的 coordinate 集合
     */
    void dependency(String coordinate, Collection dependencyCoordinateCollection) {
        if (AppInitCommonUtils.isEmpty(coordinate)) {
            throw new IllegalArgumentException("appInit {\n    dependency 「第一个参数 coordinate 不能为空」\n}")
        }
        if (dependencyCoordinateCollection == null || dependencyCoordinateCollection.isEmpty()) {
            throw new IllegalArgumentException("appInit {\n    dependency 「第二个参数 dependencyCoordinateCollection 不能为空」\n}")
        }
        getDependencyCoordinateSet(coordinate).addAll(dependencyCoordinateCollection)
    }

    /**
     * 自定义模块间初始化的依赖关系
     */
    void dependency(Map<String, Collection<String>> dependencyMap) {
        if (dependencyMap == null || dependencyMap.isEmpty()) {
            return
        }

        for (Map.Entry<String, Collection<String>> entry : dependencyMap.entrySet()) {
            if (AppInitCommonUtils.isEmpty(entry.getKey()) || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue
            }
            dependency(entry.getKey(), entry.getValue())
        }
    }

    private Set<String> getDependencyCoordinateSet(String coordinate) {
        Set<String> dependencyCoordinateSet = dependencyMap.get(coordinate)
        if (dependencyCoordinateSet == null) {
            dependencyCoordinateSet = new HashSet<>()
            dependencyMap.put(coordinate, dependencyCoordinateSet)
        }
        return dependencyCoordinateSet
    }
}
