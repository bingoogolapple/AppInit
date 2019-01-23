package com.sankuai.erp.component.plugin.appinit

import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * 作者:王浩
 * 创建时间:2018/11/18
 * 描述:
 */
abstract class BaseAsmTransform extends BaseTransform {

    BaseAsmTransform(Project project) {
        super(project)
    }

    @Override
    protected void scanClass(InputStream inputStream, File dest) {
        new ClassReader(inputStream).accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces)
                scanClass(name, superName, interfaces, dest)
            }
        }, ClassReader.EXPAND_FRAMES)
    }

    protected abstract void scanClass(String name, String superName, String[] interfaces, File dest)

    protected void modifyDirectoryClass(File destDir, String entryName) {
        File classFile = new File(destDir, entryName)
        new FileInputStream(classFile).withCloseable { fis ->
            byte[] bytes = modifyDirectoryClass(fis, entryName)
            if (bytes == null) {
                return
            }
            new FileOutputStream(classFile).withCloseable { fos ->
                fos.write(bytes)
            }
        }
    }

    protected byte[] modifyDirectoryClass(InputStream is, String entryName) {
    }

    protected static byte[] modifyClass(InputStream is, Closure<ClassVisitor> closure) {
        ClassReader classReader = new ClassReader(is)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        classReader.accept(closure.call(classWriter), ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}