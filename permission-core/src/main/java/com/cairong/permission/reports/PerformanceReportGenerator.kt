package com.cairong.permission.reports

import android.content.Context
import com.cairong.permission.analytics.AnalyticsReport
import com.cairong.permission.analytics.PermissionAnalytics
import com.cairong.permission.compatibility.CompatibilityChecker
import com.cairong.permission.performance.PerformanceMonitor
import java.text.SimpleDateFormat
import java.util.*

/**
 * 性能报告生成器
 * 
 * 生成详细的性能和稳定性报告
 */
object PerformanceReportGenerator {
    
    /**
     * 生成完整的性能报告
     */
    fun generateFullReport(context: Context): String {
        val performanceReport = PerformanceMonitor.generateReport(context)
        val analyticsReport = PermissionAnalytics.getInstance(context).getAnalyticsReport()
        val compatibilityReport = CompatibilityChecker.checkCompatibility(context)
        
        return buildString {
            appendLine("=== Android 权限框架性能报告 ===")
            appendLine("生成时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine()
            
            // 系统信息
            appendLine("📱 系统信息")
            appendLine("  Android 版本: ${performanceReport.systemInfo.androidVersion}")
            appendLine("  API 级别: ${performanceReport.systemInfo.apiLevel}")
            appendLine("  制造商: ${performanceReport.systemInfo.manufacturer}")
            appendLine("  型号: ${performanceReport.systemInfo.model}")
            appendLine("  CPU 架构: ${performanceReport.systemInfo.cpuAbi}")
            appendLine()
            
            // 性能指标
            appendLine("⚡ 性能指标")
            appendLine("  框架初始化时间: ${performanceReport.frameworkInitTime}ms")
            appendLine("  内存使用情况:")
            appendLine("    总内存: ${formatBytes(performanceReport.memoryUsage.totalMemory)}")
            appendLine("    已用内存: ${formatBytes(performanceReport.memoryUsage.usedMemory)}")
            appendLine("    空闲内存: ${formatBytes(performanceReport.memoryUsage.freeMemory)}")
            appendLine("    最大内存: ${formatBytes(performanceReport.memoryUsage.maxMemory)}")
            appendLine("    Native堆大小: ${formatBytes(performanceReport.memoryUsage.nativeHeapSize)}")
            appendLine("    Native堆已分配: ${formatBytes(performanceReport.memoryUsage.nativeHeapAllocatedSize)}")
            appendLine()
            
            // 包大小信息
            appendLine("📦 包大小信息")
            appendLine("  APK 大小: ${formatBytes(performanceReport.packageSize.apkSize)}")
            appendLine("  核心库大小: ${formatBytes(performanceReport.packageSize.coreLibrarySize)}")
            appendLine("  协程库大小: ${formatBytes(performanceReport.packageSize.coroutineLibrarySize)}")
            appendLine("  总框架大小: ${formatBytes(performanceReport.packageSize.coreLibrarySize + performanceReport.packageSize.coroutineLibrarySize)}")
            appendLine()
            
            // 性能指标详情
            if (performanceReport.metrics.isNotEmpty()) {
                appendLine("📊 详细性能指标")
                performanceReport.metrics.forEach { metric ->
                    appendLine("  ${metric.name}:")
                    appendLine("    调用次数: ${metric.callCount}")
                    appendLine("    总耗时: ${metric.totalTime}ms")
                    appendLine("    平均耗时: ${metric.avgTime}ms")
                    appendLine("    最大耗时: ${metric.maxTime}ms")
                    appendLine("    最小耗时: ${metric.minTime}ms")
                }
                appendLine()
            }
            
            // 权限使用统计
            appendLine("📈 权限使用统计")
            appendLine("  总请求次数: ${analyticsReport.totalRequests}")
            appendLine("  授权次数: ${analyticsReport.grantedCount}")
            appendLine("  拒绝次数: ${analyticsReport.deniedCount}")
            appendLine("  永久拒绝次数: ${analyticsReport.permanentlyDeniedCount}")
            appendLine("  设置跳转次数: ${analyticsReport.settingsJumpCount}")
            appendLine("  超时次数: ${analyticsReport.timeoutCount}")
            appendLine("  授权率: ${String.format("%.2f%%", analyticsReport.grantedRate * 100)}")
            appendLine("  拒绝率: ${String.format("%.2f%%", analyticsReport.deniedRate * 100)}")
            appendLine("  永久拒绝率: ${String.format("%.2f%%", analyticsReport.permanentlyDeniedRate * 100)}")
            appendLine("  平均请求时间: ${analyticsReport.avgRequestTime}ms")
            appendLine("  最大请求时间: ${analyticsReport.maxRequestTime}ms")
            appendLine("  最小请求时间: ${analyticsReport.minRequestTime}ms")
            appendLine()
            
            // 权限详细统计
            if (analyticsReport.permissionStats.isNotEmpty()) {
                appendLine("🔐 权限详细统计")
                analyticsReport.permissionStats.forEach { stat ->
                    appendLine("  ${stat.permission}:")
                    appendLine("    请求次数: ${stat.requestCount}")
                    appendLine("    授权次数: ${stat.grantedCount}")
                    appendLine("    拒绝次数: ${stat.deniedCount}")
                    appendLine("    永久拒绝次数: ${stat.permanentlyDeniedCount}")
                    appendLine("    平均请求时间: ${stat.avgRequestTime}ms")
                    val grantRate = if (stat.requestCount > 0) stat.grantedCount.toFloat() / stat.requestCount * 100 else 0f
                    appendLine("    授权率: ${String.format("%.2f%%", grantRate)}")
                }
                appendLine()
            }
            
            // 兼容性信息
            appendLine("🔧 兼容性信息")
            appendLine("  兼容性状态: ${if (compatibilityReport.isCompatible) "✅ 兼容" else "❌ 不兼容"}")
            appendLine("  支持的特性: ${compatibilityReport.supportedFeatures.size} 个")
            appendLine("  不支持的特性: ${compatibilityReport.unsupportedFeatures.size} 个")
            
            if (compatibilityReport.warnings.isNotEmpty()) {
                appendLine("  警告:")
                compatibilityReport.warnings.forEach { warning ->
                    appendLine("    ⚠️ $warning")
                }
            }
            
            if (compatibilityReport.recommendations.isNotEmpty()) {
                appendLine("  建议:")
                compatibilityReport.recommendations.forEach { recommendation ->
                    appendLine("    💡 $recommendation")
                }
            }
            appendLine()
            
            // 稳定性评估
            appendLine("🛡️ 稳定性评估")
            val stabilityScore = calculateStabilityScore(analyticsReport, performanceReport)
            appendLine("  稳定性评分: ${stabilityScore}/100")
            appendLine("  评估等级: ${getStabilityGrade(stabilityScore)}")
            appendLine()
            
            // 优化建议
            appendLine("🚀 优化建议")
            generateOptimizationSuggestions(analyticsReport, performanceReport).forEach { suggestion ->
                appendLine("  💡 $suggestion")
            }
        }
    }
    
    /**
     * 格式化字节数
     */
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", size, units[unitIndex])
    }
    
    /**
     * 计算稳定性评分
     */
    private fun calculateStabilityScore(
        analyticsReport: AnalyticsReport,
        performanceReport: PerformanceMonitor.PerformanceReport
    ): Int {
        var score = 100
        
        // 根据授权率扣分
        if (analyticsReport.grantedRate < 0.8f) {
            score -= ((0.8f - analyticsReport.grantedRate) * 50).toInt()
        }
        
        // 根据超时率扣分
        val timeoutRate = if (analyticsReport.totalRequests > 0) {
            analyticsReport.timeoutCount.toFloat() / analyticsReport.totalRequests
        } else 0f
        
        if (timeoutRate > 0.05f) {
            score -= ((timeoutRate - 0.05f) * 200).toInt()
        }
        
        // 根据内存使用率扣分
        val memoryUsageRate = performanceReport.memoryUsage.usedMemory.toFloat() / 
                             performanceReport.memoryUsage.maxMemory
        
        if (memoryUsageRate > 0.8f) {
            score -= ((memoryUsageRate - 0.8f) * 50).toInt()
        }
        
        // 根据平均请求时间扣分
        if (analyticsReport.avgRequestTime > 5000) { // 超过5秒
            score -= ((analyticsReport.avgRequestTime - 5000) / 1000 * 10).toInt()
        }
        
        return maxOf(0, score)
    }
    
    /**
     * 获取稳定性等级
     */
    private fun getStabilityGrade(score: Int): String {
        return when {
            score >= 90 -> "优秀 (A)"
            score >= 80 -> "良好 (B)"
            score >= 70 -> "一般 (C)"
            score >= 60 -> "较差 (D)"
            else -> "很差 (F)"
        }
    }
    
    /**
     * 生成优化建议
     */
    private fun generateOptimizationSuggestions(
        analyticsReport: AnalyticsReport,
        performanceReport: PerformanceMonitor.PerformanceReport
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        // 授权率相关建议
        if (analyticsReport.grantedRate < 0.8f) {
            suggestions.add("权限授权率较低，建议优化权限解释文案，提高用户理解度")
        }
        
        // 超时相关建议
        val timeoutRate = if (analyticsReport.totalRequests > 0) {
            analyticsReport.timeoutCount.toFloat() / analyticsReport.totalRequests
        } else 0f
        
        if (timeoutRate > 0.05f) {
            suggestions.add("权限请求超时率较高，建议检查网络环境或优化请求流程")
        }
        
        // 内存使用相关建议
        val memoryUsageRate = performanceReport.memoryUsage.usedMemory.toFloat() / 
                             performanceReport.memoryUsage.maxMemory
        
        if (memoryUsageRate > 0.8f) {
            suggestions.add("内存使用率较高，建议优化内存管理，及时释放不必要的对象")
        }
        
        // 请求时间相关建议
        if (analyticsReport.avgRequestTime > 3000) {
            suggestions.add("平均权限请求时间较长，建议优化UI响应速度")
        }
        
        // 永久拒绝率相关建议
        if (analyticsReport.permanentlyDeniedRate > 0.2f) {
            suggestions.add("权限永久拒绝率较高，建议优化权限引导流程")
        }
        
        // 包大小相关建议
        val totalLibrarySize = performanceReport.packageSize.coreLibrarySize + 
                              performanceReport.packageSize.coroutineLibrarySize
        
        if (totalLibrarySize > 100 * 1024) { // 超过100KB
            suggestions.add("框架库大小较大，建议考虑按需引入功能模块")
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("当前性能表现良好，继续保持！")
        }
        
        return suggestions
    }
    
    /**
     * 生成简化报告
     */
    fun generateSummaryReport(context: Context): String {
        val analyticsReport = PermissionAnalytics.getInstance(context).getAnalyticsReport()
        val performanceReport = PerformanceMonitor.generateReport(context)
        val stabilityScore = calculateStabilityScore(analyticsReport, performanceReport)
        
        return buildString {
            appendLine("=== 权限框架性能摘要 ===")
            appendLine("稳定性评分: ${stabilityScore}/100 (${getStabilityGrade(stabilityScore)})")
            appendLine("总请求次数: ${analyticsReport.totalRequests}")
            appendLine("授权率: ${String.format("%.1f%%", analyticsReport.grantedRate * 100)}")
            appendLine("平均请求时间: ${analyticsReport.avgRequestTime}ms")
            appendLine("内存使用: ${formatBytes(performanceReport.memoryUsage.usedMemory)}")
            appendLine("框架大小: ${formatBytes(performanceReport.packageSize.coreLibrarySize + performanceReport.packageSize.coroutineLibrarySize)}")
        }
    }
}