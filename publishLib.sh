#!/bin/bash +x

if [ $# = 0 ]; then
    RELEASE_TASK="uploadArchives"
    echo "发布到本地测试"
else
    RELEASE_TASK="bintrayUpload"
    echo "发布到 JCenter"
fi

# 发布 Library 到仓库
./gradlew :common:clean :common:build :common:$RELEASE_TASK -PpublishLib
./gradlew :api:clean :api:build :api:$RELEASE_TASK -PpublishLib
./gradlew :compiler:clean :compiler:build :compiler:$RELEASE_TASK -PpublishLib
sed -i -e "s/DEBUG_APP_INIT_APT=true/DEBUG_APP_INIT_APT=false/" gradle.properties
./gradlew :buildSrc:clean :buildSrc:build :buildSrc:$RELEASE_TASK -PpublishLib

# 发布两个测试模块到本地仓库
sed -i -e "s/include ':buildSrc'/\/\/include ':buildSrc'/" settings.gradle
./gradlew :module3:clean :module3:build :module3:uploadArchives -PpublishLib
./gradlew :module1:clean :module1:build :module1:uploadArchives -PpublishLib

# 打包并启动 demo
./gradlew clean assembleDebug
adb install -r demo/app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.sankuai.erp.component.appinitdemo/com.sankuai.erp.component.appinitdemo.activity.MainActivity