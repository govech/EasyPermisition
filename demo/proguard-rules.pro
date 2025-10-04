# Demo应用的ProGuard规则

# 保留权限框架的API
-keep class com.cairong.permission.** { *; }

# 保留相机相关类
-keep class androidx.camera.** { *; }

# 避免警告
-dontwarn com.cairong.permission.demo.**