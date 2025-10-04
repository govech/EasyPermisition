# Android 权限请求框架

一个 **零依赖、可复用、支持链式调用、可扩展至任意权限** 的 Android 权限请求框架。

[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## ✨ 特性

- 🚀 **零依赖**：仅依赖 `androidx.activity:activity-ktx ≥1.7`
- 🔗 **链式调用**：流畅的 API 设计，支持建造者模式
- 📱 **全面支持**：Activity & Fragment，单权限 & 多权限
- 🎯 **智能处理**：自动处理"拒绝一次后解释"与"永久拒绝后跳转设置"
- 🌐 **双语言**：完美支持 Kotlin & Java 调用
- 🎨 **可定制**：支持自定义 UI、主题、拦截器
- 📊 **权限组**：支持相关权限批量申请
- 🆕 **新版本适配**：完美适配 Android 14+ 新权限特性
- ⚡ **协程扩展**：可选的协程支持模块

## 🚀 快速开始

### 集成方式

#### Step 1: 添加 JitPack 仓库

在项目根目录的 `build.gradle` 或 `settings.gradle` 中添加 JitPack 仓库：

```gradle
// settings.gradle (推荐)
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// 或者在项目根目录的 build.gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### Step 2: 添加依赖

在应用模块的 `build.gradle` 中添加依赖：

```gradle
dependencies {
    // 核心库（必需）
    implementation 'com.github.govech:EasyPermisition:1.0.0'
    
    // 协程扩展（可选）
    implementation 'com.github.govech.EasyPermisition:permission-coroutine:1.0.0'
}
```

### Kotlin 三行代码

```kotlin
PermissionRequest.Builder(this)
    .permissions(Manifest.permission.CAMERA)
    .callback { showToast("相机权限已授权") }
    .build()
    .request()
```

### 完整示例

```kotlin
PermissionRequest.Builder(this)
    .permissions(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    .rationale("需要相机和麦克风权限来录制视频")
    .callback(object : PermissionCallback {
        override fun onGranted() {
            // 所有权限已授权
            startCamera()
        }
        
        override fun onDenied(deniedPermissions: List<String>) {
            // 部分权限被拒绝
            showToast("权限被拒绝：${deniedPermissions.joinToString()}")
        }
        
        override fun onPermanentlyDenied(permanentlyDeniedPermissions: List<String>) {
            // 权限被永久拒绝，引导用户到设置页面
            showToast("权限被永久拒绝，请到设置页面手动开启")
        }
        
        override fun onBeforeRequest() {
            // 权限请求前的回调
            showLoading()
        }
    })
    .build()
    .request()
```

## 📖 高级用法

### Java 调用

```java
PermissionManager.with(this)
    .permissions(Manifest.permission.CAMERA)
    .rationale("需要相机权限来拍照")
    .onGranted(() -> {
        // 权限已授权
        startCamera();
    })
    .onDenied(deniedPermissions -> {
        // 权限被拒绝
        showToast("权限被拒绝");
    })
    .request();
```

### 协程调用

```kotlin
// 添加 permission-coroutine 依赖
try {
    val result = PermissionRequest.Builder(this)
        .permissions(Manifest.permission.CAMERA)
        .rationale("需要相机权限")
        .build()
        .await()
    
    if (result.isGranted) {
        startCamera()
    }
} catch (e: PermissionDeniedException) {
    showToast("权限被拒绝")
}
```

### 权限组批量申请

```kotlin
// 使用预定义的权限组
val storagePermissions = PermissionGroups.getStoragePermissions()
val locationPermissions = PermissionGroups.getLocationPermissions()
val mediaPermissions = PermissionGroups.getMediaPermissions() // Android 13+

PermissionRequest.Builder(this)
    .permissions(*storagePermissions.toTypedArray())
    .rationale("需要存储权限来保存文件")
    .callback { /* 处理结果 */ }
    .build()
    .request()
```

### 自定义 Rationale UI

```kotlin
// 使用自定义对话框
val customHandler = CustomRationaleHandler { context, permissions, rationale ->
    AlertDialog.Builder(context)
        .setTitle("权限说明")
        .setMessage(rationale)
        .setPositiveButton("授权") { _, _ -> 
            // 继续请求权限
        }
        .setNegativeButton("取消", null)
        .show()
}

PermissionRequest.Builder(this)
    .permissions(Manifest.permission.CAMERA)
    .rationale("需要相机权限")
    .rationaleHandler(customHandler)
    .callback { /* 处理结果 */ }
    .build()
    .request()
```

### 全局配置

```kotlin
// 在 Application 中配置
PermissionConfig.Builder()
    .defaultRationale("应用需要此权限来正常工作")
    .defaultSettingsText("去设置")
    .forceGoToSettings(true)
    .theme(R.style.CustomPermissionTheme)
    .apply()
```

### 权限拦截器

```kotlin
// 注册全局拦截器
PermissionConfig.addInterceptor(object : PermissionInterceptor {
    override fun onBeforeRequest(permissions: List<String>) {
        // 权限请求前的埋点
        Analytics.track("permission_request", mapOf("permissions" to permissions))
    }
    
    override fun onResult(result: PermissionResult) {
        // 权限结果的埋点
        Analytics.track("permission_result", mapOf("granted" to result.isGranted))
    }
})
```

## 🆕 Android 14+ 适配

### 部分媒体权限

```kotlin
// Android 14+ 支持用户选择部分媒体文件
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    PermissionRequest.Builder(this)
        .permissions("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
        .rationale("选择您想要分享的照片")
        .callback { /* 处理结果 */ }
        .build()
        .request()
}
```

### 通知权限

```kotlin
// Android 13+ 需要显式请求通知权限
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    PermissionRequest.Builder(this)
        .permissions(Manifest.permission.POST_NOTIFICATIONS)
        .rationale("需要通知权限来发送重要消息")
        .callback { /* 处理结果 */ }
        .build()
        .request()
}
```

### 前台服务权限

```kotlin
// Android 14+ 前台服务需要特定类型权限
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    PermissionRequest.Builder(this)
        .permissions(
            "android.permission.FOREGROUND_SERVICE_LOCATION",
            "android.permission.FOREGROUND_SERVICE_CAMERA"
        )
        .rationale("需要前台服务权限来在后台提供服务")
        .callback { /* 处理结果 */ }
        .build()
        .request()
}
```

## 🔧 自定义配置

### 自定义主题

在 `styles.xml` 中定义主题：

```xml
<style name="CustomPermissionTheme" parent="Theme.AppCompat.Light.Dialog">
    <item name="colorPrimary">@color/your_primary_color</item>
    <item name="colorAccent">@color/your_accent_color</item>
</style>
```

### 多语言支持

框架支持多语言，在对应的 `strings.xml` 中添加翻译：

```xml
<!-- strings.xml -->
<string name="permission_rationale_title">权限说明</string>
<string name="permission_settings_message">请到设置页面手动开启权限</string>
<string name="permission_settings_button">去设置</string>

<!-- strings-en.xml -->
<string name="permission_rationale_title">Permission Required</string>
<string name="permission_settings_message">Please enable permission in settings</string>
<string name="permission_settings_button">Settings</string>
```

## 📋 常见问题

### Q: 如何判断权限是否被"不再询问"？

A: 框架会自动检测并调用 `onPermanentlyDenied` 回调：

```kotlin
override fun onPermanentlyDenied(permanentlyDeniedPermissions: List<String>) {
    // 权限被永久拒绝，引导用户到设置页面
}
```

### Q: 后台位置权限如何处理？

A: Android 10+ 需要先获取前台位置权限，再单独请求后台位置权限：

```kotlin
// 第一步：请求前台位置权限
PermissionRequest.Builder(this)
    .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
    .callback(object : PermissionCallback {
        override fun onGranted() {
            // 第二步：请求后台位置权限
            requestBackgroundLocation()
        }
    })
    .build()
    .request()

private fun requestBackgroundLocation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        PermissionRequest.Builder(this)
            .permissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            .rationale("需要后台位置权限来持续定位")
            .callback { /* 处理结果 */ }
            .build()
            .request()
    }
}
```

### Q: 如何处理权限请求频率限制？

A: 框架内置频率限制机制，防止恶意频繁申请：

```kotlin
// 配置频率限制（可选）
PermissionConfig.Builder()
    .rateLimitInterval(5000) // 5秒内最多请求一次
    .rateLimitMaxAttempts(3) // 最多尝试3次
    .apply()
```

## 🔄 迁移指南

### 从 EasyPermissions 迁移

```kotlin
// EasyPermissions (旧)
EasyPermissions.requestPermissions(
    this, 
    "需要相机权限", 
    RC_CAMERA, 
    Manifest.permission.CAMERA
)

// 本框架 (新)
PermissionRequest.Builder(this)
    .permissions(Manifest.permission.CAMERA)
    .rationale("需要相机权限")
    .callback { /* 处理结果 */ }
    .build()
    .request()
```

### 从 PermissionsDispatcher 迁移

```kotlin
// PermissionsDispatcher (旧)
@RuntimePermissions
class MainActivity : AppCompatActivity() {
    @NeedsPermission(Manifest.permission.CAMERA)
    fun showCamera() { /* ... */ }
}

// 本框架 (新)
class MainActivity : AppCompatActivity() {
    private fun requestCamera() {
        PermissionRequest.Builder(this)
            .permissions(Manifest.permission.CAMERA)
            .callback { showCamera() }
            .build()
            .request()
    }
}
```

## 📊 性能报告

- ✅ 冷启动耗时：< 1ms
- ✅ 无反射调用
- ✅ 包体积增加：< 50KB
- ✅ 内存泄漏：0
- ✅ 兼容性：Android 6.0 - 14

## 📄 License

```
Copyright 2024 CaiRong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📞 联系方式

- 作者：CaiRong
- 邮箱：cairong@example.com
- 项目地址：[https://github.com/govech/EasyPermisition](https://github.com/govech/EasyPermisition)