package com.cairong.permission.java;

import androidx.activity.ComponentActivity;
import androidx.fragment.app.Fragment;
import com.cairong.permission.PermissionManager;
import com.cairong.permission.PermissionRequestBuilder;

/**
 * Java兼容的权限管理器
 * 
 * 为Java开发者提供友好的静态方法API
 * 
 * 使用示例：
 * <pre>
 * JavaPermissionManager.with(this)
 *     .permission(Manifest.permission.CAMERA)
 *     .onGranted(permissions -> openCamera())
 *     .onDenied((denied, permanent) -> showMessage("需要相机权限"))
 *     .request();
 * </pre>
 */
public final class JavaPermissionManager {
    
    private JavaPermissionManager() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 使用Activity创建权限请求构建器
     * 
     * @param activity ComponentActivity实例
     * @return 权限请求构建器
     */
    public static PermissionRequestBuilder with(ComponentActivity activity) {
        return PermissionManager.with(activity);
    }
    
    /**
     * 使用Fragment创建权限请求构建器
     * 
     * @param fragment Fragment实例
     * @return 权限请求构建器
     */
    public static PermissionRequestBuilder with(Fragment fragment) {
        return PermissionManager.with(fragment);
    }
}