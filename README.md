# 云文件管理器 (CloudFileManager)

一款专为安卓平板设计的SSH文件管理器，支持自动连接服务器并管理远程文件。

## 功能特性

✅ **一键连接** - 保存服务器配置，启动即连
✅ **文件浏览** - 直观的文件列表视图
✅ **文件操作** - 查看、删除、重命名、新建文件夹
✅ **平板优化** - 双栏布局，充分利用大屏幕
✅ **安全认证** - 支持密钥和密码认证
✅ **自动连接** - 应用启动时自动连接配置的服务器

## 技术栈

- **Kotlin** - 主要开发语言
- **Jetpack Compose** - 现代化UI框架
- **Material Design 3** - 设计规范
- **JSch** - SSH连接库
- **Coroutines** - 异步处理

## 项目结构

```
CloudFileManager/
├── app/
│   ├── src/main/
│   │   ├── java/com/cloudfilemanager/
│   │   │   ├── data/
│   │   │   │   └── Models.kt              # 数据模型
│   │   │   ├── ssh/
│   │   │   │   └── SSHManager.kt          # SSH连接管理
│   │   │   ├── ui/
│   │   │   │   ├── components/            # UI组件
│   │   │   │   ├── screens/               # 屏幕
│   │   │   │   └── FileManagerViewModel.kt # 视图模型
│   │   │   └── MainActivity.kt            # 主活动
│   │   └── res/                           # 资源文件
│   └── build.gradle.kts                   # 模块配置
├── gradle/                                # Gradle配置
└── build.gradle.kts                       # 项目配置
```

## 构建步骤

### 1. 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.5

### 2. 导入项目

```bash
# 在Android Studio中
File -> Open -> 选择 CloudFileManager 目录
```

### 3. 构建APK

```bash
# 命令行构建
cd CloudFileManager

# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

生成的APK位于：`app/build/outputs/apk/debug/app-debug.apk`

## 使用说明

### 首次使用

1. 安装并打开应用
2. 点击"添加服务器"按钮
3. 填写服务器信息：
   - 服务器名称：自定义名称
   - 主机地址：服务器IP或域名
   - 端口：SSH端口（默认22）
   - 用户名：SSH用户名
   - 认证方式：选择密码或密钥
   - 自动连接：勾选后下次启动自动连接

4. 点击"连接"按钮

### 文件操作

- **浏览文件**：点击文件夹进入
- **返回上级**：点击左上角返回按钮
- **查看文件**：点击文件查看内容（平板右侧详情栏）
- **删除文件**：选中文件后点击删除图标
- **新建文件夹**：点击右上角新建文件夹按钮

### 平板优化

- 屏幕宽度 ≥ 600dp 时自动启用双栏布局
- 左侧：文件列表
- 右侧：文件详情/内容预览

## 配置您的服务器

### 使用密钥认证（推荐）

1. 将私钥文件传输到设备：
   ```bash
   adb push zmfkey.pem /sdcard/
   ```

2. 在应用中配置：
   - 认证方式：选择"密钥"
   - 私钥文件路径：`/sdcard/zmfkey.pem`

### 使用密码认证

直接在配置界面输入密码即可。

## 已知限制

- 需要存储权限访问本地私钥文件
- 大文件操作可能需要较长时间
- 暂不支持文件上传/下载（后续版本添加）

## 后续计划

- [ ] 文件上传/下载功能
- [ ] 文件编辑功能
- [ ] 多服务器管理
- [ ] 文件搜索
- [ ] 文件排序和过滤
- [ ] 书签功能

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue。
