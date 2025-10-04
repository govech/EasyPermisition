package com.cairong.permission.analytics

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 权限分析和统计
 * 
 * 收集权限请求的统计数据，用于分析和优化
 */
class PermissionAnalytics private constructor(private val context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "permission_analytics", Context.MODE_PRIVATE
    )
    
    // 统计计数器
    private val requestCount = AtomicInteger(0)
    private val grantedCount = AtomicInteger(0)
    private val deniedCount = AtomicInteger(0)
    private val permanentlyDeniedCount = AtomicInteger(0)
    private val settingsJumpCount = AtomicInteger(0)
    private val timeoutCount = AtomicInteger(0)
    
    // 权限统计映射
    private val permissionStats = ConcurrentHashMap<String, PermissionStat>()
    
    // 性能统计
    private val totalRequestTime = AtomicLong(0)
    private val maxRequestTime = AtomicLong(0)
    private val minRequestTime = AtomicLong(Long.MAX_VALUE)
    
    companion object {
        @Volatile
        private var INSTANCE: PermissionAnalytics? = null
        
        fun getInstance(context: Context): PermissionAnalytics {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PermissionAnalytics(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 权限统计数据
     */
    data class PermissionStat(
        val permission: String,
        var requestCount: Int = 0,
        var grantedCount: Int = 0,
        var deniedCount: Int = 0,
        var permanentlyDeniedCount: Int = 0,
        var avgRequestTime: Long = 0,
        var lastRequestTime: Long = 0
    )
    
    /**
     * 权限请求事件
     */
    data class PermissionEvent(
        val eventType: EventType,
        val permissions: Array<String>,
        val timestamp: Long = System.currentTimeMillis(),
        val requestTime: Long = 0,
        val deviceInfo: DeviceInfo = DeviceInfo(),
        val extra: Map<String, Any> = emptyMap()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as PermissionEvent
            
            if (eventType != other.eventType) return false
            if (!permissions.contentEquals(other.permissions)) return false
            if (timestamp != other.timestamp) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = eventType.hashCode()
            result = 31 * result + permissions.contentHashCode()
            result = 31 * result + timestamp.hashCode()
            return result
        }
    }
    
    /**
     * 事件类型
     */
    enum class EventType {
        REQUEST_START,      // 权限请求开始
        REQUEST_GRANTED,    // 权限授权
        REQUEST_DENIED,     // 权限拒绝
        REQUEST_PERMANENTLY_DENIED, // 权限永久拒绝
        SETTINGS_JUMP,      // 跳转设置页面
        REQUEST_TIMEOUT,    // 请求超时
        REQUEST_CANCELLED   // 请求取消
    }
    
    /**
     * 设备信息
     */
    data class DeviceInfo(
        val androidVersion: String = Build.VERSION.RELEASE,
        val apiLevel: Int = Build.VERSION.SDK_INT,
        val manufacturer: String = Build.MANUFACTURER,
        val model: String = Build.MODEL,
        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
    )
    
    /**
     * 记录权限请求开始
     */
    fun recordRequestStart(permissions: Array<String>, extra: Map<String, Any> = emptyMap()) {
        requestCount.incrementAndGet()
        
        val event = PermissionEvent(
            eventType = EventType.REQUEST_START,
            permissions = permissions,
            extra = extra
        )
        
        recordEvent(event)
        
        permissions.forEach { permission ->
            val stat = permissionStats.getOrPut(permission) { PermissionStat(permission) }
            stat.requestCount++
            stat.lastRequestTime = System.currentTimeMillis()
        }
    }
    
    /**
     * 记录权限授权
     */
    fun recordGranted(permissions: Array<String>, requestTime: Long = 0) {
        grantedCount.addAndGet(permissions.size)
        updateRequestTime(requestTime)
        
        val event = PermissionEvent(
            eventType = EventType.REQUEST_GRANTED,
            permissions = permissions,
            requestTime = requestTime
        )
        
        recordEvent(event)
        
        permissions.forEach { permission ->
            permissionStats[permission]?.let { stat ->
                stat.grantedCount++
                if (requestTime > 0) {
                    stat.avgRequestTime = (stat.avgRequestTime + requestTime) / 2
                }
            }
        }
    }
    
    /**
     * 记录权限拒绝
     */
    fun recordDenied(permissions: Array<String>, requestTime: Long = 0) {
        deniedCount.addAndGet(permissions.size)
        updateRequestTime(requestTime)
        
        val event = PermissionEvent(
            eventType = EventType.REQUEST_DENIED,
            permissions = permissions,
            requestTime = requestTime
        )
        
        recordEvent(event)
        
        permissions.forEach { permission ->
            permissionStats[permission]?.let { stat ->
                stat.deniedCount++
                if (requestTime > 0) {
                    stat.avgRequestTime = (stat.avgRequestTime + requestTime) / 2
                }
            }
        }
    }
    
    /**
     * 记录权限永久拒绝
     */
    fun recordPermanentlyDenied(permissions: Array<String>, requestTime: Long = 0) {
        permanentlyDeniedCount.addAndGet(permissions.size)
        updateRequestTime(requestTime)
        
        val event = PermissionEvent(
            eventType = EventType.REQUEST_PERMANENTLY_DENIED,
            permissions = permissions,
            requestTime = requestTime
        )
        
        recordEvent(event)
        
        permissions.forEach { permission ->
            permissionStats[permission]?.let { stat ->
                stat.permanentlyDeniedCount++
                if (requestTime > 0) {
                    stat.avgRequestTime = (stat.avgRequestTime + requestTime) / 2
                }
            }
        }
    }
    
    /**
     * 记录跳转设置页面
     */
    fun recordSettingsJump(permissions: Array<String>) {
        settingsJumpCount.incrementAndGet()
        
        val event = PermissionEvent(
            eventType = EventType.SETTINGS_JUMP,
            permissions = permissions
        )
        
        recordEvent(event)
    }
    
    /**
     * 记录请求超时
     */
    fun recordTimeout(permissions: Array<String>, requestTime: Long) {
        timeoutCount.incrementAndGet()
        updateRequestTime(requestTime)
        
        val event = PermissionEvent(
            eventType = EventType.REQUEST_TIMEOUT,
            permissions = permissions,
            requestTime = requestTime
        )
        
        recordEvent(event)
    }
    
    /**
     * 更新请求时间统计
     */
    private fun updateRequestTime(requestTime: Long) {
        if (requestTime > 0) {
            totalRequestTime.addAndGet(requestTime)
            
            // 更新最大值
            var currentMax = maxRequestTime.get()
            while (requestTime > currentMax && !maxRequestTime.compareAndSet(currentMax, requestTime)) {
                currentMax = maxRequestTime.get()
            }
            
            // 更新最小值
            var currentMin = minRequestTime.get()
            while (requestTime < currentMin && !minRequestTime.compareAndSet(currentMin, requestTime)) {
                currentMin = minRequestTime.get()
            }
        }
    }
    
    /**
     * 记录事件
     */
    private fun recordEvent(event: PermissionEvent) {
        // 这里可以扩展为发送到远程分析服务
        // 目前只是本地存储
        saveEventToLocal(event)
    }
    
    /**
     * 保存事件到本地
     */
    private fun saveEventToLocal(event: PermissionEvent) {
        try {
            val json = JSONObject().apply {
                put("eventType", event.eventType.name)
                put("permissions", event.permissions.joinToString(","))
                put("timestamp", event.timestamp)
                put("requestTime", event.requestTime)
                put("deviceInfo", JSONObject().apply {
                    put("androidVersion", event.deviceInfo.androidVersion)
                    put("apiLevel", event.deviceInfo.apiLevel)
                    put("manufacturer", event.deviceInfo.manufacturer)
                    put("model", event.deviceInfo.model)
                })
            }
            
            // 保存到 SharedPreferences（实际项目中可能需要更高效的存储方案）
            val eventKey = "event_${event.timestamp}_${event.eventType.name}"
            preferences.edit().putString(eventKey, json.toString()).apply()
            
        } catch (e: Exception) {
            // 静默处理异常，不影响主要功能
        }
    }
    
    /**
     * 获取统计报告
     */
    fun getAnalyticsReport(): AnalyticsReport {
        val totalRequests = requestCount.get()
        val avgRequestTime = if (totalRequests > 0) {
            totalRequestTime.get() / totalRequests
        } else 0L
        
        return AnalyticsReport(
            totalRequests = totalRequests,
            grantedCount = grantedCount.get(),
            deniedCount = deniedCount.get(),
            permanentlyDeniedCount = permanentlyDeniedCount.get(),
            settingsJumpCount = settingsJumpCount.get(),
            timeoutCount = timeoutCount.get(),
            grantedRate = if (totalRequests > 0) grantedCount.get().toFloat() / totalRequests else 0f,
            deniedRate = if (totalRequests > 0) deniedCount.get().toFloat() / totalRequests else 0f,
            permanentlyDeniedRate = if (totalRequests > 0) permanentlyDeniedCount.get().toFloat() / totalRequests else 0f,
            avgRequestTime = avgRequestTime,
            maxRequestTime = maxRequestTime.get(),
            minRequestTime = if (minRequestTime.get() == Long.MAX_VALUE) 0L else minRequestTime.get(),
            permissionStats = permissionStats.values.toList(),
            deviceInfo = DeviceInfo()
        )
    }
    
    /**
     * 清理统计数据
     */
    fun clearAnalytics() {
        requestCount.set(0)
        grantedCount.set(0)
        deniedCount.set(0)
        permanentlyDeniedCount.set(0)
        settingsJumpCount.set(0)
        timeoutCount.set(0)
        totalRequestTime.set(0)
        maxRequestTime.set(0)
        minRequestTime.set(Long.MAX_VALUE)
        permissionStats.clear()
        
        // 清理本地存储
        preferences.edit().clear().apply()
    }
}

/**
 * 分析报告
 */
data class AnalyticsReport(
    val totalRequests: Int,
    val grantedCount: Int,
    val deniedCount: Int,
    val permanentlyDeniedCount: Int,
    val settingsJumpCount: Int,
    val timeoutCount: Int,
    val grantedRate: Float,
    val deniedRate: Float,
    val permanentlyDeniedRate: Float,
    val avgRequestTime: Long,
    val maxRequestTime: Long,
    val minRequestTime: Long,
    val permissionStats: List<PermissionAnalytics.PermissionStat>,
    val deviceInfo: PermissionAnalytics.DeviceInfo
)