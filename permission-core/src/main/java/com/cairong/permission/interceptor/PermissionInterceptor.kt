package com.cairong.permission.interceptor

import com.cairong.permission.PermissionRequest

/**
 * 权限拦截器接口
 * 
 * 允许第三方模块扩展权限请求的处理逻辑
 */
interface PermissionInterceptor {
    
    /**
     * 权限请求前拦截
     * 
     * @param request 权限请求信息
     * @return 是否继续执行权限请求，false表示拦截
     */
    fun beforeRequest(request: PermissionRequest): Boolean = true
    
    /**
     * 权限请求后拦截
     * 
     * @param request 权限请求信息
     * @param results 权限请求结果
     */
    fun afterRequest(request: PermissionRequest, results: Map<String, Boolean>) {}
    
    /**
     * 权限授权成功拦截
     * 
     * @param request 权限请求信息
     * @param grantedPermissions 已授权的权限列表
     */
    fun onGranted(request: PermissionRequest, grantedPermissions: Array<String>) {}
    
    /**
     * 权限被拒绝拦截
     * 
     * @param request 权限请求信息
     * @param deniedPermissions 被拒绝的权限列表
     * @param permanentlyDeniedPermissions 被永久拒绝的权限列表
     */
    fun onDenied(
        request: PermissionRequest,
        deniedPermissions: Array<String>,
        permanentlyDeniedPermissions: Array<String>
    ) {}
    
    /**
     * 权限被永久拒绝拦截
     * 
     * @param request 权限请求信息
     * @param permanentlyDeniedPermissions 被永久拒绝的权限列表
     */
    fun onPermanentlyDenied(
        request: PermissionRequest,
        permanentlyDeniedPermissions: Array<String>
    ) {}
    
    /**
     * 权限请求异常拦截
     * 
     * @param request 权限请求信息
     * @param exception 异常信息
     */
    fun onError(request: PermissionRequest, exception: Throwable) {}
}

/**
 * 权限拦截器注册管理器
 */
object PermissionInterceptorRegistry {
    
    private val interceptors = mutableListOf<PermissionInterceptor>()
    
    /**
     * 注册权限拦截器
     * 
     * @param interceptor 拦截器实例
     */
    @JvmStatic
    fun register(interceptor: PermissionInterceptor) {
        synchronized(interceptors) {
            if (!interceptors.contains(interceptor)) {
                interceptors.add(interceptor)
            }
        }
    }
    
    /**
     * 注销权限拦截器
     * 
     * @param interceptor 拦截器实例
     */
    @JvmStatic
    fun unregister(interceptor: PermissionInterceptor) {
        synchronized(interceptors) {
            interceptors.remove(interceptor)
        }
    }
    
    /**
     * 清空所有拦截器
     */
    @JvmStatic
    fun clear() {
        synchronized(interceptors) {
            interceptors.clear()
        }
    }
    
    /**
     * 获取所有拦截器
     */
    @JvmStatic
    fun getInterceptors(): List<PermissionInterceptor> {
        synchronized(interceptors) {
            return interceptors.toList()
        }
    }
    
    /**
     * 执行权限请求前拦截
     */
    internal fun executeBeforeRequest(request: PermissionRequest): Boolean {
        synchronized(interceptors) {
            return interceptors.all { it.beforeRequest(request) }
        }
    }
    
    /**
     * 执行权限请求后拦截
     */
    internal fun executeAfterRequest(request: PermissionRequest, results: Map<String, Boolean>) {
        synchronized(interceptors) {
            interceptors.forEach { it.afterRequest(request, results) }
        }
    }
    
    /**
     * 执行权限授权成功拦截
     */
    internal fun executeOnGranted(request: PermissionRequest, grantedPermissions: Array<String>) {
        synchronized(interceptors) {
            interceptors.forEach { it.onGranted(request, grantedPermissions) }
        }
    }
    
    /**
     * 执行权限被拒绝拦截
     */
    internal fun executeOnDenied(
        request: PermissionRequest,
        deniedPermissions: Array<String>,
        permanentlyDeniedPermissions: Array<String>
    ) {
        synchronized(interceptors) {
            interceptors.forEach { 
                it.onDenied(request, deniedPermissions, permanentlyDeniedPermissions) 
            }
        }
    }
    
    /**
     * 执行权限被永久拒绝拦截
     */
    internal fun executeOnPermanentlyDenied(
        request: PermissionRequest,
        permanentlyDeniedPermissions: Array<String>
    ) {
        synchronized(interceptors) {
            interceptors.forEach { it.onPermanentlyDenied(request, permanentlyDeniedPermissions) }
        }
    }
    
    /**
     * 执行权限请求异常拦截
     */
    internal fun executeOnError(request: PermissionRequest, exception: Throwable) {
        synchronized(interceptors) {
            interceptors.forEach { it.onError(request, exception) }
        }
    }
}

/**
 * 埋点拦截器
 * 
 * 用于统计权限请求的各种数据
 */
class AnalyticsPermissionInterceptor(
    private val analyticsCallback: (String, Map<String, Any>) -> Unit
) : PermissionInterceptor {
    
    override fun beforeRequest(request: PermissionRequest): Boolean {
        val params = mapOf(
            "permissions" to request.permissions.toList(),
            "permission_count" to request.permissions.size,
            "has_rationale" to (request.rationale != null),
            "timestamp" to System.currentTimeMillis()
        )
        analyticsCallback("permission_request_started", params)
        return true
    }
    
    override fun afterRequest(request: PermissionRequest, results: Map<String, Boolean>) {
        val grantedCount = results.values.count { it }
        val deniedCount = results.size - grantedCount
        
        val params = mapOf(
            "permissions" to request.permissions.toList(),
            "results" to results,
            "granted_count" to grantedCount,
            "denied_count" to deniedCount,
            "all_granted" to (deniedCount == 0),
            "timestamp" to System.currentTimeMillis()
        )
        analyticsCallback("permission_request_completed", params)
    }
    
    override fun onGranted(request: PermissionRequest, grantedPermissions: Array<String>) {
        val params = mapOf(
            "permissions" to grantedPermissions.toList(),
            "permission_count" to grantedPermissions.size,
            "timestamp" to System.currentTimeMillis()
        )
        analyticsCallback("permissions_granted", params)
    }
    
    override fun onDenied(
        request: PermissionRequest,
        deniedPermissions: Array<String>,
        permanentlyDeniedPermissions: Array<String>
    ) {
        val params = mapOf(
            "denied_permissions" to deniedPermissions.toList(),
            "permanently_denied_permissions" to permanentlyDeniedPermissions.toList(),
            "denied_count" to deniedPermissions.size,
            "permanently_denied_count" to permanentlyDeniedPermissions.size,
            "timestamp" to System.currentTimeMillis()
        )
        analyticsCallback("permissions_denied", params)
    }
    
    override fun onError(request: PermissionRequest, exception: Throwable) {
        val params = mapOf(
            "permissions" to request.permissions.toList(),
            "error_type" to exception.javaClass.simpleName,
            "error_message" to (exception.message ?: "Unknown error"),
            "timestamp" to System.currentTimeMillis()
        )
        analyticsCallback("permission_request_error", params)
    }
}

/**
 * 日志拦截器
 * 
 * 用于记录权限请求的详细日志
 */
class LoggingPermissionInterceptor(
    private val logCallback: (String, String) -> Unit = { tag, message -> 
        println("[$tag] $message") 
    }
) : PermissionInterceptor {
    
    companion object {
        private const val TAG = "PermissionInterceptor"
    }
    
    override fun beforeRequest(request: PermissionRequest): Boolean {
        logCallback(TAG, "权限请求开始: ${request.permissions.joinToString()}")
        if (request.rationale != null) {
            logCallback(TAG, "权限解释: ${request.rationale}")
        }
        return true
    }
    
    override fun afterRequest(request: PermissionRequest, results: Map<String, Boolean>) {
        val granted = results.filter { it.value }.keys
        val denied = results.filter { !it.value }.keys
        
        logCallback(TAG, "权限请求完成")
        if (granted.isNotEmpty()) {
            logCallback(TAG, "已授权权限: ${granted.joinToString()}")
        }
        if (denied.isNotEmpty()) {
            logCallback(TAG, "被拒绝权限: ${denied.joinToString()}")
        }
    }
    
    override fun onGranted(request: PermissionRequest, grantedPermissions: Array<String>) {
        logCallback(TAG, "权限授权成功: ${grantedPermissions.joinToString()}")
    }
    
    override fun onDenied(
        request: PermissionRequest,
        deniedPermissions: Array<String>,
        permanentlyDeniedPermissions: Array<String>
    ) {
        if (deniedPermissions.isNotEmpty()) {
            logCallback(TAG, "权限被拒绝: ${deniedPermissions.joinToString()}")
        }
        if (permanentlyDeniedPermissions.isNotEmpty()) {
            logCallback(TAG, "权限被永久拒绝: ${permanentlyDeniedPermissions.joinToString()}")
        }
    }
    
    override fun onError(request: PermissionRequest, exception: Throwable) {
        logCallback(TAG, "权限请求出错: ${exception.message}")
    }
}