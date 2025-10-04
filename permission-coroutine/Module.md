# Module permission-coroutine

Android 权限请求框架协程扩展模块

## 概述

这是 Android 权限请求框架的 Kotlin 协程扩展模块，提供了基于协程的权限请求 API。

## 主要特性

- **协程支持**：使用 `suspend` 函数进行权限请求
- **Flow 支持**：权限状态监听
- **异常处理**：使用异常来处理权限拒绝情况
- **取消支持**：支持协程取消

## 核心扩展函数

### PermissionRequest.await()
将权限请求转换为挂起函数，可以在协程中使用。

### PermissionRequest.asFlow()
将权限请求转换为 Flow，可以监听权限状态变化。

## 异常类型

### PermissionDeniedException
当权限被拒绝时抛出的异常。

### PermissionPermanentlyDeniedException
当权限被永久拒绝时抛出的异常。

## 使用示例

### 基本用法

```kotlin
try {
    val result = PermissionRequest.Builder(this)
        .permissions(Manifest.permission.CAMERA)
        .rationale("需要相机权限")
        .build()
        .await()
    
    if (result.isGranted) {
        // 权限已授权
        startCamera()
    }
} catch (e: PermissionDeniedException) {
    // 权限被拒绝
    showToast("权限被拒绝")
} catch (e: PermissionPermanentlyDeniedException) {
    // 权限被永久拒绝
    showToast("权限被永久拒绝，请到设置页面开启")
}
```

### Flow 用法

```kotlin
PermissionRequest.Builder(this)
    .permissions(Manifest.permission.CAMERA)
    .build()
    .asFlow()
    .collect { state ->
        when (state) {
            is PermissionState.Granted -> startCamera()
            is PermissionState.Denied -> showToast("权限被拒绝")
            is PermissionState.PermanentlyDenied -> showSettingsDialog()
        }
    }
```