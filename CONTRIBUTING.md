# 贡献指南

感谢您对 Android 权限请求框架的关注！我们欢迎所有形式的贡献。

## 🤝 如何贡献

### 报告 Bug
1. 在提交 Bug 报告之前，请先搜索现有的 [Issues](https://github.com/cairong/android-permission-framework/issues)
2. 使用 Bug 报告模板创建新的 Issue
3. 提供详细的复现步骤和环境信息
4. 如果可能，请提供最小化的复现示例

### 建议新功能
1. 在提交功能请求之前，请先搜索现有的 Issues
2. 使用功能请求模板创建新的 Issue
3. 详细描述功能的使用场景和预期行为
4. 考虑功能对现有 API 的影响

### 提交代码
1. Fork 这个仓库
2. 创建你的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建一个 Pull Request

## 📋 开发环境设置

### 前置要求
- Android Studio Arctic Fox 或更高版本
- JDK 17 或更高版本
- Android SDK API 23-34
- Git

### 克隆项目
```bash
git clone https://github.com/cairong/android-permission-framework.git
cd android-permission-framework
```

### 构建项目
```bash
./gradlew build
```

### 运行测试
```bash
# 运行单元测试
./gradlew test

# 运行集成测试
./gradlew connectedAndroidTest

## 生成测试覆盖率报告
#./gradlew jacocoTestReport
```

## 🎨 代码风格

### Kotlin 代码风格
- 使用 4 个空格缩进
- 遵循 [Kotlin 官方代码风格指南](https://kotlinlang.org/docs/coding-conventions.html)
- 使用有意义的变量和函数名
- 为公共 API 添加 KDoc 注释

### Java 代码风格
- 使用 4 个空格缩进
- 遵循 [Google Java 代码风格指南](https://google.github.io/styleguide/javaguide.html)
- 为公共 API 添加 Javadoc 注释

### 示例代码风格
```kotlin
/**
 * 权限请求构建器
 * 
 * 提供链式调用API来构建权限请求
 * 
 * @param activity Activity实例
 */
class PermissionRequestBuilder(
    private val activity: ComponentActivity
) {
    
    /**
     * 添加权限
     * 
     * @param permission 权限名称
     * @return 构建器实例
     */
    fun permission(permission: String): PermissionRequestBuilder {
        // 实现逻辑
        return this
    }
}
```

## 🧪 测试指南

### 单元测试
- 为所有公共 API 编写单元测试
- 测试覆盖率应达到 80% 以上
- 使用 JUnit 4 和 Mockito
- 测试文件命名：`ClassNameTest.kt`

### 集成测试
- 为关键功能编写集成测试
- 测试真实的 Android 环境
- 使用 Espresso 进行 UI 测试

### 测试示例
```kotlin
@Test
fun `should request permission successfully`() {
    // Given
    val permission = Manifest.permission.CAMERA
    val callback = mock<PermissionCallback>()
    
    // When
    PermissionManager.with(activity)
        .permission(permission)
        .callback(callback)
        .request()
    
    // Then
    verify(callback).onGranted(arrayOf(permission))
}
```

## 📚 文档

### API 文档
- 为所有公共类和方法添加文档注释
- 使用 KDoc 格式（Kotlin）或 Javadoc 格式（Java）
- 包含参数说明、返回值说明和使用示例

### README 更新
- 如果添加了新功能，请更新 README.md
- 添加使用示例和配置说明
- 更新功能列表

### CHANGELOG 更新
- 在 CHANGELOG.md 中记录所有变更
- 遵循 [Keep a Changelog](https://keepachangelog.com/) 格式
- 分类记录：Added、Changed、Deprecated、Removed、Fixed、Security

## 🔄 Pull Request 流程

### 提交前检查
- [ ] 代码通过所有测试
- [ ] 代码符合项目风格指南
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] 更新了 CHANGELOG.md

### PR 描述
- 使用 PR 模板
- 清楚描述变更内容
- 关联相关的 Issue
- 提供测试说明

### 代码审查
- 所有 PR 都需要至少一个维护者的审查
- 解决所有审查意见
- 确保 CI 检查通过

## 🏷️ 版本发布

### 版本号规则
遵循 [语义化版本](https://semver.org/lang/zh-CN/) 规则：
- **主版本号**：不兼容的 API 修改
- **次版本号**：向下兼容的功能性新增
- **修订号**：向下兼容的问题修正

### 发布流程
1. 更新版本号
2. 更新 CHANGELOG.md
3. 创建 Git 标签
4. GitHub Actions 自动构建和发布
5. 更新文档

## 🎯 贡献类型

### 代码贡献
- Bug 修复
- 新功能开发
- 性能优化
- 代码重构

### 非代码贡献
- 文档改进
- 翻译
- 测试用例
- 示例代码
- 问题反馈

## 📞 联系方式

### 获取帮助
- 查看 [FAQ](README.md#常见问题)
- 搜索现有的 [Issues](https://github.com/cairong/android-permission-framework/issues)
- 创建新的 Issue

### 讨论
- 对于一般性问题，请使用 GitHub Discussions
- 对于 Bug 报告，请使用 GitHub Issues
- 对于功能请求，请使用 GitHub Issues

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

### 贡献者
- [CaiRong](https://github.com/cairong) - 项目创建者和维护者

### 特别感谢
- Android 开发团队提供的 Activity Result API
- 所有提供反馈和建议的用户

## 📄 许可证

通过贡献代码，您同意您的贡献将在 [Apache License 2.0](LICENSE) 下获得许可。