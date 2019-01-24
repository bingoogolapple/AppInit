package com.sankuai.erp.component.plugin.appinit

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.sankuai.erp.component.appinit.common.AppInitCommonUtils
import com.sankuai.erp.component.appinit.common.AppInitLogger
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.gradle.internal.Pair

import java.text.SimpleDateFormat
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 作者:王浩
 * 创建时间:2018/11/6
 * 描述:
 */
abstract class BaseTransform extends Transform {
    protected final Set<String> mExcludeJarSet = ["com.android.support", "android.arch.", "androidx."]
    protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    protected final Project mProject
    private URLClassLoader mUrlClassLoader
    protected String mVariant
    protected StringBuilder mLogSb

    BaseTransform(Project project) {
        mProject = project
    }

    /**
     * 用于指明本 Transform 的名字，也是代表该 Transform 的 Task 的名字。transformClassesWith「getName()」For「Free|Vip」「Baidu|Xiaomi」「Debug|Release」
     */
    @Override
    String getName() {
        return this.class.simpleName.replace('Transform', '')
    }

    /**
     * 用于指明 Transform 的输入类型，可以作为输入过滤的手段。
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 用于指明 Transform 的作用域
     */
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 用于指明是否是增量构建
     */
    @Override
    boolean isIncremental() {
        return false
    }

    protected void updateVariant(TransformInvocation transformInvocation) {
        TransformTask task = (TransformTask) transformInvocation.context
        mVariant = task.getVariantName()
        AppInitLogger.d "变体为 ${mVariant}"
    }

    protected static String getDestJarName(JarInput jarInput) {
        String destJarName = jarInput.name
        if (destJarName.endsWith(".jar")) {
            destJarName = "${destJarName.substring(0, destJarName.length() - 4)}_${DigestUtils.md5Hex(jarInput.file.absolutePath)}"
        }
        return destJarName
    }

    protected static void transformAndroidTest(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        transformInput(transformInvocation, new Consumer<JarInput>() {
            @Override
            void accept(JarInput jarInput) {
                FileUtils.copyFile(jarInput.file, transformInvocation.outputProvider.getContentLocation(getDestJarName(jarInput), jarInput.contentTypes, jarInput.scopes, Format.JAR))
            }
        }, new Consumer<DirectoryInput>() {
            @Override
            void accept(DirectoryInput directoryInput) {
                FileUtils.copyDirectory(directoryInput.file, transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY))
            }
        })
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll()
        }

        updateVariant(transformInvocation)
        if (mVariant.toLowerCase().endsWith('androidtest')) {
            transformAndroidTest(transformInvocation)
            return
        }

        mLogSb = new StringBuilder(SDF.format(new Date())).append('\n\n')

        String scanTime = ''
        String handleTime = ''
        String transformTime = AppInitCommonUtils.timeStr("transform ") {
            beforeTransform()
            try {
                scanTime = AppInitCommonUtils.timeStr("scan ") {
                    scan(transformInvocation)
                }
                handleTime = AppInitCommonUtils.timeStr("handle ") {
                    handle()
                }
            } finally {
                afterTransform()
            }
        }
        mLogSb.append(scanTime).append(handleTime).append(transformTime)
        FileUtils.writeStringToFile(new File(mAppInitLogDir, "${mVariant}.log"), mLogSb.toString())
    }

    protected void scan(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        transformInput(transformInvocation, new Consumer<JarInput>() {
            @Override
            void accept(JarInput jarInput) {
                handleJarInput(jarInput, transformInvocation)
            }
        }, new Consumer<DirectoryInput>() {
            @Override
            void accept(DirectoryInput directoryInput) {
                handleDirectoryInput(directoryInput, transformInvocation)
            }
        })
    }

    protected void beforeTransform() {
        // 每个 Variant 都会执行一次 transform 方法，在 beforeTransform 时重新创建 ClassLoader，避免多个 Variant 中存在相同类时加载类异常
        mUrlClassLoader = new URLClassLoader(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs(), this.class.classLoader) {
            @Override
            protected void addURL(URL url) {
                super.addURL(url)
            }
        }
        // /Applications/develop/AndroidSDK/platforms/android-28/android.jar
        String androidBootClasspath = mProject.android.bootClasspath[0].toString()
        mUrlClassLoader.addURL(new File(androidBootClasspath).toURI().toURL())
    }

    protected void afterTransform() {
    }

    protected abstract void handle()

    protected void handleJarInput(JarInput jarInput, TransformInvocation transformInvocation) {
        addJarInputToClassPath(jarInput.file)

        String destJarName = getDestJarName(jarInput)
        // /Users/wanghao/git/AndroidStudio/xmd/erp-components-android/appinit/demo/app/build/intermediates/transforms/AppInit/vipBaidu/debug/0.jar
        // /Users/wanghao/git/AndroidStudio/xmd/erp-components-android/appinit/demo/app/build/intermediates/transforms/AppInit/baidu/debug/0.jar
        // /Users/wanghao/git/AndroidStudio/xmd/erp-components-android/appinit/demo/app/build/intermediates/transforms/AppInit/debug/0.jar
        File destJarFile = transformInvocation.outputProvider.getContentLocation(destJarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

        if (shouldScanJar(jarInput)) {
            scanJar(jarInput.file, destJarFile)
        }

        FileUtils.copyFile(jarInput.file, destJarFile)
    }

    protected void addJarInputToClassPath(File jarInputFile) {
        mUrlClassLoader.addURL(jarInputFile.toURI().toURL())
    }

    protected void scanJar(File jarInputFile, File destJarFile) {
        new JarFile(jarInputFile).withCloseable { JarFile jarFile ->
            Enumeration enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                if (shouldScanPathInternal(jarEntry.name)) {
                    jarFile.getInputStream(jarEntry).withCloseable {
                        scanClass(it, destJarFile)
                    }
                }
            }
        }
    }

    protected void handleDirectoryInput(DirectoryInput directoryInput, TransformInvocation transformInvocation) {
        addDirectoryInputToClassPath(directoryInput.file)
        // /Users/wanghao/git/AndroidStudio/xmd/erp-components-android/appinit/demo/app/build/intermediates/transforms/AppInit/vipXiaomi/debug/18
        // /Users/wanghao/git/AndroidStudio/xmd/erp-components-android/appinit/demo/app/build/intermediates/transforms/AppInit/baidu/debug/18
        // /Users/wanghao/git/AndroidStudio/xmd/erp-components-android/appinit/demo/app/build/intermediates/transforms/AppInit/debug/18
        File destDir = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

        directoryInput.file.eachFileRecurse(FileType.FILES) { File file ->
            if (shouldScanPathInternal(file.absolutePath)) {
                new FileInputStream(file).withCloseable {
                    scanClass(it, destDir)
                }
            }
        }

        FileUtils.copyDirectory(directoryInput.file, destDir)
    }

    protected void addDirectoryInputToClassPath(File directoryInputFile) {
        mUrlClassLoader.addURL(directoryInputFile.toURI().toURL())
    }

    protected abstract void scanClass(InputStream inputStream, File dest)

    protected boolean shouldScanJar(JarInput jarInput) {
        if (jarInput == null || jarInput.file == null || !jarInput.file.exists()) {
            return false
        }
        mExcludeJarSet.each {
            if (jarInput.name.contains(it)) {
                return false
            }
        }
        return true
    }

    protected boolean shouldScanPathInternal(String path) {
        if (AppInitCommonUtils.isEmpty(path)) {
            return false
        }
        if (!path.endsWith(SdkConstants.DOT_CLASS)) {
//            AppInitLogger.d "shouldScanPathInternal 没有以 class 结尾：${path}"
            return false
        }
        path = path.replaceAll("\\\\", "/")
        return shouldScanPath(path)
    }

    protected abstract boolean shouldScanPath(String path)

    protected void modifyJarClass(File inputJarFile) {
        if (inputJarFile == null || !inputJarFile.exists() || !inputJarFile.name.endsWith('.jar')) {
            return
        }

        def tempJarFile = new File(inputJarFile.getParent(), inputJarFile.name + ".temp")
        if (tempJarFile.exists()) {
            tempJarFile.delete()
        }

        new JarFile(inputJarFile).withCloseable { JarFile jarFile ->
            new JarOutputStream(new FileOutputStream(tempJarFile)).withCloseable { JarOutputStream jarOutputStream ->
                Enumeration enumeration = jarFile.entries()
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = enumeration.nextElement()
                    String entryName = jarEntry.name
//                    AppInitLogger.d "entryName 为 ${entryName}"
                    jarOutputStream.putNextEntry(new ZipEntry(entryName))
                    jarFile.getInputStream(jarEntry).withCloseable { jarEntryInputStream ->
                        byte[] bytes = modifyJarClass(inputJarFile, jarEntryInputStream, entryName)
                        if (bytes == null) {
                            bytes = IOUtils.toByteArray(jarEntryInputStream)
                        }
                        jarOutputStream.write(bytes)
                        jarOutputStream.closeEntry()
                    }
                }
            }
        }

        inputJarFile.delete()
        tempJarFile.renameTo(inputJarFile)
    }

    /**
     *
     * @param jarFile jar 文件
     * @param jarEntryInputStream class 文件输入流
     * @param entryName 「com/sankuai/erp/component/appinit/api/SimpleAppInit.class」「com/sankuai/erp/component/appinit/api/AppInitManager$$Lambda$0.class」
     * @return 修改后的字节数组，如果为 null 则标识不修改
     */
    protected abstract byte[] modifyJarClass(File jarFile, InputStream jarEntryInputStream, String entryName)

    protected Class<?> loadClass(String name) throws ClassNotFoundException {
        return mUrlClassLoader.loadClass(name)
    }

    private static void transformInput(TransformInvocation transformInvocation, Consumer<JarInput> jarInputConsumer, Consumer<DirectoryInput> directoryInputConsumer) throws TransformException, InterruptedException, IOException {
        Pair<Executor, CyclicBarrier> pair = getExecutorCyclicBarrierPair(transformInvocation)
        transformInvocation.inputs.parallelStream().forEach { TransformInput transformInput ->
            if (!transformInput.jarInputs.empty) {
                pair.left.execute {
                    transformInput.jarInputs.parallelStream().forEach { JarInput jarInput ->
                        jarInputConsumer.accept(jarInput)
                    }
                    pair.right.await()
                }
            }
            if (!transformInput.directoryInputs.empty) {
                pair.left.execute {
                    transformInput.directoryInputs.parallelStream().forEach { DirectoryInput directoryInput ->
                        directoryInputConsumer.accept(directoryInput)
                    }
                    pair.right.await()
                }
            }
        }
        pair.right.await()
    }

    private static Pair<Executor, CyclicBarrier> getExecutorCyclicBarrierPair(TransformInvocation transformInvocation) {
        int inputsCount = 0
        transformInvocation.inputs.each { TransformInput transformInput ->
            if (!transformInput.jarInputs.empty) {
                inputsCount++
            }
            if (!transformInput.directoryInputs.empty) {
                inputsCount++
            }
        }
        return Pair.of(Executors.newFixedThreadPool(inputsCount), new CyclicBarrier(inputsCount + 1))
    }

    protected static String convertCanonicalNameToEntryName(String canonicalName, boolean hasClass) {
        if (canonicalName == null || canonicalName.length() == 0) {
            return ""
        }
        String result = canonicalName.replace('.', '/')
        if (hasClass) {
            result += SdkConstants.DOT_CLASS
        }
        return result
    }

}