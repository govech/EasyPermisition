一个 **零依赖、可复用、支持链式调用、可扩展至任意权限** 的 Android 权限请求框架。

------

### 🎯 Milestone 1 ｜需求澄清 & 技术方案（Day 0）

- [x] 1.1 明确框架边界（只处理运行时危险权限；不处理特殊权限如悬浮窗、通知、Root）
- [x] 1.2 输出《框架需求清单》.md：
  – 支持 Activity & Fragment
  – 支持单权限 / 多权限
  – 支持"拒绝一次后解释"与"永久拒绝后跳转设置"
  – 支持 Kotlin & Java 调用
  – 链式回调：onGranted / onDenied / onPermanentlyDenied / onBeforeRequest
  – 零依赖（仅依赖 androidx.activity:activity-ktx ≥1.7）
  – **新增：Android 14+ 权限适配（部分权限、通知权限）**
  – **新增：权限组处理策略（相关权限一起申请）**
- [x] 1.3 技术选型确认：
  – Activity Result API（RequestPermission / RequestMultiplePermissions）
  – 不依赖 RxJava / Kotlin-Coroutine，但内部可挂协程扩展
- [x] **1.4 竞品分析：对比 PermissionsDispatcher、EasyPermissions 等框架的优劣势**

------

### 🎯 Milestone 2 ｜核心架构搭建（Day 1-2）

- [x] 2.1 新建 `permission-core` module，包名 `com.cairong.permission`
- [x] 2.2 设计对外门面：`PermissionManager`（单例 + 建造者）
- [x] 2.3 设计请求实体：`PermissionRequest`（权限数组、解释文案、回调）
- [x] 2.4 设计回调接口：`PermissionCallback`（4 个方法见 1.2）
- [x] 2.5 设计解释器接口：`RationaleHandler`（默认实现：AlertDialog）
- [x] 2.6 设计跳转设置封装：`AppSettingsLauncher`
- [x] **2.7 设计权限状态管理：`PermissionState`（已授权/首次拒绝/永久拒绝）**
- [x] **2.8 添加 ProGuard 规则文件**
- [x] 2.9 单元测试覆盖核心分支 ≥ 80%（Jacoco 报告）

------

### 🎯 Milestone 3 ｜单权限链路跑通（Day 2-3）

- [x] 3.1 封装 `SinglePermissionContract`（ActivityResultContract<String,Boolean>）
- [x] 3.2 实现"检查→解释→请求→回调"完整链
- [x] **3.3 添加权限预检查：避免重复申请已授权权限**
- [x] 3.4 Demo 模块新增 `CameraActivity`：
  – 点击按钮 → 请求 CAMERA → 授权成功打开预览
  – 拒绝一次弹出解释 → 再拒绝跳转设置
- [x] 3.5 手动验收通过

------

### 🎯 Milestone 4 ｜多权限 & 批量拒绝场景（Day 3-4）

- [x] 4.1 封装 `MultiplePermissionContract`（ActivityResultContract<Array<String>, Map<String,Boolean>>）
- [x] 4.2 设计结果聚合策略：
  – 全部 granted → onGranted
  – 部分拒绝 → onDenied(拒绝列表)
  – 只要有一个永久拒绝 → 额外回调 onPermanentlyDenied
- [x] **4.3 支持权限依赖关系：某些权限必须同时授权才有意义**
- [x] 4.4 Demo 新增 `LocationActivity`：一次性请求 ACCESS_FINE_LOCATION + BACKGROUND_LOCATION
- [x] 4.5 自动化测试：Espresso 点击"仅允许前台"→ 断言后台权限进入永久拒绝路径

------

### 🎯 Milestone 5 ｜Java 兼容 & 协程扩展（Day 4-5）

- [x] 5.1 新增 `PermissionManager.java` 同名门面，提供静态方法供 Java 链式调用
- [x] 5.2 新增 `permission-coroutine` module，提供：
  – `suspend fun PermissionRequest.await()`
  – **`Flow<PermissionState>` 支持权限状态监听**
- [x] 5.3 Sample 分别用 Java 与 Kotlin-Coroutine 各写一个请求示例
- [x] 5.4 API 稳定性评审：@JvmOverloads / @JvmStatic 检查无遗漏

------

### 🎯 Milestone 6 ｜可配置 & 可扩展（Day 5-6）

- [x] 6.1 支持自定义主题：在 `RationaleHandler` 暴露 `themeResId` 参数
- [x] 6.2 支持自定义解释 UI：允许接入外部 DialogFragment / BottomSheet
- [x] 6.3 支持全局默认配置：
  – 默认解释文案、跳转设置按钮文案、是否强制跳转设置
  – **多语言支持（i18n）**
- [x] 6.4 提供 SPI 接口：其他模块可注册 `PermissionInterceptor` 做埋点 / 日志 / 灰度
- [x] **6.5 添加权限请求频率限制：防止恶意频繁申请**

------

### 🎯 Milestone 7 ｜Sample & 文档（Day 6-7）

- [x] 7.1 Demo 工程覆盖 8 个典型场景：
  1. 单权限 ✅
  2. 多权限 ✅
  3. 永久拒绝跳转设置 ✅
  4. 自定义 Rationale UI ✅
  5. Java 调用 ✅
  6. 协程调用 ✅
  7. **权限组批量申请** ✅
  8. **Android 14+ 新权限适配** ✅
- [x] 7.2 输出 README.md：
  – 集成方式（Gradle 依赖） ✅
  – 快速开始（Kotlin 三行代码） ✅
  – 高级配置（自定义 Handler、主题、拦截器） ✅
  – 常见 QA（"不再询问"判断、后台权限、Android 14 新特性） ✅
  – **迁移指南（从其他权限库迁移）** ✅
- [x] **7.3 输出 API 文档（KDoc + Dokka 生成）** ✅

------

### 🎯 Milestone 8 ｜性能 & 稳定性收口（Day 7-8）

- [x] 8.1 内存泄漏扫描：
  – ✅ 创建内存泄漏检测器 (MemoryLeakDetector)
  – ✅ 实现内存安全的回调包装器
  – ✅ 集成到权限请求执行器中
- [x] 8.2 异常熔断：
  – ✅ 创建权限请求熔断器 (PermissionCircuitBreaker)
  – ✅ 当 Activity 被销毁时自动取消回调
  – ✅ 防止 IllegalStateException
  – ✅ **添加超时机制：权限请求超时自动取消**
- [x] 8.3 灰度埋点：
  – ✅ 创建权限分析统计系统 (PermissionAnalytics)
  – ✅ 申请次数 / 授权率 / 永久拒绝率 / 设置页跳转率
  – ✅ 设备信息和性能数据收集
- [x] 8.4 输出《性能报告》：
  – ✅ 创建性能监控器 (PerformanceMonitor)
  – ✅ 性能报告生成器 (PerformanceReportGenerator)
  – ✅ 冷启动耗时 < 1 ms，无反射，包体积增加 < 50 KB
- [x] **8.5 兼容性测试：**
  – ✅ 创建兼容性检查器 (CompatibilityChecker)
  – ✅ Android 6.0 - 14 全版本验证
  – ✅ 厂商特定兼容性问题检测
- [x] **8.6 混淆测试：**
  – ✅ 更新 ProGuard 规则文件
  – ✅ 确保 ProGuard/R8 混淆后正常工作

------

### 🎯 **新增 Milestone 9 ｜发布准备（Day 8-9）**

- [ ] 9.1 **CI/CD 配置：GitHub Actions 自动构建、测试、发布**
- [ ] 9.2 **版本管理：语义化版本号 + CHANGELOG.md**
- [ ] 9.3 **Maven Central 发布配置**
- [ ] 9.4 **License 文件 + 开源协议选择**
- [ ] 9.5 **Issue/PR 模板配置**

------

### ✅ 交付清单（Done Criteria）

1. `permission-core` AAR（Kotlin + Java API）
2. `permission-coroutine` AAR（可选）
3. Demo APK + 源码
4. 文档站点（GitHub Pages）
5. **Maven Central 发布**
6. **CI/CD 流水线**
7. **完整的测试覆盖报告**

------

### 📋 **风险点 & 缓解措施**

| 风险点 | 影响 | 缓解措施 |
|--------|------|----------|
| Android 版本兼容性问题 | 高 | 提前在多版本设备测试 |
| 权限策略变更 | 中 | 关注 Android 官方文档更新 |
| 第三方库冲突 | 中 | 零依赖设计 + 命名空间隔离 |
| 性能回归 | 低 | 持续性能监控 + 基准测试 |

------

### 🔄 **迭代优化建议**

- **Phase 2**: 支持特殊权限（悬浮窗、通知、设备管理员）
- **Phase 3**: 权限使用统计 & 分析面板
- **Phase 4**: 可视化权限配置工具