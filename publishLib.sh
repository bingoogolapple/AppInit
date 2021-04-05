#!/bin/bash +x

# 发布两个测试模块到本地仓库
sed -i -e "s/include ':buildSrc'/\/\/include ':buildSrc'/" settings.gradle
./gradlew :module3:clean :module3:build :module3:uploadArchives -PpublishLib
./gradlew :module1:clean :module1:build :module1:uploadArchives -PpublishLib

# 打包并启动 demo
./gradlew clean assembleDebug
adb install -r demo/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n com.sankuai.erp.component.appinitdemo/com.sankuai.erp.component.appinitdemo.activity.MainActivity
