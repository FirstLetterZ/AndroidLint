Android Lint 工具库
=========
### 主要内容包括：
自定义lint规则：lintrule
lint规则依赖：lintLib
lint配置插件：pluginLint

项目内的各项依赖库版本由[build](./build.gradle)文件统一管理

工具库发布
---------
#### 单项目发布
lintLib：必须先rebuild project再执行发布1命令
>./gradlew -p lintLib install bintrayUpload --info
./gradlew -p pluginLint clean install bintrayUpload --info
