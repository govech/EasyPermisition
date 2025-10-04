package com.cairong.permission

/**
 * 权限请求回调接口
 * 
 * 定义权限请求过程中的各种回调方法
 */
interface PermissionCallback {
    
    /**
     * 权限请求前回调
     * 
     * @param permissions 即将请求的权限列表
     */
    fun onBeforeRequest(permissions: Array<String>) {}
    
    /**
     * 权限授权成功回调
     * 
     * @param permissions 已授权的权限列表
     */
    fun onGranted(permissions: Array<String>)
    
    /**
     * 权限被拒绝回调
     * 
     * @param deniedPermissions 被拒绝的权限列表
     * @param permanentlyDeniedPermissions 被永久拒绝的权限列表
     */
    fun onDenied(
        deniedPermissions: Array<String>,
        permanentlyDeniedPermissions: Array<String>
    ) {}
    
    /**
     * 权限被永久拒绝回调
     * 
     * @param permanentlyDeniedPermissions 被永久拒绝的权限列表
     */
    fun onPermanentlyDenied(permanentlyDeniedPermissions: Array<String>) {}
}

/**
 * 简化的权限回调接口
 * 适用于只关心授权结果的场景
 */
interface SimplePermissionCallback {
    /**
     * 权限请求结果回调
     * 
     * @param allGranted 是否所有权限都已授权
     * @param grantedPermissions 已授权的权限列表
     * @param deniedPermissions 被拒绝的权限列表
     */
    fun onResult(
        allGranted: Boolean,
        grantedPermissions: Array<String>,
        deniedPermissions: Array<String>
    )
}