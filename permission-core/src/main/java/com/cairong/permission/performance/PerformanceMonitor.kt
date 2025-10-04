package com.cairong.permission.performance

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 性能监控器
 * 
 * 监控权限框架的性能指标
 */
object PerformanceMonitor {
    
    private val startTimes = ConcurrentHashMap<String, Long>()
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val initTime = AtomicLong(0)
    
    /**
     * 性能指标
     */
    data class PerformanceMetric(
        val name: String,
        var totalTime: Long = 0,
        var callCount: Int = 0,
        var maxTime: Long = 0,
        var minTime: Long = Long.MAX_VALUE,
        var avgTime: Long = 0
    ) {
        fun addMeasurement(time: Long) {
            totalTime += time
            callCount++
            maxTime = maxOf(maxTime, time)
            minTime = minOf(minTime, time)
            avgTime = totalTime / callCount
        }
    }
    
    /**
     * 性能报告
     */
    data class PerformanceReport(
        val frameworkInitTime: Long,
        val memoryUsage: MemoryUsage,
        val packageSize: PackageSize,
        val metrics: List<PerformanceMetric>,
        val systemInfo: SystemInfo
    )
    
    /**
     * 内存使用情况
     */
    data class MemoryUsage(
        val totalMemory: Long,
        val usedMemory: Long,
        val freeMemory: Long,
        val maxMemory: Long,
        val nativeHeapSize: Long,
        val nativeHeapAllocatedSize: Long
    )
    
    /**
     * 包大小信息
     */
    data class PackageSize(
        val apkSize: Long,
        val coreLibrarySize: Long,
        val coroutineLibrarySize: Long
    )
    
    /**
     * 系统信息
     */
    data class SystemInfo(
        val androidVersion: String = Build.VERSION.RELEASE,
        val apiLevel: Int = Build.VERSION.SDK_INT,
        val manufacturer: String = Build.MANUFACTURER,
        val model: String = Build.MODEL,
        val cpuAbi: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
    )
    
    /**
     * 记录框架初始化时间
     */
    fun recordInitTime(time: Long) {
        initTime.set(time)
    }
    
    /**
     * 开始性能测量
     */
    fun startMeasurement(name: String) {
        startTimes[name] = System.nanoTime()
    }
    
    /**
     * 结束性能测量
     */
    fun endMeasurement(name: String) {
        val startTime = startTimes.remove(name) ?: return
        val duration = (System.nanoTime() - startTime) / 1_000_000 // 转换为毫秒
        
        val metric = performanceMetrics.getOrPut(name) { PerformanceMetric(name) }
        metric.addMeasurement(duration)
    }
    
    /**
     * 测量代码块执行时间
     */
    inline fun <T> measure(name: String, block: () -> T): T {
        startMeasurement(name)
        try {
            return block()
        } finally {
            endMeasurement(name)
        }
    }
    
    /**
     * 获取内存使用情况
     */
    fun getMemoryUsage(): MemoryUsage {
        val runtime = Runtime.getRuntime()
        return MemoryUsage(
            totalMemory = runtime.totalMemory(),
            usedMemory = runtime.totalMemory() - runtime.freeMemory(),
            freeMemory = runtime.freeMemory(),
            maxMemory = runtime.maxMemory(),
            nativeHeapSize = Debug.getNativeHeapSize(),
            nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize()
        )
    }
    
    /**
     * 获取包大小信息
     */
    fun getPackageSize(context: Context): PackageSize {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName, 
                PackageManager.GET_META_DATA
            )
            
            val apkFile = File(packageInfo.applicationInfo.sourceDir)
            val apkSize = if (apkFile.exists()) apkFile.length() else 0L
            
            PackageSize(
                apkSize = apkSize,
                coreLibrarySize = estimateLibrarySize("permission-core"),
                coroutineLibrarySize = estimateLibrarySize("permission-coroutine")
            )
        } catch (e: Exception) {
            PackageSize(0, 0, 0)
        }
    }
    
    /**
     * 估算库大小（简化实现）
     */
    private fun estimateLibrarySize(libraryName: String): Long {
        return when (libraryName) {
            "permission-core" -> 45 * 1024L // 约45KB
            "permission-coroutine" -> 8 * 1024L // 约8KB
            else -> 0L
        }
    }
    
    /**
     * 生成性能报告
     */
    fun generateReport(context: Context): PerformanceReport {
        return PerformanceReport(
            frameworkInitTime = initTime.get(),
            memoryUsage = getMemoryUsage(),
            packageSize = getPackageSize(context),
            metrics = performanceMetrics.values.toList(),
            systemInfo = SystemInfo()
        )
    }
    
    /**
     * 清理性能数据
     */
    fun clear() {
        startTimes.clear()
        performanceMetrics.clear()
        initTime.set(0)
    }
    
    /**
     * 获取性能指标
     */
    fun getMetric(name: String): PerformanceMetric? {
        return performanceMetrics[name]
    }
    
    /**
     * 获取所有性能指标
     */
    fun getAllMetrics(): Map<String, PerformanceMetric> {
        return performanceMetrics.toMap()
    }
}

/**
 * 性能测量注解
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Measured(val name: String = "")

/**
 * 性能测量扩展函数
 */
inline fun <T> measured(name: String, block: () -> T): T {
    return PerformanceMonitor.measure(name, block)
}