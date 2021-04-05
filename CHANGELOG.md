# 更新日志

## Version 1.0.8 (2020-04-05)

- 从 JCenter 迁移到 JitPack

## Version 1.0.7 (2020-03-26)

- 改由个人账号维护，Gradle 依赖由「com.sankuai.erp.component:appinit-plugin」变为「cn.bingoogolapple:bga-appinit-plugin」
- v1.0.7 扫描 jar 包时捕获一下异常

## Version 1.0.6 (2019-10-29)

- v1.0.6 fix #15 兼容 gradle 4.1

## Version 1.0.5 (2019-04-14)

- v1.0.5 修复 Windows 下 rebuild 时提示无法删除 app\build\intermediates\transforms\xxx\debug\xxx.jar

## Version 1.0.4 (2019-04-13)

- v1.0.4 去掉对 commons-io:2.6 的依赖

## Version 1.0.3 (2019-02-19)

- Fix #10 兼容 rootProject 目录以数字开头

## Version 1.0.2 (2019-01-30)

- Fix #6 minSdkVersion 改为 16

## Version 1.0.1 (2019-01-25)

- Fix #1 不支持增量编译的 Transform 在消费上游文件前需要先清除输出目录的文件
- Fix #3 Transform 里获取 Variant 的方式可以更优雅一些

## Version 1.0.0 (2019-01-23)

- 第一个开源版本