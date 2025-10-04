# 协程扩展模块ProGuard规则

# 保留协程扩展API
-keep public class com.cairong.permission.coroutine.** {
    public *;
}

# 保留协程相关类
-keep class kotlinx.coroutines.** { *; }

# 避免警告
-dontwarn com.cairong.permission.coroutine.**