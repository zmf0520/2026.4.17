# 快速开始指南

## 📱 项目已创建完成！

您的SSH文件管理器应用已成功创建，位于：
```
d:\4.11\CloudFileManager\
```

## 🚀 如何构建和运行

### 方法一：使用 Android Studio（推荐）

1. **安装 Android Studio**
   - 下载地址：https://developer.android.com/studio
   - 版本要求：Hedgehog (2023.1.1) 或更高

2. **打开项目**
   ```
   Android Studio -> File -> Open -> 选择 d:\4.11\CloudFileManager
   ```

3. **等待 Gradle 同步**
   - 首次打开会自动下载依赖
   - 需要网络连接

4. **运行应用**
   - 连接安卓设备或启动模拟器
   - 点击绿色运行按钮 ▶️
   - 或使用快捷键：Shift + F10

### 方法二：命令行构建

如果您已安装 Android SDK：

```powershell
cd d:\4.11\CloudFileManager

# 设置环境变量
$env:ANDROID_HOME = "C:\Users\您的用户名\AppData\Local\Android\Sdk"

# 构建
.\gradlew.bat assembleDebug

# APK 位置
# app\build\outputs\apk\debug\app-debug.apk
```

## 📋 配置您的服务器

### 首次运行

1. 打开应用
2. 点击"添加服务器"
3. 填写信息：

```
服务器名称：我的腾讯云
主机地址：42.193.101.14
端口：22
用户名：ubuntu
认证方式：密钥
私钥路径：/sdcard/zmfkey.pem
自动连接：✓ 勾选
```

### 传输密钥文件到设备

```bash
# 使用 adb 传输
adb push C:\Users\18453\Desktop\zmfkey.pem /sdcard/

# 或通过其他方式（邮件、云盘等）传输到设备
```

## 🎯 功能说明

### 已实现功能

| 功能 | 状态 | 说明 |
|------|------|------|
| SSH连接 | ✅ | 支持密码和密钥认证 |
| 文件浏览 | ✅ | 列表视图，支持导航 |
| 文件查看 | ✅ | 文本文件内容预览 |
| 文件删除 | ✅ | 支持删除文件和文件夹 |
| 新建文件夹 | ✅ | 创建新目录 |
| 自动连接 | ✅ | 启动时自动连接 |
| 平板适配 | ✅ | 双栏布局 |

### 待实现功能

- [ ] 文件上传/下载
- [ ] 文件编辑
- [ ] 文件重命名
- [ ] 多服务器管理
- [ ] 文件搜索

## 📱 平板优化

应用会自动检测屏幕尺寸：
- **手机**（< 600dp）：单栏布局
- **平板**（≥ 600dp）：双栏布局
  - 左侧：文件列表
  - 右侧：文件详情

## 🔧 故障排除

### 构建失败

1. **Gradle 下载失败**
   - 检查网络连接
   - 配置国内镜像源

2. **SDK 未找到**
   - 在 Android Studio 中：File -> Project Structure -> SDK Location
   - 设置正确的 SDK 路径

3. **依赖下载慢**
   - 在 `settings.gradle.kts` 中添加阿里云镜像：
   ```kotlin
   repositories {
       maven { url = uri("https://maven.aliyun.com/repository/google") }
       maven { url = uri("https://maven.aliyun.com/repository/central") }
       google()
       mavenCentral()
   }
   ```

### 连接失败

1. **检查网络**
   - 确保设备可以访问服务器IP
   - 检查防火墙设置

2. **检查密钥文件**
   - 确保密钥文件路径正确
   - 检查文件权限

3. **查看日志**
   - 在 Android Studio 的 Logcat 中查看错误信息
   - 过滤标签：`SSHManager`

## 📞 需要帮助？

如果遇到问题，请提供：
1. 错误截图
2. Android Studio 版本
3. 设备信息（型号、系统版本）
4. Logcat 日志

---

**祝您使用愉快！** 🎉
