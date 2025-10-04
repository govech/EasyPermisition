package com.cairong.permission.ratelimit

import com.cairong.permission.config.PermissionConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * 权限请求频率限制器
 * 
 * 防止恶意频繁申请权限
 */
class PermissionRateLimiter {
    
    private val requestTimes = ConcurrentHashMap<String, MutableList<Long>>()
    
    /**
     * 检查是否可以请求权限
     * 
     * @param permission 权限名称
     * @return 是否可以请求
     */
    fun canRequest(permission: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val times = requestTimes.getOrPut(permission) { mutableListOf() }
        
        synchronized(times) {
            // 清理过期的请求记录
            cleanupExpiredRequests(times, currentTime)
            
            // 检查频率限制
            if (times.isNotEmpty()) {
                val lastRequestTime = times.last()
                if (currentTime - lastRequestTime < PermissionConfig.rateLimitInterval) {
                    return false
                }
            }
            
            // 检查每小时请求次数限制
            val oneHourAgo = currentTime - 3600_000L // 1小时
            val recentRequests = times.count { it > oneHourAgo }
            if (recentRequests >= PermissionConfig.maxRequestsPerHour) {
                return false
            }
            
            return true
        }
    }
    
    /**
     * 记录权限请求
     * 
     * @param permission 权限名称
     */
    fun recordRequest(permission: String) {
        val currentTime = System.currentTimeMillis()
        val times = requestTimes.getOrPut(permission) { mutableListOf() }
        
        synchronized(times) {
            times.add(currentTime)
            cleanupExpiredRequests(times, currentTime)
        }
    }
    
    /**
     * 获取权限的下次可请求时间
     * 
     * @param permission 权限名称
     * @return 下次可请求的时间戳，如果可以立即请求则返回0
     */
    fun getNextRequestTime(permission: String): Long {
        val currentTime = System.currentTimeMillis()
        val times = requestTimes[permission] ?: return 0L
        
        synchronized(times) {
            if (times.isEmpty()) return 0L
            
            val lastRequestTime = times.last()
            val nextAllowedTime = lastRequestTime + PermissionConfig.rateLimitInterval
            
            return if (nextAllowedTime > currentTime) {
                nextAllowedTime
            } else {
                0L
            }
        }
    }
    
    /**
     * 获取权限的剩余请求次数（每小时）
     * 
     * @param permission 权限名称
     * @return 剩余请求次数
     */
    fun getRemainingRequests(permission: String): Int {
        val currentTime = System.currentTimeMillis()
        val times = requestTimes[permission] ?: return PermissionConfig.maxRequestsPerHour
        
        synchronized(times) {
            val oneHourAgo = currentTime - 3600_000L
            val recentRequests = times.count { it > oneHourAgo }
            return (PermissionConfig.maxRequestsPerHour - recentRequests).coerceAtLeast(0)
        }
    }
    
    /**
     * 清理过期的请求记录
     */
    private fun cleanupExpiredRequests(times: MutableList<Long>, currentTime: Long) {
        val oneHourAgo = currentTime - 3600_000L
        times.removeAll { it < oneHourAgo }
    }
    
    /**
     * 清空指定权限的请求记录
     * 
     * @param permission 权限名称
     */
    fun clearRequestHistory(permission: String) {
        requestTimes.remove(permission)
    }
    
    /**
     * 清空所有权限的请求记录
     */
    fun clearAllRequestHistory() {
        requestTimes.clear()
    }
    
    /**
     * 获取权限请求统计信息
     * 
     * @param permission 权限名称
     * @return 统计信息
     */
    fun getRequestStats(permission: String): PermissionRequestStats {
        val currentTime = System.currentTimeMillis()
        val times = requestTimes[permission] ?: emptyList()
        
        synchronized(times) {
            val oneHourAgo = currentTime - 3600_000L
            val oneDayAgo = currentTime - 86400_000L
            
            val totalRequests = times.size
            val requestsInLastHour = times.count { it > oneHourAgo }
            val requestsInLastDay = times.count { it > oneDayAgo }
            val lastRequestTime = times.maxOrNull() ?: 0L
            
            return PermissionRequestStats(
                permission = permission,
                totalRequests = totalRequests,
                requestsInLastHour = requestsInLastHour,
                requestsInLastDay = requestsInLastDay,
                lastRequestTime = lastRequestTime,
                canRequestNow = canRequest(permission),
                nextRequestTime = getNextRequestTime(permission),
                remainingRequests = getRemainingRequests(permission)
            )
        }
    }
}

/**
 * 权限请求统计信息
 */
data class PermissionRequestStats(
    /**
     * 权限名称
     */
    val permission: String,
    
    /**
     * 总请求次数
     */
    val totalRequests: Int,
    
    /**
     * 最近1小时请求次数
     */
    val requestsInLastHour: Int,
    
    /**
     * 最近24小时请求次数
     */
    val requestsInLastDay: Int,
    
    /**
     * 最后一次请求时间
     */
    val lastRequestTime: Long,
    
    /**
     * 是否可以立即请求
     */
    val canRequestNow: Boolean,
    
    /**
     * 下次可请求时间
     */
    val nextRequestTime: Long,
    
    /**
     * 剩余请求次数（每小时）
     */
    val remainingRequests: Int
)

/**
 * 全局权限频率限制器实例
 */
object GlobalPermissionRateLimiter {
    private val rateLimiter = PermissionRateLimiter()
    
    fun canRequest(permission: String): Boolean = rateLimiter.canRequest(permission)
    fun recordRequest(permission: String) = rateLimiter.recordRequest(permission)
    fun getNextRequestTime(permission: String): Long = rateLimiter.getNextRequestTime(permission)
    fun getRemainingRequests(permission: String): Int = rateLimiter.getRemainingRequests(permission)
    fun clearRequestHistory(permission: String) = rateLimiter.clearRequestHistory(permission)
    fun clearAllRequestHistory() = rateLimiter.clearAllRequestHistory()
    fun getRequestStats(permission: String): PermissionRequestStats = rateLimiter.getRequestStats(permission)
}

/**
 * 权限滥用检测器
 * 
 * 检测可能的权限滥用行为
 */
class PermissionAbuseDetector {
    
    private val rateLimiter = GlobalPermissionRateLimiter
    
    /**
     * 检查是否存在权限滥用
     * 
     * @param permission 权限名称
     * @return 滥用检测结果
     */
    fun checkAbuse(permission: String): AbuseDetectionResult {
        val stats = rateLimiter.getRequestStats(permission)
        val issues = mutableListOf<String>()
        val severity = mutableListOf<AbuseSeverity>()
        
        // 检查频繁请求
        if (stats.requestsInLastHour >= PermissionConfig.maxRequestsPerHour) {
            issues.add("权限请求过于频繁（1小时内${stats.requestsInLastHour}次）")
            severity.add(AbuseSeverity.HIGH)
        } else if (stats.requestsInLastHour >= PermissionConfig.maxRequestsPerHour * 0.8) {
            issues.add("权限请求频率较高（1小时内${stats.requestsInLastHour}次）")
            severity.add(AbuseSeverity.MEDIUM)
        }
        
        // 检查短时间内重复请求
        if (!stats.canRequestNow && stats.nextRequestTime > System.currentTimeMillis()) {
            val waitTime = (stats.nextRequestTime - System.currentTimeMillis()) / 1000
            issues.add("权限请求间隔过短，需等待${waitTime}秒")
            severity.add(AbuseSeverity.LOW)
        }
        
        // 检查异常高频请求
        if (stats.requestsInLastDay >= PermissionConfig.maxRequestsPerHour * 5) {
            issues.add("24小时内权限请求次数异常（${stats.requestsInLastDay}次）")
            severity.add(AbuseSeverity.CRITICAL)
        }
        
        val maxSeverity = severity.maxOrNull() ?: AbuseSeverity.NONE
        
        return AbuseDetectionResult(
            permission = permission,
            isAbusive = issues.isNotEmpty(),
            severity = maxSeverity,
            issues = issues,
            stats = stats,
            recommendation = generateRecommendation(maxSeverity, stats)
        )
    }
    
    /**
     * 生成建议
     */
    private fun generateRecommendation(severity: AbuseSeverity, stats: PermissionRequestStats): String {
        return when (severity) {
            AbuseSeverity.CRITICAL -> "建议暂停权限请求功能，检查应用逻辑是否存在问题"
            AbuseSeverity.HIGH -> "建议增加权限请求间隔，避免频繁打扰用户"
            AbuseSeverity.MEDIUM -> "建议优化权限请求时机，提高用户体验"
            AbuseSeverity.LOW -> "建议等待${(stats.nextRequestTime - System.currentTimeMillis()) / 1000}秒后再次请求"
            AbuseSeverity.NONE -> "权限请求频率正常"
        }
    }
}

/**
 * 滥用检测结果
 */
data class AbuseDetectionResult(
    /**
     * 权限名称
     */
    val permission: String,
    
    /**
     * 是否存在滥用
     */
    val isAbusive: Boolean,
    
    /**
     * 滥用严重程度
     */
    val severity: AbuseSeverity,
    
    /**
     * 问题列表
     */
    val issues: List<String>,
    
    /**
     * 统计信息
     */
    val stats: PermissionRequestStats,
    
    /**
     * 建议
     */
    val recommendation: String
)

/**
 * 滥用严重程度
 */
enum class AbuseSeverity {
    NONE,       // 无问题
    LOW,        // 轻微
    MEDIUM,     // 中等
    HIGH,       // 严重
    CRITICAL    // 极严重
}