package com.cairong.permission.circuit

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 权限请求熔断器
 * 
 * 防止权限请求过程中的异常情况，如Activity销毁、超时等
 */
class PermissionCircuitBreaker(
    private val lifecycleOwner: LifecycleOwner,
    private val timeoutMs: Long = 30000L // 30秒超时
) : DefaultLifecycleObserver {
    
    private val isDestroyed = AtomicBoolean(false)
    private val activeRequests = ConcurrentHashMap<String, RequestInfo>()
    private val handler = Handler(Looper.getMainLooper())
    
    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }
    
    /**
     * 请求信息
     */
    private data class RequestInfo(
        val requestId: String,
        val startTime: Long,
        val timeoutRunnable: Runnable,
        val onTimeout: () -> Unit,
        val onCancel: () -> Unit
    )
    
    /**
     * 开始权限请求
     * 
     * @param requestId 请求ID
     * @param onTimeout 超时回调
     * @param onCancel 取消回调
     * @return 是否成功开始（如果已销毁则返回false）
     */
    fun startRequest(
        requestId: String,
        onTimeout: () -> Unit = {},
        onCancel: () -> Unit = {}
    ): Boolean {
        if (isDestroyed.get()) {
            return false
        }
        
        val timeoutRunnable = Runnable {
            handleTimeout(requestId)
        }
        
        val requestInfo = RequestInfo(
            requestId = requestId,
            startTime = System.currentTimeMillis(),
            timeoutRunnable = timeoutRunnable,
            onTimeout = onTimeout,
            onCancel = onCancel
        )
        
        activeRequests[requestId] = requestInfo
        handler.postDelayed(timeoutRunnable, timeoutMs)
        
        return true
    }
    
    /**
     * 完成权限请求
     * 
     * @param requestId 请求ID
     */
    fun completeRequest(requestId: String) {
        activeRequests.remove(requestId)?.let { requestInfo ->
            handler.removeCallbacks(requestInfo.timeoutRunnable)
        }
    }
    
    /**
     * 处理超时
     */
    private fun handleTimeout(requestId: String) {
        activeRequests.remove(requestId)?.let { requestInfo ->
            if (!isDestroyed.get()) {
                requestInfo.onTimeout()
            }
        }
    }
    
    /**
     * 检查是否已销毁
     */
    fun isDestroyed(): Boolean {
        return isDestroyed.get()
    }
    
    /**
     * 获取活跃请求数量
     */
    fun getActiveRequestCount(): Int {
        return activeRequests.size
    }
    
    /**
     * 获取请求持续时间
     */
    fun getRequestDuration(requestId: String): Long {
        return activeRequests[requestId]?.let { requestInfo ->
            System.currentTimeMillis() - requestInfo.startTime
        } ?: 0L
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        isDestroyed.set(true)
        
        // 取消所有活跃请求
        activeRequests.values.forEach { requestInfo ->
            handler.removeCallbacks(requestInfo.timeoutRunnable)
            requestInfo.onCancel()
        }
        
        activeRequests.clear()
        super.onDestroy(owner)
    }
}

/**
 * 全局熔断器管理器
 */
object CircuitBreakerManager {
    
    private val circuitBreakers = ConcurrentHashMap<String, PermissionCircuitBreaker>()
    private val requestCounter = AtomicInteger(0)
    
    /**
     * 获取或创建熔断器
     */
    fun getOrCreateCircuitBreaker(
        key: String,
        lifecycleOwner: LifecycleOwner,
        timeoutMs: Long = 30000L
    ): PermissionCircuitBreaker {
        return circuitBreakers.getOrPut(key) {
            val circuitBreaker = PermissionCircuitBreaker(lifecycleOwner, timeoutMs)
            
            // 监听生命周期，在销毁时清理
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    circuitBreakers.remove(key)
                    super.onDestroy(owner)
                }
            })
            
            circuitBreaker
        }
    }
    
    /**
     * 生成唯一请求ID
     */
    fun generateRequestId(): String {
        return "permission_request_${requestCounter.incrementAndGet()}_${System.currentTimeMillis()}"
    }
    
    /**
     * 获取所有活跃的熔断器数量
     */
    fun getActiveCircuitBreakerCount(): Int {
        return circuitBreakers.size
    }
    
    /**
     * 清理所有熔断器
     */
    fun clearAll() {
        circuitBreakers.clear()
    }
}