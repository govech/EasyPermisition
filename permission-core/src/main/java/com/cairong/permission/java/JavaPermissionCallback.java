package com.cairong.permission.java;

/**
 * Java友好的权限回调接口
 * 
 * 提供简化的回调方法，适合Java开发者使用
 */
public interface JavaPermissionCallback {
    
    /**
     * 权限请求前回调
     * 
     * @param permissions 即将请求的权限列表
     */
    default void onBeforeRequest(String[] permissions) {
        // 默认空实现
    }
    
    /**
     * 权限授权成功回调
     * 
     * @param permissions 已授权的权限列表
     */
    void onGranted(String[] permissions);
    
    /**
     * 权限被拒绝回调
     * 
     * @param deniedPermissions 被拒绝的权限列表
     * @param permanentlyDeniedPermissions 被永久拒绝的权限列表
     */
    default void onDenied(String[] deniedPermissions, String[] permanentlyDeniedPermissions) {
        // 默认空实现
    }
    
    /**
     * 权限被永久拒绝回调
     * 
     * @param permanentlyDeniedPermissions 被永久拒绝的权限列表
     */
    default void onPermanentlyDenied(String[] permanentlyDeniedPermissions) {
        // 默认空实现
    }
}

