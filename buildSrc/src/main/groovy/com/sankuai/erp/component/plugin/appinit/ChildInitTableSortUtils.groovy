package com.sankuai.erp.component.plugin.appinit

import com.sankuai.erp.component.appinit.common.AppInit
import com.sankuai.erp.component.appinit.common.AppInitCommonUtils
import com.sankuai.erp.component.appinit.common.AppInitItem
import com.sankuai.erp.component.appinit.common.AppInitLogger
import com.sankuai.erp.component.appinit.common.ChildInitTable

/**
 * 作者:王浩
 * 创建时间:2018/11/18
 * 描述:
 */
class ChildInitTableSortUtils {
    private static final StringBuilder NOT_EXIST_MODULE_SB = new StringBuilder()

    static List<AppInitItem> sortMasterInitTable(AppInitExtension appInitExtension, List<ChildInitTable> childInitTableList, StringBuilder logSb) {
        if (childInitTableList == null || childInitTableList.isEmpty()) {
            logSb.append(String.format('没有找到添加了 %s 注解的类', AppInit.class.getName()))
            return null
        }
        // 检测子表名称是否重复并对子表进行排序
        logSb.append(AppInitCommonUtils.timeStr('模块排序') {
            checkChildInitTableDuplicateAndSort(childInitTableList, appInitExtension.dependencyMap)
        })

        String notExistMsg = NOT_EXIST_MODULE_SB.toString()
        if (!AppInitCommonUtils.isEmpty(notExistMsg)) {
            if (appInitExtension.getAbortOnNotExist()) {
                throw new IllegalArgumentException(notExistMsg)
            } else {
                logSb.append("    !!!!!!不存在的模块有:\n").append(NOT_EXIST_MODULE_SB.toString()).append("\n")
            }
        }

        return AppInitCommonUtils.sortAppInitItem(appInitExtension.getAbortOnNotExist(), childInitTableList, null, logSb)
    }

    private static void checkChildInitTableDuplicateAndSort(List<ChildInitTable> childInitTableList,
                                                            Map<String, Collection<String>> moduleCoordinateDependencyMap) {
        handleModuleCoordinateDependency(childInitTableList, moduleCoordinateDependencyMap)

        // 默认按 coordinate 忽略大小写自然排序
        Collections.sort(childInitTableList) { first, second ->
            return first.coordinate.compareToIgnoreCase(second.coordinate)
        }
        int priority = 0
        for (ChildInitTable childInitTable : childInitTableList) {
            childInitTable.priority = priority++
        }

        // 根据依赖关系计算 priority
        Map<String, ChildInitTable> moduleCoordinateMap = new HashMap<>()
        for (ChildInitTable childInitTable : childInitTableList) {
            // 检测 coordinate 是否重复
            checkChildInitTableDuplicate(childInitTable, moduleCoordinateMap)
            // 计算优先级
            calculateChildInitTablePriority(childInitTable, childInitTableList)
        }
        // 根据 priority 排序
        Collections.sort(childInitTableList)
    }

    private static void handleModuleCoordinateDependency(List<ChildInitTable> childInitTableList,
                                                         Map<String, Collection<String>> moduleCoordinateDependencyMap) {
        if (moduleCoordinateDependencyMap == null || moduleCoordinateDependencyMap.isEmpty()) {
            return
        }
        ChildInitTable childInitTable
        for (Map.Entry<String, Collection<String>> dependencyEntry : moduleCoordinateDependencyMap.entrySet()) {
            if (AppInitCommonUtils.isEmpty(dependencyEntry.getKey()) || dependencyEntry.getValue() == null || dependencyEntry.getValue().isEmpty()) {
                continue
            }
            childInitTable = findChildInitTable(dependencyEntry.getKey(), childInitTableList, null)
            if (childInitTable != null) {
                childInitTable.setDependenciesSet(new HashSet<>(dependencyEntry.getValue()))
            }
        }
    }

    private static void checkChildInitTableDuplicate(ChildInitTable childInitTable, Map<String, ChildInitTable> moduleCoordinateMap) {
        String moduleCoordinate = childInitTable.coordinate
        if (moduleCoordinateMap.containsKey(moduleCoordinate)) {
            String msg = String.format("不允许出现两个 ChildInitTable 的 coordinate 相同：\n%s\n%s\n",
                    childInitTable.getModuleInfo(), moduleCoordinateMap.get(moduleCoordinate).getModuleInfo())
            throw new IllegalArgumentException(msg)
        } else {
            moduleCoordinateMap.put(moduleCoordinate, childInitTable)
        }
    }

    private static void calculateChildInitTablePriority(ChildInitTable childInitTable, List<ChildInitTable> childInitTableList) {
        if (childInitTable.calculated) {
            return
        }
        if (childInitTable.dependencies == null || childInitTable.dependencies.isEmpty()) {
            childInitTable.calculated = true
            return
        }

        childInitTable.calculated = true
        ChildInitTable dependencyChildInitTable
        for (String dependencyModuleCoordinate : childInitTable.dependencies) {
            if (childInitTable.coordinate == dependencyModuleCoordinate) {
                throw new IllegalArgumentException("「AppInitPlugin」=> 不允许 ChildInitTable 依赖自己：" + childInitTable.getModuleInfo())
            }
            dependencyChildInitTable = findChildInitTable(dependencyModuleCoordinate, childInitTableList, childInitTable)
            if (dependencyChildInitTable == null) {
                continue
            }
            // 检测循环依赖
            checkChildInitTableCircularDependency(childInitTable, dependencyChildInitTable, childInitTableList, null)
            // 计算优先级
            calculateChildInitTablePriority(dependencyChildInitTable, childInitTableList)
            if (childInitTable.priority <= dependencyChildInitTable.priority) {
                childInitTable.priority = dependencyChildInitTable.priority + 1
            }
        }
    }

    private static void checkChildInitTableCircularDependency(ChildInitTable childInitTable, ChildInitTable dependencyChildInitTable,
                                                              List<ChildInitTable> childInitTableList, List<ChildInitTable> transitiveDependencyList) {
        if (dependencyChildInitTable == null) {
            return
        }
        Set<String> dependencies = dependencyChildInitTable.dependencies
        if (dependencies == null || dependencies.isEmpty()) {
            return
        }

        for (String dependencyModuleCoordinate : dependencies) {
            if (dependencyChildInitTable.coordinate == dependencyModuleCoordinate) {
                throw new IllegalArgumentException("「AppInitPlugin」=> 不允许 ChildInitTable 依赖自己：" + dependencyChildInitTable.getModuleInfo())
            } else if (childInitTable.coordinate == dependencyModuleCoordinate) {
                StringBuilder msgSb = new StringBuilder("「AppInitPlugin」=> 不允许出现两个 ChildInitTable ")
                if (transitiveDependencyList == null || transitiveDependencyList.size() == 0) {
                    msgSb.append("循环依赖：\n").append(childInitTable.getModuleInfo()).append("\n").append(dependencyChildInitTable.getModuleInfo())
                } else {
                    msgSb.append("传递循环依赖：\n").append(childInitTable.getModuleInfo()).append("\n")
                    for (ChildInitTable transitiveChildInitTable : transitiveDependencyList) {
                        msgSb.append(transitiveChildInitTable.getModuleInfo()).append("\n")
                    }
                    msgSb.append(dependencyChildInitTable.getModuleInfo())
                }
                throw new IllegalArgumentException(msgSb.toString())
            } else {
                ChildInitTable transitiveDependencyChildInitTable = findChildInitTable(dependencyModuleCoordinate, childInitTableList,
                        dependencyChildInitTable)
                if (transitiveDependencyList == null) {
                    transitiveDependencyList = new ArrayList<>()
                }
                transitiveDependencyList.add(dependencyChildInitTable)
                checkChildInitTableCircularDependency(childInitTable, transitiveDependencyChildInitTable, childInitTableList, transitiveDependencyList)
            }
        }
    }

    private static ChildInitTable findChildInitTable(String moduleCoordinate, List<ChildInitTable> childInitTableList, ChildInitTable lastChildInitTable) {
        if (AppInitCommonUtils.isEmpty(moduleCoordinate)) {
            return null
        }

        ChildInitTable childInitTable = childInitTableList.find { childInitTable -> childInitTable.coordinate == moduleCoordinate }
        if (childInitTable != null) {
            return childInitTable
        }

        String msg
        if (lastChildInitTable != null) {
            msg = String.format("    %s=> 依赖的模块《%s》不存在\n", lastChildInitTable.getModuleInfo(), moduleCoordinate)
        } else {
            msg = String.format("        模块《%s》不存在\n", moduleCoordinate)
        }
        NOT_EXIST_MODULE_SB.append(msg)
        AppInitLogger.e(msg)
        return null
    }

}