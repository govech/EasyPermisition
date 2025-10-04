一个 **零依赖、可复用、支持链式调用、可扩展至任意权限** 的 Android 权限请求框架。

------

### 🎯 Milestone 1 ｜需求澄清 & 技术方案（Day 0）

- [ ] 1.1 明确框架边界（只处理运行时危险权限；不处理特殊权限如悬浮窗、通知、Root）
- [ ] 1.2 输出《框架需求清单》.md：
  – 支持 Activity & Fragment
  – 支持单权限 / 多权限
  – 支持“拒绝一次后解释”与“永久拒绝后跳转设置”
  – 支持 Kotlin & Java 调用
  – 链式回调：onGranted / onDenied / onPermanentlyDenied / onBeforeRequest
  – 零依赖（仅依赖 androidx.activity:activity-ktx ≥1.7）
- [ ] 1.3 技术选型确认：
  – Activity Result API（RequestPermission / RequestMultiplePermissions）
  – 不依赖 RxJava / Kotlin-Coroutine，但内部可挂协程扩展

------

### 🎯 Milestone 2 ｜核心架构搭建（Day 1-2）

- [ ] 2.1 新建 `permission-core` module，包名 `com.xxx.permission`
- [ ] 2.2 设计对外门面：`PermissionManager`（单例 + 建造者）
- [ ] 2.3 设计请求实体：`PermissionRequest`（权限数组、解释文案、回调）
- [ ] 2.4 设计回调接口：`PermissionCallback`（4 个方法见 1.2）
- [ ] 2.5 设计解释器接口：`RationaleHandler`（默认实现：AlertDialog）
- [ ] 2.6 设计跳转设置封装：`AppSettingsLauncher`
- [ ] 2.7 单元测试覆盖核心分支 ≥ 80%（Jacoco 报告）

------

### 🎯 Milestone 3 ｜单权限链路跑通（Day 2-3）

- [ ] 3.1 封装 `SinglePermissionContract`（ActivityResultContract<String,Boolean>）
- [ ] 3.2 实现“检查→解释→请求→回调”完整链
- [ ] 3.3 Demo 模块新增 `CameraActivity`：
  – 点击按钮 → 请求 CAMERA → 授权成功打开预览
  – 拒绝一次弹出解释 → 再拒绝跳转设置
- [ ] 3.4 手动验收通过

------

### 🎯 Milestone 4 ｜多权限 & 批量拒绝场景（Day 3-4）

- [ ] 4.1 封装 `MultiplePermissionContract`（ActivityResultContract<Array<String>
- [ ] 4.2 设计结果聚合策略：
  – 全部 granted → onGranted
  – 部分拒绝 → onDenied(拒绝列表)
  – 只要有一个永久拒绝 → 额外回调 onPermanentlyDenied
- [ ] 4.3 Demo 新增 `LocationActivity`：一次性请求 ACCESS_FINE_LOCATION + BACKGROUND_LOCATION
- [ ] 4.4 自动化测试：Espresso 点击“仅允许前台”→ 断言后台权限进入永久拒绝路径

------

### 🎯 Milestone 5 ｜Java 兼容 & 协程扩展（Day 4-5）

- [ ] 5.1 新增 `PermissionManager.java` 同名门面，提供静态方法供 Java 链式调用
- [ ] 5.2 新增 `permission-coroutine` module，提供
  – `suspend fun PermissionRequest.await()`
- [ ] 5.3 Sample 分别用 Java 与 Kotlin-Coroutine 各写一个请求示例
- [ ] 5.4 API 稳定性评审：@JvmOverloads / @JvmStatic 检查无遗漏

------

### 🎯 Milestone 6 ｜可配置 & 可扩展（Day 5-6）

- [ ] 6.1 支持自定义主题：在 `RationaleHandler` 暴露 `themeResId` 参数
- [ ] 6.2 支持自定义解释 UI：允许接入外部 DialogFragment / BottomSheet
- [ ] 6.3 支持全局默认配置：
  – 默认解释文案、跳转设置按钮文案、是否强制跳转设置
- [ ] 6.4 提供 SPI 接口：其他模块可注册 `PermissionInterceptor` 做埋点 / 日志 / 灰度

------

### 🎯 Milestone 7 ｜Sample & 文档（Day 6-7）

- [ ] 7.1 Demo 工程覆盖 6 个典型场景：
  1. 单权限
  2. 多权限
  3. 永久拒绝跳转设置
  4. 自定义 Rationale UI
  5. Java 调用
  6. 协程调用
- [ ] 7.2 输出 README.md：
  – 集成方式（Gradle 依赖）
  – 快速开始（Kotlin 三行代码）
  – 高级配置（自定义 Handler、主题、拦截器）
  – 常见 QA（“不再询问”判断、后台权限、Android 14 新特性）

------

### 🎯 Milestone 8 ｜性能 & 稳定性收口（Day 7-8）

- [ ] 8.1 内存泄漏扫描：Profiler 连续旋转屏幕 50 次无 Activity 泄漏
- [ ] 8.2 异常熔断：
  – 当 Activity 被销毁时尚未收到结果，自动取消回调
  – 防止 IllegalStateException
- [ ] 8.3 灰度埋点：
  – 申请次数 / 授权率 / 永久拒绝率 / 设置页跳转率
- [ ] 8.4 输出《性能报告》：冷启动耗时 < 1 ms，无反射，包体积增加 < 50 KB

------

### ✅ 交付清单（Done Criteria）

1. `permission-core` AAR（Kotlin + Java API）
2. `permission-coroutine` AAR（可选）
3. Demo APK + 源码
4. 文档站点（GitHub Pages ）