package com.cairong.permission

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

/**
 * 权限结果聚合器
 * 
 * 负责处理多权限请求的结果聚合逻辑
 */
class PermissionResultAggregator {
    
    /**
     * 聚合多权限请求结果
     * 
     * @param context 上下文
     * @param requestedPermissions 请求的权限列表
     * @param results 权限请求结果
     * @return 聚合后的权限结果
     */
    fun aggregateResults(
        context: Context,
        requestedPermissions: Array<String>,
        results: Map<String, Boolean>
    ): AggregatedPermissionResult {
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()
        val permanentlyDeniedPermissions = mutableListOf<String>()
        
        requestedPermissions.forEach { permission ->
            val isGranted = results[permission] ?: false
            
            if (isGranted) {
                grantedPermissions.add(permission)
            } else {
                deniedPermissions.add(permission)
                
                // 检查是否被永久拒绝
                if (isPermanentlyDenied(context, permission)) {
                    permanentlyDeniedPermissions.add(permission)
                }
            }
        }
        
        return AggregatedPermissionResult(
            allGranted = deniedPermissions.isEmpty(),
            grantedPermissions = grantedPermissions.toTypedArray(),
            deniedPermissions = deniedPermissions.toTypedArray(),
            permanentlyDeniedPermissions = permanentlyDeniedPermissions.toTypedArray()
        )
    }
    
    /**
     * 聚合多权限请求结果（Fragment版本）
     */
    fun aggregateResults(
        fragment: Fragment,
        requestedPermissions: Array<String>,
        results: Map<String, Boolean>
    ): AggregatedPermissionResult {
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()
        val permanentlyDeniedPermissions = mutableListOf<String>()
        
        requestedPermissions.forEach { permission ->
            val isGranted = results[permission] ?: false
            
            if (isGranted) {
                grantedPermissions.add(permission)
            } else {
                deniedPermissions.add(permission)
                
                // 检查是否被永久拒绝
                if (isPermanentlyDenied(fragment, permission)) {
                    permanentlyDeniedPermissions.add(permission)
                }
            }
        }
        
        return AggregatedPermissionResult(
            allGranted = deniedPermissions.isEmpty(),
            grantedPermissions = grantedPermissions.toTypedArray(),
            deniedPermissions = deniedPermissions.toTypedArray(),
            permanentlyDeniedPermissions = permanentlyDeniedPermissions.toTypedArray()
        )
    }
    
    /**
     * 检查权限是否被永久拒绝
     */
    private fun isPermanentlyDenied(context: Context, permission: String): Boolean {
        return try {
            if (context is ComponentActivity) {
                !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                    context, permission
                )
            } else {
                // 对于非Activity上下文，无法准确判断，默认返回false
                false
            }
        } catch (e: Exception) {
            // 如果出现异常，默认返回false
            false
        }
    }
    
    /**
     * 检查权限是否被永久拒绝（Fragment版本）
     */
    private fun isPermanentlyDenied(fragment: Fragment, permission: String): Boolean {
        return !fragment.shouldShowRequestPermissionRationale(permission)
    }
}

/**
 * 聚合后的权限结果
 */
data class AggregatedPermissionResult(
    /**
     * 是否所有权限都已授权
     */
    val allGranted: Boolean,
    
    /**
     * 已授权的权限列表
     */
    val grantedPermissions: Array<String>,
    
    /**
     * 被拒绝的权限列表
     */
    val deniedPermissions: Array<String>,
    
    /**
     * 被永久拒绝的权限列表
     */
    val permanentlyDeniedPermissions: Array<String>
) {
    
    /**
     * 是否有权限被拒绝
     */
    val hasDeniedPermissions: Boolean
        get() = deniedPermissions.isNotEmpty()
    
    /**
     * 是否有权限被永久拒绝
     */
    val hasPermanentlyDeniedPermissions: Boolean
        get() = permanentlyDeniedPermissions.isNotEmpty()
    
    /**
     * 是否部分权限已授权
     */
    val isPartiallyGranted: Boolean
        get() = grantedPermissions.isNotEmpty() && deniedPermissions.isNotEmpty()
    
    /**
     * 获取临时拒绝的权限（非永久拒绝）
     */
    val temporarilyDeniedPermissions: Array<String>
        get() = deniedPermissions.filter { permission ->
            !permanentlyDeniedPermissions.contains(permission)
        }.toTypedArray()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AggregatedPermissionResult

        if (allGranted != other.allGranted) return false
        if (!grantedPermissions.contentEquals(other.grantedPermissions)) return false
        if (!deniedPermissions.contentEquals(other.deniedPermissions)) return false
        if (!permanentlyDeniedPermissions.contentEquals(other.permanentlyDeniedPermissions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = allGranted.hashCode()
        result = 31 * result + grantedPermissions.contentHashCode()
        result = 31 * result + deniedPermissions.contentHashCode()
        result = 31 * result + permanentlyDeniedPermissions.contentHashCode()
        return result
    }
}