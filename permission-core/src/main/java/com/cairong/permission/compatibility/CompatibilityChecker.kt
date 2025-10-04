package com.cairong.permission.compatibility

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 兼容性检查器
 * 
 * 检查不同Android版本的兼容性
 */
object CompatibilityChecker {
    
    /**
     * 兼容性检查结果
     */
    data class CompatibilityResult(
        val isCompatible: Boolean,
        val androidVersion: String,
        val apiLevel: Int,
        val supportedFeatures: List<String>,
        val unsupportedFeatures: List<String>,
        val warnings: List<String>,
        val recommendations: List<String>
    )
    
    /**
     * 权限特性
     */
    enum class PermissionFeature(
        val featureName: String,
        val minApiLevel: Int,
        val description: String
    ) {
        RUNTIME_PERMISSIONS("运行时权限", 23, "Android 6.0+ 运行时权限系统"),
        BACKGROUND_LOCATION("后台位置权限", 29, "Android 10+ 后台位置权限"),
        SCOPED_STORAGE("分区存储", 29, "Android 10+ 分区存储"),
        NOTIFICATION_PERMISSION("通知权限", 33, "Android 13+ 通知权限"),
        MEDIA_PERMISSIONS("媒体权限", 33, "Android 13+ 细分媒体权限"),
        PARTIAL_MEDIA_PERMISSIONS("部分媒体权限", 34, "Android 14+ 部分媒体权限选择"),
        FOREGROUND_SERVICE_TYPES("前台服务类型", 34, "Android 14+ 前台服务类型权限"),
        EXACT_ALARM_PERMISSION("精确闹钟权限", 31, "Android 12+ 精确闹钟权限")
    }
    
    /**
     * 检查系统兼容性
     */
    fun checkCompatibility(context: Context): CompatibilityResult {
        val currentApiLevel = Build.VERSION.SDK_INT
        val androidVersion = Build.VERSION.RELEASE
        
        val supportedFeatures = mutableListOf<String>()
        val unsupportedFeatures = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        // 检查各个特性的支持情况
        PermissionFeature.values().forEach { feature ->
            if (currentApiLevel >= feature.minApiLevel) {
                supportedFeatures.add(feature.featureName)
            } else {
                unsupportedFeatures.add(feature.featureName)
            }
        }
        
        // 生成警告和建议
        generateWarningsAndRecommendations(
            currentApiLevel, 
            warnings, 
            recommendations
        )
        
        // 检查特定的兼容性问题
        checkSpecificCompatibilityIssues(context, currentApiLevel, warnings, recommendations)
        
        val isCompatible = currentApiLevel >= 23 // 最低支持 Android 6.0
        
        return CompatibilityResult(
            isCompatible = isCompatible,
            androidVersion = androidVersion,
            apiLevel = currentApiLevel,
            supportedFeatures = supportedFeatures,
            unsupportedFeatures = unsupportedFeatures,
            warnings = warnings,
            recommendations = recommendations
        )
    }
    
    /**
     * 生成警告和建议
     */
    private fun generateWarningsAndRecommendations(
        apiLevel: Int,
        warnings: MutableList<String>,
        recommendations: MutableList<String>
    ) {
        when {
            apiLevel < 23 -> {
                warnings.add("当前系统版本过低，不支持运行时权限")
                recommendations.add("建议升级到 Android 6.0 或更高版本")
            }
            apiLevel < 29 -> {
                warnings.add("不支持后台位置权限和分区存储")
                recommendations.add("某些高级功能可能无法使用")
            }
            apiLevel < 33 -> {
                warnings.add("不支持通知权限和细分媒体权限")
                recommendations.add("通知功能可能受限")
            }
            apiLevel < 34 -> {
                warnings.add("不支持部分媒体权限选择和前台服务类型权限")
                recommendations.add("某些新特性无法使用")
            }
        }
        
        // 添加通用建议
        recommendations.add("建议在不同Android版本上进行充分测试")
        recommendations.add("使用条件检查来处理版本差异")
    }
    
    /**
     * 检查特定的兼容性问题
     */
    private fun checkSpecificCompatibilityIssues(
        context: Context,
        apiLevel: Int,
        warnings: MutableList<String>,
        recommendations: MutableList<String>
    ) {
        // 检查是否支持特定权限
        val packageManager = context.packageManager
        
        // 检查相机权限
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            warnings.add("设备不支持相机功能")
        }
        
        // 检查位置服务
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
            warnings.add("设备不支持位置服务")
        }
        
        // 检查蓝牙
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            warnings.add("设备不支持蓝牙功能")
        }
        
        // 检查特定厂商的兼容性问题
        checkManufacturerSpecificIssues(warnings, recommendations)
    }
    
    /**
     * 检查厂商特定的兼容性问题
     */
    private fun checkManufacturerSpecificIssues(
        warnings: MutableList<String>,
        recommendations: MutableList<String>
    ) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        when {
            manufacturer.contains("xiaomi") -> {
                warnings.add("小米设备可能需要额外的自启动权限")
                recommendations.add("引导用户在安全中心中开启自启动权限")
            }
            manufacturer.contains("huawei") -> {
                warnings.add("华为设备可能需要额外的后台应用保护设置")
                recommendations.add("引导用户在手机管家中关闭后台应用保护")
            }
            manufacturer.contains("oppo") || manufacturer.contains("oneplus") -> {
                warnings.add("OPPO/OnePlus设备可能需要额外的后台权限设置")
                recommendations.add("引导用户在设置中允许后台运行")
            }
            manufacturer.contains("vivo") -> {
                warnings.add("vivo设备可能需要额外的后台高耗电权限")
                recommendations.add("引导用户在i管家中允许高耗电")
            }
        }
    }
    
    /**
     * 检查权限是否在当前版本可用
     */
    fun isPermissionAvailable(permission: String, apiLevel: Int = Build.VERSION.SDK_INT): Boolean {
        return when (permission) {
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> apiLevel >= 29
            android.Manifest.permission.POST_NOTIFICATIONS -> apiLevel >= 33
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO -> apiLevel >= 33
            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED" -> apiLevel >= 34
            android.Manifest.permission.SCHEDULE_EXACT_ALARM -> apiLevel >= 31
            else -> apiLevel >= 23 // 大部分危险权限从API 23开始
        }
    }
    
    /**
     * 获取权限的最低API级别
     */
    fun getPermissionMinApiLevel(permission: String): Int {
        return when (permission) {
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> 29
            android.Manifest.permission.POST_NOTIFICATIONS -> 33
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO -> 33
            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED" -> 34
            android.Manifest.permission.SCHEDULE_EXACT_ALARM -> 31
            else -> 23
        }
    }
    
    /**
     * 检查是否需要特殊处理
     */
    fun needsSpecialHandling(permission: String, apiLevel: Int = Build.VERSION.SDK_INT): Boolean {
        return when (permission) {
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                // 后台位置权限需要先获取前台位置权限
                apiLevel >= 29
            }
            android.Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                // 精确闹钟权限需要特殊处理
                apiLevel >= 31
            }
            else -> false
        }
    }
    
    /**
     * 生成兼容性报告
     */
    fun generateCompatibilityReport(context: Context): String {
        val result = checkCompatibility(context)
        
        return buildString {
            appendLine("=== Android 权限框架兼容性报告 ===")
            appendLine()
            appendLine("系统信息:")
            appendLine("  Android 版本: ${result.androidVersion}")
            appendLine("  API 级别: ${result.apiLevel}")
            appendLine("  兼容性: ${if (result.isCompatible) "✅ 兼容" else "❌ 不兼容"}")
            appendLine()
            
            if (result.supportedFeatures.isNotEmpty()) {
                appendLine("支持的特性:")
                result.supportedFeatures.forEach { feature ->
                    appendLine("  ✅ $feature")
                }
                appendLine()
            }
            
            if (result.unsupportedFeatures.isNotEmpty()) {
                appendLine("不支持的特性:")
                result.unsupportedFeatures.forEach { feature ->
                    appendLine("  ❌ $feature")
                }
                appendLine()
            }
            
            if (result.warnings.isNotEmpty()) {
                appendLine("警告:")
                result.warnings.forEach { warning ->
                    appendLine("  ⚠️ $warning")
                }
                appendLine()
            }
            
            if (result.recommendations.isNotEmpty()) {
                appendLine("建议:")
                result.recommendations.forEach { recommendation ->
                    appendLine("  💡 $recommendation")
                }
            }
        }
    }
}