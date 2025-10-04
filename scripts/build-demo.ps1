# Android 权限框架演示应用构建脚本

Write-Host "🚀 开始构建 Android 权限框架演示应用..." -ForegroundColor Green

# 检查是否在项目根目录
if (-not (Test-Path "settings.gradle.kts")) {
    Write-Host "❌ 错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

# 清理项目
Write-Host "🧹 清理项目..." -ForegroundColor Yellow
& ./gradlew clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 项目清理失败" -ForegroundColor Red
    exit 1
}

# 构建核心库
Write-Host "🔧 构建核心库..." -ForegroundColor Cyan
& ./gradlew :permission-core:assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 核心库构建失败" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 核心库构建成功" -ForegroundColor Green

# 构建协程扩展库
Write-Host "🔧 构建协程扩展库..." -ForegroundColor Cyan
& ./gradlew :permission-coroutine:assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 协程扩展库构建失败" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 协程扩展库构建成功" -ForegroundColor Green

# 构建演示应用
Write-Host "📱 构建演示应用..." -ForegroundColor Cyan
& ./gradlew :demo:assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 演示应用构建失败" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 演示应用构建成功" -ForegroundColor Green

# 运行测试
Write-Host "🧪 运行单元测试..." -ForegroundColor Cyan
& ./gradlew test

if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️ 部分测试失败，但构建继续" -ForegroundColor Yellow
} else {
    Write-Host "✅ 所有测试通过" -ForegroundColor Green
}

# 显示构建结果
Write-Host "`n🎉 构建完成！" -ForegroundColor Green
Write-Host "📦 构建产物位置：" -ForegroundColor Cyan
Write-Host "  - 核心库：permission-core/build/outputs/aar/" -ForegroundColor White
Write-Host "  - 协程库：permission-coroutine/build/outputs/aar/" -ForegroundColor White
Write-Host "  - 演示应用：demo/build/outputs/apk/debug/" -ForegroundColor White

# 检查 APK 文件
$apkPath = "demo/build/outputs/apk/debug/demo-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "📱 演示 APK 大小：$([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
}

Write-Host "`n🚀 可以使用以下命令安装演示应用：" -ForegroundColor Yellow
Write-Host "adb install demo/build/outputs/apk/debug/demo-debug.apk" -ForegroundColor White