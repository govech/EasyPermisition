# Android权限请求框架 ProGuard规则

# 保留公共API类和方法
-keep public class com.cairong.permission.PermissionManager {
    public *;
}

-keep public class com.cairong.permission.PermissionRequestBuilder {
    public *;
}

# 保留回调接口
-keep public interface com.cairong.permission.PermissionCallback {
    *;
}

-keep public interface com.cairong.permission.SimplePermissionCallback {
    *;
}

# 保留处理器接口
-keep public interface com.cairong.permission.RationaleHandler {
    *;
}

-keep public interface com.cairong.permission.SettingsHandler {
    *;
}

# 保留枚举类
-keepclassmembers enum com.cairong.permission.PermissionState {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留数据类的字段
-keep public class com.cairong.permission.PermissionRequest {
    public *;
}

# 保留工具类的静态方法
-keep public class com.cairong.permission.PermissionStateChecker {
    public static *;
}

-keep public class com.cairong.permission.AppSettingsLauncher {
    public static *;
}

# 保留默认实现类
-keep public class com.cairong.permission.DefaultRationaleHandler {
    public <init>(...);
    public *;
}

-keep public class com.cairong.permission.DefaultSettingsHandler {
    public <init>(...);
    public *;
}

# 保留Activity Result Contract相关类
-keep class * extends androidx.activity.result.contract.ActivityResultContract {
    *;
}

# 保留性能监控相关类
-keep public class com.cairong.permission.performance.PerformanceMonitor {
    public *;
}

-keep public class com.cairong.permission.performance.PerformanceMonitor$* {
    *;
}

# 保留分析统计相关类
-keep public class com.cairong.permission.analytics.PermissionAnalytics {
    public *;
}

-keep public class com.cairong.permission.analytics.PermissionAnalytics$* {
    *;
}

# 保留兼容性检查相关类
-keep public class com.cairong.permission.compatibility.CompatibilityChecker {
    public *;
}

-keep public class com.cairong.permission.compatibility.CompatibilityChecker$* {
    *;
}

# 保留内存检测相关类
-keep public class com.cairong.permission.memory.MemoryLeakDetector {
    public *;
}

# 保留熔断器相关类
-keep public class com.cairong.permission.circuit.PermissionCircuitBreaker {
    public *;
}

-keep public class com.cairong.permission.circuit.CircuitBreakerManager {
    public *;
}

# 保留注解
-keep @interface com.cairong.permission.performance.Measured

# 保留枚举类
-keepclassmembers enum com.cairong.permission.analytics.PermissionAnalytics$EventType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers enum com.cairong.permission.compatibility.CompatibilityChecker$PermissionFeature {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 避免警告
-dontwarn com.cairong.permission.**