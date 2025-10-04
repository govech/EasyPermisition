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
         * 在Activity的onCreate方法中初始化权限管理器
         * 这样可以避免"LifecycleOwner is attempting to register while current state is RESUMED"错误
         * 
         * @param activity Activity实例
         */
        @JvmStatic
        fun initialize(activity: ComponentActivity) {
            PermissionExecutorManager.getOrCreateExecutor(activity)
        }
        
        /**
         * 在Fragment的onCreate方法中初始化权限管理器
         * 
         * @param fragment Fragment实例
         */
        @JvmStatic
        fun initialize(fragment: Fragment) {
            PermissionExecutorManager.getOrCreateExecutor(fragment)
        }
        
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
        
        /**
         * 使用Activity创建权限请求构建器（延迟初始化版本）
         * 如果遇到生命周期问题，会提供更友好的错误信息
         */
        @JvmStatic
        fun withLazy(activity: ComponentActivity): PermissionRequestBuilder {
            // 尝试初始化执行器，如果失败会给出友好的错误提示
            try {
                PermissionExecutorManager.getOrCreateExecutor(activity)
            } catch (e: IllegalStateException) {
                // 忽略错误，在实际请求时再处理
            }
            return PermissionRequestBuilder(activity)
        }
    }
}