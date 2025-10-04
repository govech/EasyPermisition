# 消费者ProGuard规则
# 这些规则会自动应用到使用此库的项目中

# 保留权限框架的公共API
-keep public class com.cairong.permission.** {
    public *;
}

# 保留回调接口的所有方法
-keep interface com.cairong.permission.** {
    *;
}