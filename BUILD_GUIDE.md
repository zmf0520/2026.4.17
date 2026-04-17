# 使用国内镜像构建APK

## 方案一：使用国内镜像源

### 步骤1：下载Android SDK（使用国内镜像）

```powershell
# 在PowerShell中运行
$sdkDir = "d:\4.11\android-sdk"
New-Item -ItemType Directory -Path $sdkDir -Force

# 使用清华大学镜像
$cmdlineToolsUrl = "https://mirrors.tuna.tsinghua.edu.cn/android/repository/commandlinetools-win-11076708_latest.zip"
$zipFile = "$sdkDir\cmdline-tools.zip"

Invoke-WebRequest -Uri $cmdlineToolsUrl -OutFile $zipFile
Expand-Archive -Path $zipFile -DestinationPath "$sdkDir\cmdline-tools"
Rename-Item "$sdkDir\cmdline-tools\cmdline-tools" "latest"
```

### 步骤2：设置环境变量

```powershell
$env:ANDROID_HOME = "d:\4.11\android-sdk"
$env:ANDROID_SDK_ROOT = "d:\4.11\android-sdk"
```

### 步骤3：安装SDK组件

```powershell
$cmdlineTools = "d:\4.11\android-sdk\cmdline-tools\latest\bin"

# 接受许可
& "$cmdlineTools\sdkmanager.bat" --licenses

# 安装必要组件
& "$cmdlineTools\sdkmanager.bat" "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 步骤4：构建APK

```powershell
cd d:\4.11\CloudFileManager
.\gradlew.bat assembleDebug
```

---

## 方案二：使用Android Studio（最简单）

### 1. 下载Android Studio
- 官网：https://developer.android.com/studio
- 国内镜像：https://developer.android.google.cn/studio

### 2. 安装后打开项目
```
File -> Open -> 选择 d:\4.11\CloudFileManager
```

### 3. 等待自动下载SDK和依赖

### 4. 点击运行按钮 ▶️

---

## 方案三：在线构建（无需安装）

### 使用GitHub Actions自动构建

1. 将项目上传到GitHub
2. 添加 `.github/workflows/build.yml`：

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Gradle
      uses: gradle/gradle-build-action@v2
    
    - name: Build APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

3. 在Actions页面下载构建好的APK

---

## 方案四：直接使用ConnectBot

您已经有ConnectBot APK，可以直接使用：

### 安装步骤

1. 安装 `ConnectBot-v1.9.12-oss.apk`
2. 打开应用
3. 添加主机：
   - 主机：ubuntu@42.193.101.14:22
   - 认证：选择密钥文件

### ConnectBot功能

- ✅ SSH连接
- ✅ 终端操作
- ✅ 密钥认证
- ✅ 保存连接

---

## 推荐方案

**最快方案**：使用Android Studio
- 自动处理SDK下载
- 自动处理依赖
- 图形化界面，简单易用

**最省事方案**：直接使用ConnectBot
- 您已有APK
- 功能完整
- 立即可用

**最灵活方案**：GitHub Actions在线构建
- 无需安装任何工具
- 自动化构建
- 可随时下载APK

---

## 需要帮助？

告诉我您选择哪个方案，我可以提供详细的步骤指导！
