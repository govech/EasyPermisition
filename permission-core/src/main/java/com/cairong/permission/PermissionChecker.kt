package com.cairong.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * 权限检查工具类
 * 
 * 提供权限预检查功能，避免重复申请已授权权限
 */
object PermissionChecker {
    
    /**
     * 检查单个权限是否已授权
     * 
     * @param context 上下文
     * @param permission 权限名称
     * @return 是否已授权
     */
    @JvmStatic
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查多个权限是否都已授权
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 是否所有权限都已授权
     */
    @JvmStatic
    fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            isPermissionGranted(context, permission)
        }
    }
    
    /**
     * 过滤出未授权的权限
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 未授权的权限列表
     */
    @JvmStatic
    fun filterDeniedPermissions(context: Context, permissions: Array<String>): Array<String> {
        return permissions.filter { permission ->
            !isPermissionGranted(context, permission)
        }.toTypedArray()
    }
    
    /**
     * 过滤出已授权的权限
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 已授权的权限列表
     */
    @JvmStatic
    fun filterGrantedPermissions(context: Context, permissions: Array<String>): Array<String> {
        return permissions.filter { permission ->
            isPermissionGranted(context, permission)
        }.toTypedArray()
    }
    
    /**
     * 获取权限授权统计信息
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 权限统计信息
     */
    @JvmStatic
    fun getPermissionStats(context: Context, permissions: Array<String>): PermissionStats {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        
        permissions.forEach { permission ->
            if (isPermissionGranted(context, permission)) {
                granted.add(permission)
            } else {
                denied.add(permission)
            }
        }
        
        return PermissionStats(
            totalCount = permissions.size,
            grantedCount = granted.size,
            deniedCount = denied.size,
            grantedPermissions = granted.toTypedArray(),
            deniedPermissions = denied.toTypedArray()
        )
    }
}

/**
 * 权限统计信息
 */
data class PermissionStats(
    val totalCount: Int,
    val grantedCount: Int,
    val deniedCount: Int,
    val grantedPermissions: Array<String>,
    val deniedPermissions: Array<String>
) {
    
    /**
     * 是否所有权限都已授权
     */
    val allGranted: Boolean
        get() = deniedCount == 0
    
    /**
     * 是否所有权限都被拒绝
     */
    val allDenied: Boolean
        get() = grantedCount == 0
    
    /**
     * 是否部分权限已授权
     */
    val partiallyGranted: Boolean
        get() = grantedCount > 0 && deniedCount > 0
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionStats

        if (totalCount != other.totalCount) return false
        if (grantedCount != other.grantedCount) return false
        if (deniedCount != other.deniedCount) return false
        if (!grantedPermissions.contentEquals(other.grantedPermissions)) return false
        if (!deniedPermissions.contentEquals(other.deniedPermissions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalCount
        result = 31 * result + grantedCount
        result = 31 * result + deniedCount
        result = 31 * result + grantedPermissions.contentHashCode()
        result = 31 * result + deniedPermissions.contentHashCode()
        return result
    }
}