#!/bin/bash

# Android 权限框架文档生成脚本

echo "🚀 开始生成 Android 权限框架文档..."

# 检查是否在项目根目录
if [ ! -f "settings.gradle.kts" ]; then
    echo "❌ 错误：请在项目根目录运行此脚本"
    exit 1
fi

# 清理之前的文档
echo "🧹 清理旧文档..."
rm -rf docs/
rm -rf permission-core/build/dokka/
rm -rf permission-coroutine/build/dokka/

# 生成 API 文档
echo "📚 生成 API 文档..."
./gradlew dokkaHtml

# 检查文档生成是否成功
if [ $? -eq 0 ]; then
    echo "✅ API 文档生成成功"
else
    echo "❌ API 文档生成失败"
    exit 1
fi

# 创建文档目录结构
echo "📁 创建文档目录结构..."
mkdir -p docs/api/
mkdir -p docs/guides/

# 复制 API 文档
echo "📋 复制 API 文档..."
cp -r permission-core/build/dokka/* docs/api/core/
cp -r permission-coroutine/build/dokka/* docs/api/coroutine/

# 复制指南文档
echo "📖 复制指南文档..."
cp README.md docs/
cp CHANGELOG.md docs/
cp doc/竞品分析报告.md docs/guides/

# 生成文档索引
echo "📝 生成文档索引..."
cat > docs/index.html << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Android 权限请求框架 - 文档</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; }
        .header { text-align: center; margin-bottom: 40px; }
        .section { margin: 20px 0; padding: 20px; border: 1px solid #e1e4e8; border-radius: 6px; }
        .link { display: inline-block; margin: 10px; padding: 10px 20px; background: #0366d6; color: white; text-decoration: none; border-radius: 4px; }
        .link:hover { background: #0256cc; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🛡️ Android 权限请求框架</h1>
        <p>零依赖、可复用、支持链式调用的 Android 权限请求框架</p>
    </div>
    
    <div class="section">
        <h2>📚 API 文档</h2>
        <a href="api/core/index.html" class="link">Core API 文档</a>
        <a href="api/coroutine/index.html" class="link">Coroutine API 文档</a>
    </div>
    
    <div class="section">
        <h2>📖 使用指南</h2>
        <a href="README.html" class="link">快速开始</a>
        <a href="CHANGELOG.html" class="link">更新日志</a>
        <a href="guides/竞品分析报告.html" class="link">竞品分析</a>
    </div>
    
    <div class="section">
        <h2>🔗 相关链接</h2>
        <a href="https://github.com/govech/EasyPermisition" class="link">GitHub 仓库</a>
        <a href="https://github.com/govech/EasyPermisition/issues" class="link">问题反馈</a>
    </div>
</body>
</html>
EOF

echo "✅ 文档生成完成！"
echo "📂 文档位置：docs/"
echo "🌐 打开 docs/index.html 查看文档首页"