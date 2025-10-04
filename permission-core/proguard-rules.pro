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

# 避免警告
-dontwarn com.cairong.permission.**