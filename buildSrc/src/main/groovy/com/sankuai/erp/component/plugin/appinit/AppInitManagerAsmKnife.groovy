package com.sankuai.erp.component.plugin.appinit

import com.sankuai.erp.component.appinit.common.AppInitItem
import com.sankuai.erp.component.appinit.common.ChildInitTable
import com.sankuai.erp.component.appinit.common.ModuleConsts
import org.objectweb.asm.*

/**
 * 作者:王浩
 * 创建时间:2018/11/18
 * 描述:修改 AppInitManager 字节码
 */
class AppInitManagerAsmKnife {

    static class AppInitManagerClassVisitor extends ClassVisitor {
        private List<ChildInitTable> mChildInitTableList
        private List<AppInitItem> mAppInitItemList
        private boolean mAbortOnNotExist

        AppInitManagerClassVisitor(ClassVisitor cv, List<ChildInitTable> childInitTableList, List<AppInitItem> appInitItemList, boolean abortOnNotExist) {
            super(Opcodes.ASM5, cv)
            mChildInitTableList = childInitTableList
            mAppInitItemList = appInitItemList
            mAbortOnNotExist = abortOnNotExist
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor result = super.visitMethod(access, name, desc, signature, exceptions)
            if (ModuleConsts.METHOD_INJECT_CHILD_INIT_TABLE_LIST == name) {
                result = new InjectChildInitTableListMethodVisitor(result, mChildInitTableList, mAbortOnNotExist)
            } else if (ModuleConsts.METHOD_INJECT_APP_INIT_ITEM_LIST == name) {
                result = new InjectAppInitItemListMethodVisitor(result, mAppInitItemList)
            }
            return result
        }
    }

    private static class InjectChildInitTableListMethodVisitor extends MethodVisitor {
        private List<ChildInitTable> mChildInitTableList
        private boolean mAbortOnNotExist

        InjectChildInitTableListMethodVisitor(MethodVisitor mv, List<ChildInitTable> childInitTableList, boolean abortOnNotExist) {
            super(Opcodes.ASM5, mv)
            mChildInitTableList = childInitTableList
            mAbortOnNotExist = abortOnNotExist
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                // 修改 mAbortOnNotExist
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                if (mAbortOnNotExist) {
                    mv.visitInsn(Opcodes.ICONST_1)
                } else {
                    mv.visitInsn(Opcodes.ICONST_0)
                }
                mv.visitFieldInsn(Opcodes.PUTFIELD, "com/sankuai/erp/component/appinit/api/AppInitManager", "mAbortOnNotExist", "Z")

                // 注入 mChildInitTableList
                String childInitTableEntryName
                mChildInitTableList.each { childInitTable ->
                    childInitTableEntryName = BaseTransform.convertCanonicalNameToEntryName(childInitTable.class.name, false)
                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, "com/sankuai/erp/component/appinit/api/AppInitManager", "mChildInitTableList", "Ljava/util/List;")
                    mv.visitTypeInsn(Opcodes.NEW, childInitTableEntryName)
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitIntInsn(Opcodes.SIPUSH, childInitTable.priority)
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, childInitTableEntryName, "<init>", "(I)V", false)
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
                    mv.visitInsn(Opcodes.POP)
                }
            }
            super.visitInsn(opcode)
        }
    }

    private static class InjectAppInitItemListMethodVisitor extends MethodVisitor {
        private List<AppInitItem> mAppInitItemList

        InjectAppInitItemListMethodVisitor(MethodVisitor mv, List<AppInitItem> appInitItemList) {
            super(Opcodes.ASM5, mv)
            mAppInitItemList = appInitItemList
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                String appInitEntryName
                mAppInitItemList.each { appInitItem ->
                    appInitEntryName = BaseTransform.convertCanonicalNameToEntryName(appInitItem.appInitClassName, false)

                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, "com/sankuai/erp/component/appinit/api/AppInitManager", "mAppInitItemList", "Ljava/util/List;")
                    mv.visitTypeInsn(Opcodes.NEW, "com/sankuai/erp/component/appinit/common/AppInitItem")
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitTypeInsn(Opcodes.NEW, appInitEntryName)
                    mv.visitInsn(Opcodes.DUP)
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, appInitEntryName, "<init>", "()V", false)
                    mv.visitIntInsn(Opcodes.SIPUSH, appInitItem.process.ordinal())
                    mv.visitIntInsn(Opcodes.SIPUSH, appInitItem.priority)
                    mv.visitLdcInsn(appInitItem.coordinate)
                    mv.visitLdcInsn(appInitItem.aheadOf)
                    mv.visitLdcInsn(appInitItem.description)
                    mv.visitLdcInsn(appInitItem.onlyForDebug.toString())
                    mv.visitLdcInsn(appInitItem.lazyInit.toString())
                    mv.visitLdcInsn(appInitItem.moduleCoordinate)
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/sankuai/erp/component/appinit/common/AppInitItem", "<init>",
                            "(Lcom/sankuai/erp/component/appinit/common/IAppInit;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                            false)
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
                    mv.visitInsn(Opcodes.POP)
                }
            }
            super.visitInsn(opcode)
        }
    }
}