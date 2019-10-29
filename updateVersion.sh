#!/bin/bash +x

OLD_VERSION="1.0.5"
NEW_VERSION="1.0.6"

sed -i -e "s/POM_VERSION_NAME=$OLD_VERSION/POM_VERSION_NAME=$NEW_VERSION/" gradle.properties
sed -i -e "s/VERSION_NAME = \"$OLD_VERSION\"/VERSION_NAME = \"$NEW_VERSION\"/" buildSrc/src/main/groovy/com/sankuai/erp/component/plugin/appinit/AppInitPlugin.groovy
sed -i -e "s/appinit-plugin:$OLD_VERSION/appinit-plugin:$NEW_VERSION/" build.gradle
