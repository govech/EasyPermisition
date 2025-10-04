# 消费者ProGuard规则

# 保留协程扩展的公共API
-keep public class com.cairong.permission.coroutine.** {
    public *;
}

# 保留协程相关类
-keep class kotlinx.coroutines.** { *; }