package com.sankuai.erp.component.plugin.appinit

import com.sankuai.erp.component.appinit.common.*
import org.gradle.api.Project
import org.objectweb.asm.ClassWriter

import java.util.concurrent.CopyOnWriteArrayList

/**
 * 作者:王浩
 * 创建时间:2018/11/18
 * 描述:
 */
class AppInitAsmTransform extends BaseAsmTransform {
    private static final String CHILD_INIT_TABLE_ENTRY_NAME = convertCanonicalNameToEntryName(ModuleConsts.CHILD_INIT_TABLE_CANONICAL_NAME, false)
    private static final String APP_INIT_MANAGER_ENTRY_NAME = convertCanonicalNameToEntryName(ModuleConsts.APP_INIT_MANAGER_CANONICAL_NAME, false)
    private static final String APP_INIT_MANAGER_ENTRY_NAME_WITH_CLASS = convertCanonicalNameToEntryName(ModuleConsts.APP_INIT_MANAGER_CANONICAL_NAME, true)

    private File mAppInitManagerCtClassDest
    private File mApplicationCtClassDest

    private File mAppInitLogDir
    private String mApplicationEntryName
    private String mApplicationEntryNameWithClass
    private AppInitExtension mAppInitExtension
    private List<String> mChildInitTableClassNameList
    private List<ChildInitTable> mChildInitTableList

    AppInitAsmTransform(Project project) {
        super(project)
        mAppInitLogDir = new File(mProject.buildDir, 'AppInitLog')
        if (mAppInitLogDir.exists()) {
            mAppInitLogDir.delete()
        }
        mAppInitLogDir.mkdirs()
    }

    @Override
    String getName() {
        return "AppInit"
    }

    @Override
    protected void beforeTransform() {
        super.beforeTransform()
        mAppInitExtension = mProject.extensions.findByType(AppInitExtension)
        if (!AppInitCommonUtils.isEmpty(mAppInitExtension.applicationCanonicalName)) {
            mApplicationEntryNameWithClass = convertCanonicalNameToEntryName(mAppInitExtension.applicationCanonicalName, true)
            mApplicationEntryName = convertCanonicalNameToEntryName(mAppInitExtension.applicationCanonicalName, false)
        }

        mChildInitTableClassNameList = new CopyOnWriteArrayList<>()
        mChildInitTableList = new ArrayList<>()

        mAppInitManagerCtClassDest = null
        mApplicationCtClassDest = null
    }

    @Override
    protected boolean shouldScanPath(String path) {
        boolean result = path.contains(ModuleConsts.PACKAGE_NAME_GENERATED_SLASH) || path.contains(APP_INIT_MANAGER_ENTRY_NAME_WITH_CLASS)
        if (!AppInitCommonUtils.isEmpty(mAppInitExtension.applicationCanonicalName)) {
            result = result || path.contains(mApplicationEntryNameWithClass)
        }
        return result
    }

    @Override
    protected void scanClass(String name, String superName, String[] interfaces, File dest) {
        if (superName == CHILD_INIT_TABLE_ENTRY_NAME) {
            mChildInitTableClassNameList.add(name.replace('/', '.'))
            AppInitLogger.d "找到了子表 ${name}"
        } else if (name == APP_INIT_MANAGER_ENTRY_NAME) {
            mAppInitManagerCtClassDest = dest
            AppInitLogger.d "找到了 AppInitManager ${name}，存放位置为 ${dest}"
        } else if (name == mApplicationEntryName) {
            mApplicationCtClassDest = dest
            mApplicationEntryNameWithClass = "${name}.class"
            AppInitLogger.d "找到了 Application ${name}，存放位置为 ${dest}"
        }
    }

    @Override
    protected void handle() {
        if (mAppInitManagerCtClassDest == null || !mAppInitManagerCtClassDest.exists()) {
            throw new IllegalStateException("未找到 ${ModuleConsts.APP_INIT_MANAGER_CANONICAL_NAME}")
        }
        AppInitLogger.d "mAppInitManagerCtClassDest 为 ${mAppInitManagerCtClassDest.absolutePath}"
        // 修改 AppInitManager，一定是在 jar 包里
        modifyJarClass(mAppInitManagerCtClassDest)

        // 处理 Application 自动注入
        if (mApplicationCtClassDest == null || !mApplicationCtClassDest.exists()) {
            // 未找到 AndroidManifest.xml 中配置的 Application，不自动注入字节码
            return
        }
        AppInitLogger.d "mApplicationCtClassDest 为 ${mApplicationCtClassDest.absolutePath}"
        // 修改 Application，可能在 jar 包里，也可能在壳工程的目录里
        if (mApplicationCtClassDest.isDirectory()) {
            modifyDirectoryClass(mApplicationCtClassDest, mApplicationEntryNameWithClass)
        } else {
            modifyJarClass(mApplicationCtClassDest)
        }
    }

    @Override
    protected byte[] modifyDirectoryClass(InputStream is, String entryName) {
        if (entryName == mApplicationEntryNameWithClass) { // 修改 Application
            return modifyClass(is) { new ApplicationAsmKnife.ApplicationClassVisitor(it) }
        }
        return null
    }

    @Override
    protected byte[] modifyJarClass(File jarFile, InputStream jarEntryInputStream, String entryName) {
        if (entryName == APP_INIT_MANAGER_ENTRY_NAME_WITH_CLASS) { // 修改 AppInitManager
            for (String childInitTableClassName : mChildInitTableClassNameList) {
                mChildInitTableList.add(loadClass(childInitTableClassName).newInstance(0))
            }
            List<AppInitItem> appInitItemList = ChildInitTableSortUtils.sortMasterInitTable(mAppInitExtension, mChildInitTableList, mLogSb)
            return modifyClass(jarEntryInputStream) { ClassWriter classWriter ->
                new AppInitManagerAsmKnife.AppInitManagerClassVisitor(classWriter, mChildInitTableList, appInitItemList, mAppInitExtension.getAbortOnNotExist())
            }
        } else if (entryName == mApplicationEntryNameWithClass) { // 修改 Application
            return modifyClass(jarEntryInputStream) { new ApplicationAsmKnife.ApplicationClassVisitor(it) }
        }
        return null
    }
}