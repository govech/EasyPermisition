package com.cairong.permission.java;

/**
 * 简化的Java权限回调接口
 * 只关心最终结果的场景
 */
public interface SimpleJavaPermissionCallback {
    
    /**
     * 权限请求结果回调
     * 
     * @param allGranted 是否所有权限都已授权
     * @param grantedPermissions 已授权的权限列表
     * @param deniedPermissions 被拒绝的权限列表
     */
    void onResult(boolean allGranted, String[] grantedPermissions, String[] deniedPermissions);
}