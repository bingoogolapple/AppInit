# Issue/提问须知

在提交 issue 之前，我们应该先查询是否已经有相关的 issue 和[使用文档](https://github.com/Meituan-Dianping/AppInit/blob/master/docs/user-manual.md)。提交 issue 时，我们需要写明 issue 的原因，最好可以携带编译或运行过程的日志或者截图。issue 需要以下面的格式提出：

----

* 异常类型：app 运行时异常/编译异常

* 手机型号（如是编译异常，则可以不填）：如：Nexus 5

* 手机系统版本（如是编译异常，则可以不填）：如：Android 5.0

* AppInit 版本：如：1.0.0

* Gradle 版本：如：3.1.3

* 系统：如：Mac

* 堆栈/日志或者截图：

----

如是编译异常，请在执行 gradle 命令时，加上 --stacktrace，并把结果重定向，如：

```shell
./gradlew clean assembleRelease --stacktrace --no-daemon > log.txt
```

结果重定向到当前的目录下的 log.txt 文件；日志中我们需要过滤`AppInit`关键字，可以初步查找问题的大概原因。

AppInit 提供了 sample 样例与我们的源码，大家在使用前可以先将样例跑通，如遇任何疑问也欢迎大家提出，更鼓励大家给我们提 PR，谢谢大家的支持。