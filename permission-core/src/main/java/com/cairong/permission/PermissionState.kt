package com.cairong.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * 权限状态枚举
 */
enum class PermissionState {
    /**
     * 权限已授权
     */
    GRANTED,
    
    /**
     * 权限被拒绝（首次拒绝，可以显示解释）
     */
    DENIED,
    
    /**
     * 权限被永久拒绝（用户选择了"不再询问"）
     */
    PERMANENTLY_DENIED
}

/**
 * 权限状态检查器
 * 
 * 提供权限状态检查的工具方法
 */
object PermissionStateChecker {
    
    /**
     * 检查单个权限的状态
     * 
     * @param context 上下文
     * @param permission 权限名称
     * @return 权限状态
     */
    @JvmStatic
    fun checkPermissionState(context: Context, permission: String): PermissionState {
        // 检查权限是否已授权
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return PermissionState.GRANTED
        }
        
        // 对于Activity，检查是否应该显示权限解释
        if (context is ComponentActivity) {
            return if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                PermissionState.DENIED
            } else {
                PermissionState.PERMANENTLY_DENIED
            }
        }
        
        // 对于其他情况，默认返回DENIED
        return PermissionState.DENIED
    }
    
    /**
     * 检查单个权限的状态（Fragment版本）
     * 
     * @param fragment Fragment实例
     * @param permission 权限名称
     * @return 权限状态
     */
    @JvmStatic
    fun checkPermissionState(fragment: Fragment, permission: String): PermissionState {
        val context = fragment.requireContext()
        
        // 检查权限是否已授权
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return PermissionState.GRANTED
        }
        
        // 检查是否应该显示权限解释
        return if (fragment.shouldShowRequestPermissionRationale(permission)) {
            PermissionState.DENIED
        } else {
            PermissionState.PERMANENTLY_DENIED
        }
    }
    
    /**
     * 检查多个权限的状态
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 权限状态映射
     */
    @JvmStatic
    fun checkPermissionsState(context: Context, permissions: Array<String>): Map<String, PermissionState> {
        return permissions.associateWith { permission ->
            checkPermissionState(context, permission)
        }
    }
    
    /**
     * 检查多个权限的状态（Fragment版本）
     * 
     * @param fragment Fragment实例
     * @param permissions 权限列表
     * @return 权限状态映射
     */
    @JvmStatic
    fun checkPermissionsState(fragment: Fragment, permissions: Array<String>): Map<String, PermissionState> {
        return permissions.associateWith { permission ->
            checkPermissionState(fragment, permission)
        }
    }
    
    /**
     * 检查是否所有权限都已授权
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 是否所有权限都已授权
     */
    @JvmStatic
    fun areAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取已授权的权限列表
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 已授权的权限列表
     */
    @JvmStatic
    fun getGrantedPermissions(context: Context, permissions: Array<String>): Array<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
    
    /**
     * 获取被拒绝的权限列表
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 被拒绝的权限列表
     */
    @JvmStatic
    fun getDeniedPermissions(context: Context, permissions: Array<String>): Array<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }
    
    /**
     * 获取被永久拒绝的权限列表
     * 
     * @param context 上下文
     * @param permissions 权限列表
     * @return 被永久拒绝的权限列表
     */
    @JvmStatic
    fun getPermanentlyDeniedPermissions(context: Context, permissions: Array<String>): Array<String> {
        if (context !is ComponentActivity) {
            return emptyArray()
        }
        
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED &&
            !ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
        }.toTypedArray()
    }
    
    /**
     * 获取被永久拒绝的权限列表（Fragment版本）
     * 
     * @param fragment Fragment实例
     * @param permissions 权限列表
     * @return 被永久拒绝的权限列表
     */
    @JvmStatic
    fun getPermanentlyDeniedPermissions(fragment: Fragment, permissions: Array<String>): Array<String> {
        val context = fragment.requireContext()
        
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED &&
            !fragment.shouldShowRequestPermissionRationale(permission)
        }.toTypedArray()
    }
}