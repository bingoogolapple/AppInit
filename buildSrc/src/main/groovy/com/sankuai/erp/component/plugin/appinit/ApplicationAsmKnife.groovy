package com.sankuai.erp.component.plugin.appinit

import org.objectweb.asm.*

/**
 * 作者:王浩
 * 创建时间:2018/11/15
 * 描述:修改 Application 字节码
 */
class ApplicationAsmKnife {

    static class ApplicationClassVisitor extends ClassVisitor {
        private InjectNoParameterMethodVisitor mOnTerminateMethodVisitor
        private OnConfigurationChangedMethodVisitor mOnConfigurationChangedMethodVisitor
        private InjectNoParameterMethodVisitor mOnLowMemoryMethodVisitor
        private OnTrimMemoryMethodVisitor mOnTrimMemoryMethodVisitor

        ApplicationClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor result = super.visitMethod(access, name, desc, signature, exceptions)
            if ('onTerminate' == name) {
                result = mOnTerminateMethodVisitor = new InjectNoParameterMethodVisitor(result, name)
            } else if ('onConfigurationChanged' == name) {
                result = mOnConfigurationChangedMethodVisitor = new OnConfigurationChangedMethodVisitor(result)
            } else if ('onLowMemory' == name) {
                result = mOnLowMemoryMethodVisitor = new InjectNoParameterMethodVisitor(result, name)
            } else if ('onTrimMemory' == name) {
                result = mOnTrimMemoryMethodVisitor = new OnTrimMemoryMethodVisitor(result)
            }
            return result
        }

        @Override
        void visitEnd() {
            if (mOnTerminateMethodVisitor == null) {
                InjectNoParameterMethodVisitor.visitMethodOnNotExist(this, 'onTerminate')
            }
            if (mOnConfigurationChangedMethodVisitor == null) {
                OnConfigurationChangedMethodVisitor.visitMethodOnNotExist(this)
            }
            if (mOnLowMemoryMethodVisitor == null) {
                InjectNoParameterMethodVisitor.visitMethodOnNotExist(this, 'onLowMemory')
            }
            if (mOnTrimMemoryMethodVisitor == null) {
                OnTrimMemoryMethodVisitor.visitMethodOnNotExist(this)
            }
            super.visitEnd()
        }
    }

    private static class OnConfigurationChangedMethodVisitor extends MethodVisitor {

        OnConfigurationChangedMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        static void visitMethodOnNotExist(ClassVisitor classVisitor) {
            OnConfigurationChangedMethodVisitor methodVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, 'onConfigurationChanged', "(Landroid/content/res/Configuration;)V", null, null)
            methodVisitor.visitCode()
            methodVisitor.callSuper()
            methodVisitor.visitInsn(Opcodes.RETURN)
            methodVisitor.visitEnd()
        }

        void callSuper() {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitVarInsn(Opcodes.ALOAD, 1)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Application", 'onConfigurationChanged', "(Landroid/content/res/Configuration;)V", false)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/sankuai/erp/component/appinit/api/AppInitManager", "get", "()Lcom/sankuai/erp/component/appinit/api/AppInitManager;", false)
                mv.visitVarInsn(Opcodes.ALOAD, 1)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/sankuai/erp/component/appinit/api/AppInitManager", 'onConfigurationChanged', "(Landroid/content/res/Configuration;)V", false)
            }
            super.visitInsn(opcode)
        }
    }

    private static class OnTrimMemoryMethodVisitor extends MethodVisitor {

        OnTrimMemoryMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        static void visitMethodOnNotExist(ClassVisitor classVisitor) {
            OnTrimMemoryMethodVisitor methodVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, 'onTrimMemory', "(I)V", null, null)
            methodVisitor.visitCode()
            methodVisitor.callSuper()
            methodVisitor.visitInsn(Opcodes.RETURN)
            methodVisitor.visitEnd()
        }

        void callSuper() {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitVarInsn(Opcodes.ILOAD, 1)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Application", 'onTrimMemory', "(I)V", false)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/sankuai/erp/component/appinit/api/AppInitManager", "get", "()Lcom/sankuai/erp/component/appinit/api/AppInitManager;", false)
                mv.visitVarInsn(Opcodes.ILOAD, 1)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/sankuai/erp/component/appinit/api/AppInitManager", 'onTrimMemory', "(I)V", false)
            }
            super.visitInsn(opcode)
        }
    }

    private static class InjectNoParameterMethodVisitor extends MethodVisitor {
        private String mName

        InjectNoParameterMethodVisitor(MethodVisitor mv, String name) {
            super(Opcodes.ASM5, mv)
            mName = name
        }

        static void visitMethodOnNotExist(ClassVisitor classVisitor, String name) {
            InjectNoParameterMethodVisitor methodVisitor = classVisitor.visitMethod(Opcodes.ACC_PUBLIC, name, "()V", null, null)
            methodVisitor.visitCode()
            methodVisitor.callSuper()
            methodVisitor.visitInsn(Opcodes.RETURN)
            methodVisitor.visitEnd()
        }

        void callSuper() {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Application", mName, "()V", false)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/sankuai/erp/component/appinit/api/AppInitManager", "get", "()Lcom/sankuai/erp/component/appinit/api/AppInitManager;", false)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/sankuai/erp/component/appinit/api/AppInitManager", mName, "()V", false)
            }
            super.visitInsn(opcode)
        }
    }
}