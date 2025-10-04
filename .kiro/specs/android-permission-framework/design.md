# Android权限请求框架 - 设计文档

## 概述

本设计文档基于需求文档，详细描述了Android权限请求框架的技术架构、组件设计、数据模型和实现策略。框架采用现代化的Android开发最佳实践，提供零依赖、高性能、易扩展的权限请求解决方案。

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Application Layer)                │
├─────────────────────────────────────────────────────────────┤
│                    API层 (API Layer)                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ PermissionManager│  │RequestBuilder   │  │  Callback API   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                   核心层 (Core Layer)                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │RequestExecutor  │  │  StateChecker   │  │SettingsLauncher │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                   处理层 (Handler Layer)                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │RationaleHandler │  │SettingsHandler  │  │PermissionContract│ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                  Android系统层 (System Layer)               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │Activity Result  │  │Permission API   │  │  Settings API   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 设计原则

1. **单一职责原则**：每个组件只负责一个特定功能
2. **开闭原则**：对扩展开放，对修改封闭
3. **依赖倒置原则**：依赖抽象而非具体实现
4. **接口隔离原则**：提供最小化的接口
5. **零依赖原则**：最小化外部依赖

## 组件和接口

### 1. API层组件

#### PermissionManager (门面类)
```kotlin
class PermissionManager {
    companion object {
        fun with(activity: ComponentActivity): PermissionRequestBuilder
        fun with(fragment: Fragment): PermissionRequestBuilder
    }
}
```

**职责**：
- 提供统一的入口点
- 创建权限请求构建器
- 管理全局配置

#### PermissionRequestBuilder (构建器)
```kotlin
class PermissionRequestBuilder {
    fun permission(permission: String): PermissionRequestBuilder
    fun permissions(vararg permissions: String): PermissionRequestBuilder
    fun rationale(rationale: String): PermissionRequestBuilder
    fun onGranted(callback: (Array<String>) -> Unit): PermissionRequestBuilder
    fun onDenied(callback: (Array<String>, Array<String>) -> Unit): PermissionRequestBuilder
    fun onPermanentlyDenied(callback: (Array<String>) -> Unit): PermissionRequestBuilder
    fun request()
}
```

**职责**：
- 提供链式调用API
- 收集权限请求参数
- 创建并执行权限请求

### 2. 核心层组件

#### PermissionRequestExecutor (执行器)
```kotlin
class PermissionRequestExecutor {
    fun execute(request: PermissionRequest)
    private fun checkPermissionStates()
    private fun requestPermissions()
    private fun handleResults()
}
```

**职责**：
- 执行权限请求逻辑
- 管理Activity Result Launcher
- 处理权限请求结果

#### PermissionStateChecker (状态检查器)
```kotlin
object PermissionStateChecker {
    fun checkPermissionState(context: Context, permission: String): PermissionState
    fun checkPermissionsState(context: Context, permissions: Array<String>): Map<String, PermissionState>
    fun areAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean
}
```

**职责**：
- 检查权限状态
- 区分权限状态类型
- 提供权限统计信息

#### AppSettingsLauncher (设置启动器)
```kotlin
class AppSettingsLauncher {
    fun initialize(activity: ComponentActivity, callback: (() -> Unit)?)
    fun initialize(fragment: Fragment, callback: (() -> Unit)?)
    fun launch(context: Context)
}
```

**职责**：
- 管理设置页面跳转
- 监听设置页面返回
- 提供设置页面工具方法

### 3. 处理层组件

#### RationaleHandler (解释处理器)
```kotlin
interface RationaleHandler {
    fun showRationale(context: Context, request: PermissionRequest, callback: RationaleCallback)
}

class DefaultRationaleHandler : RationaleHandler {
    // 使用AlertDialog实现
}
```

**职责**：
- 显示权限解释对话框
- 处理用户选择
- 支持自定义UI实现

#### SettingsHandler (设置处理器)
```kotlin
interface SettingsHandler {
    fun showSettingsDialog(context: Context, request: PermissionRequest, callback: SettingsCallback)
}

class DefaultSettingsHandler : SettingsHandler {
    // 使用AlertDialog实现
}
```

**职责**：
- 显示设置跳转对话框
- 处理用户选择
- 支持自定义UI实现

## 数据模型

### PermissionRequest (权限请求实体)
```kotlin
data class PermissionRequest(
    val permissions: Array<String>,
    val rationale: String? = null,
    val rationaleTitle: String? = null,
    val positiveButtonText: String = "确定",
    val negativeButtonText: String = "取消",
    val settingsText: String? = null,
    val settingsTitle: String? = null,
    val forceGoToSettings: Boolean = false,
    val callback: PermissionCallback? = null
)
```

### PermissionState (权限状态枚举)
```kotlin
enum class PermissionState {
    GRANTED,           // 已授权
    DENIED,            // 首次拒绝
    PERMANENTLY_DENIED // 永久拒绝
}
```

### PermissionCallback (回调接口)
```kotlin
interface PermissionCallback {
    fun onBeforeRequest(permissions: Array<String>)
    fun onGranted(permissions: Array<String>)
    fun onDenied(deniedPermissions: Array<String>, permanentlyDeniedPermissions: Array<String>)
    fun onPermanentlyDenied(permanentlyDeniedPermissions: Array<String>)
}
```

### PermissionStats (权限统计)
```kotlin
data class PermissionStats(
    val totalCount: Int,
    val grantedCount: Int,
    val deniedCount: Int,
    val grantedPermissions: Array<String>,
    val deniedPermissions: Array<String>
) {
    val allGranted: Boolean
    val allDenied: Boolean
    val partiallyGranted: Boolean
}
```

## 错误处理策略

### 1. 异常分类

#### 系统异常
- `SecurityException`：权限相关安全异常
- `IllegalStateException`：状态异常（如Activity已销毁）
- `IllegalArgumentException`：参数异常

#### 业务异常
- `PermissionRequestTimeoutException`：权限请求超时
- `PermissionRequestCancelledException`：权限请求被取消

### 2. 错误处理机制

#### 异常熔断
```kotlin
class PermissionRequestExecutor {
    private var isDestroyed = false
    
    private fun checkLifecycleState() {
        if (isDestroyed) {
            throw IllegalStateException("Cannot request permissions after lifecycle destroyed")
        }
    }
}
```

#### 超时处理
```kotlin
class PermissionRequestExecutor {
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        handleTimeout()
    }
    
    private fun startTimeout() {
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DURATION)
    }
}
```

#### 内存泄漏预防
```kotlin
class PermissionRequestExecutor {
    override fun onDestroy() {
        cleanup()
    }
    
    private fun cleanup() {
        currentRequest = null
        timeoutHandler.removeCallbacks(timeoutRunnable)
        // 清理其他资源
    }
}
```

## 测试策略

### 1. 单元测试

#### 测试覆盖范围
- 权限状态检查逻辑
- 权限请求构建器
- 数据模型验证
- 工具类方法

#### 测试工具
- JUnit 4
- Mockito
- Robolectric (Android组件测试)

#### 测试示例
```kotlin
@Test
fun `checkPermissionState should return GRANTED when permission is granted`() {
    // Given
    mockStatic(ContextCompat::class.java).use { contextCompatMock ->
        contextCompatMock.`when`<Int> { 
            ContextCompat.checkSelfPermission(context, testPermission) 
        }.thenReturn(PackageManager.PERMISSION_GRANTED)
        
        // When
        val state = PermissionStateChecker.checkPermissionState(context, testPermission)
        
        // Then
        assertEquals(PermissionState.GRANTED, state)
    }
}
```

### 2. 集成测试

#### 测试场景
- 完整的权限请求流程
- Activity和Fragment上下文
- 权限解释和设置跳转
- 多权限批量请求

#### 测试工具
- Espresso
- UI Automator
- 模拟器自动化

### 3. 兼容性测试

#### 测试范围
- Android 6.0 - 14 全版本
- 不同设备厂商
- 不同屏幕尺寸

#### 测试策略
- 自动化测试脚本
- 云测试平台
- 真机测试

## 性能优化

### 1. 启动性能

#### 懒加载初始化
```kotlin
class PermissionManager {
    companion object {
        private val lazyInitializer by lazy {
            // 初始化逻辑
        }
    }
}
```

#### 避免反射
- 使用编译时注解处理
- 避免运行时反射调用
- 使用内联函数优化

### 2. 内存优化

#### 弱引用管理
```kotlin
class PermissionRequestExecutor {
    private var activityRef: WeakReference<ComponentActivity>? = null
    private var fragmentRef: WeakReference<Fragment>? = null
}
```

#### 资源及时释放
```kotlin
class PermissionRequestExecutor {
    private fun cleanup() {
        activityRef?.clear()
        fragmentRef?.clear()
        currentRequest = null
    }
}
```

### 3. 包体积优化

#### ProGuard规则
```proguard
# 保留公共API
-keep public class com.cairong.permission.PermissionManager {
    public *;
}

# 移除未使用代码
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
```

#### 资源优化
- 最小化字符串资源
- 避免不必要的drawable
- 使用vector drawable

## 扩展性设计

### 1. SPI接口

#### PermissionInterceptor (权限拦截器)
```kotlin
interface PermissionInterceptor {
    fun beforeRequest(permissions: Array<String>): Boolean
    fun afterRequest(permissions: Array<String>, results: Map<String, Boolean>)
}
```

#### 注册机制
```kotlin
object PermissionInterceptorRegistry {
    private val interceptors = mutableListOf<PermissionInterceptor>()
    
    fun register(interceptor: PermissionInterceptor)
    fun unregister(interceptor: PermissionInterceptor)
}
```

### 2. 配置系统

#### 全局配置
```kotlin
object PermissionConfig {
    var defaultRationaleHandler: RationaleHandler = DefaultRationaleHandler()
    var defaultSettingsHandler: SettingsHandler = DefaultSettingsHandler()
    var enableLogging: Boolean = false
    var requestTimeout: Long = 30_000L
}
```

#### 本地化支持
```kotlin
object PermissionStrings {
    var rationaleTitle: String = "权限说明"
    var settingsTitle: String = "权限设置"
    var positiveButton: String = "确定"
    var negativeButton: String = "取消"
}
```

### 3. 协程扩展

#### 扩展模块设计
```kotlin
// permission-coroutine模块
suspend fun PermissionRequestBuilder.await(): Boolean {
    return suspendCoroutine { continuation ->
        onResult { allGranted, _, _ ->
            continuation.resume(allGranted)
        }
        request()
    }
}

fun observePermissionState(context: Context, permission: String): Flow<PermissionState> {
    return callbackFlow {
        // 实现权限状态监听
    }
}
```

## Android版本适配

### 1. Android 14+ 新特性

#### 部分媒体权限
```kotlin
object PermissionGroups {
    val MEDIA_PERMISSIONS_ANDROID_13 = arrayOf(
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO"
    )
}
```

#### 通知权限
```kotlin
object PermissionCompat {
    fun getNotificationPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            "android.permission.POST_NOTIFICATIONS"
        } else {
            null
        }
    }
}
```

### 2. 版本兼容处理

#### 权限检查适配
```kotlin
object PermissionStateChecker {
    fun checkPermissionState(context: Context, permission: String): PermissionState {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> PermissionState.GRANTED
            else -> checkRuntimePermission(context, permission)
        }
    }
}
```

#### API降级处理
```kotlin
object PermissionCompat {
    fun shouldShowRequestPermissionRationale(
        activity: Activity, 
        permission: String
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        } else {
            false
        }
    }
}
```

## 安全考虑

### 1. 权限验证

#### 权限名称验证
```kotlin
object PermissionValidator {
    private val VALID_PERMISSIONS = setOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        // ... 其他有效权限
    )
    
    fun validatePermission(permission: String): Boolean {
        return VALID_PERMISSIONS.contains(permission)
    }
}
```

### 2. 防护机制

#### 频率限制
```kotlin
class PermissionRateLimiter {
    private val requestTimes = mutableMapOf<String, Long>()
    
    fun canRequest(permission: String): Boolean {
        val lastRequestTime = requestTimes[permission] ?: 0
        val currentTime = System.currentTimeMillis()
        return currentTime - lastRequestTime > MIN_REQUEST_INTERVAL
    }
}
```

#### 恶意请求检测
```kotlin
class PermissionAbuseDetector {
    private val requestCounts = mutableMapOf<String, Int>()
    
    fun checkAbuse(permission: String): Boolean {
        val count = requestCounts.getOrDefault(permission, 0)
        return count > MAX_REQUESTS_PER_HOUR
    }
}
```

## 总结

本设计文档详细描述了Android权限请求框架的技术架构和实现策略。框架采用分层架构设计，确保了代码的可维护性和可扩展性。通过合理的组件划分、完善的错误处理、全面的测试策略和性能优化，框架能够提供稳定、高效、易用的权限请求解决方案。

设计的核心优势：
1. **零依赖**：最小化外部依赖，避免版本冲突
2. **高性能**：优化启动时间和内存使用
3. **易扩展**：提供SPI接口和配置系统
4. **强兼容**：支持Android 6.0-14全版本
5. **高可靠**：完善的错误处理和测试覆盖