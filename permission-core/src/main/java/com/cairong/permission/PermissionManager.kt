package com.cairong.permission

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

/**
 * Android权限请求框架的主入口
 * 
 * 提供链式调用API来简化权限请求流程
 * 支持Activity和Fragment上下文
 * 
 * 使用示例：
 * ```kotlin
 * PermissionManager.with(this)
 *     .permission(Manifest.permission.CAMERA)
 *     .onGranted { openCamera() }
 *     .onDenied { showMessage("需要相机权限") }
 *     .request()
 * ```
 */
class PermissionManager private constructor() {
    
    companion object {
        /**
         * 使用Activity创建权限请求构建器
         */
        @JvmStatic
        fun with(activity: ComponentActivity): PermissionRequestBuilder {
            return PermissionRequestBuilder(activity)
        }
        
        /**
         * 使用Fragment创建权限请求构建器
         */
        @JvmStatic
        fun with(fragment: Fragment): PermissionRequestBuilder {
            return PermissionRequestBuilder(fragment = fragment)
        }
    }
}