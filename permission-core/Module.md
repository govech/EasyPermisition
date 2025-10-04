# Module permission-core

Android 权限请求框架核心模块

## 概述

这是一个零依赖、可复用、支持链式调用的 Android 权限请求框架的核心模块。

## 主要特性

- **零依赖**：仅依赖 androidx.activity:activity-ktx
- **链式调用**：流畅的 API 设计
- **智能处理**：自动处理权限状态和用户交互
- **可扩展**：支持自定义 UI 和拦截器

## 核心类

### PermissionRequest
权限请求的核心类，使用建造者模式创建权限请求。

### PermissionCallback
权限请求结果回调接口，包含四个回调方法：
- `onGranted()` - 权限已授权
- `onDenied(List<String>)` - 权限被拒绝
- `onPermanentlyDenied(List<String>)` - 权限被永久拒绝
- `onBeforeRequest()` - 权限请求前

### PermissionState
权限状态枚举，表示权限的当前状态。

### RationaleHandler
权限解释处理器接口，用于自定义权限解释 UI。

## 使用示例

```kotlin
PermissionRequest.Builder(this)
    .permissions(Manifest.permission.CAMERA)
    .rationale("需要相机权限来拍照")
    .callback(object : PermissionCallback {
        override fun onGranted() {
            // 权限已授权
        }
        override fun onDenied(deniedPermissions: List<String>) {
            // 权限被拒绝
        }
        override fun onPermanentlyDenied(permanentlyDeniedPermissions: List<String>) {
            // 权限被永久拒绝
        }
        override fun onBeforeRequest() {
            // 权限请求前
        }
    })
    .build()
    .request()
```