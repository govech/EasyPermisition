package com.cairong.permission

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

/**
 * 权限请求构建器
 * 
 * 提供链式调用API来构建权限请求
 */
class PermissionRequestBuilder internal constructor(
    private val activity: ComponentActivity? = null,
    private val fragment: Fragment? = null
) {
    
    private val permissions = mutableListOf<String>()
    private var rationale: String? = null
    private var rationaleTitle: String? = null
    private var positiveButtonText: String = "确定"
    private var negativeButtonText: String = "取消"
    private var settingsText: String? = null
    private var settingsTitle: String? = null
    private var forceGoToSettings: Boolean = false
    
    // 回调函数
    private var onBeforeRequestCallback: ((Array<String>) -> Unit)? = null
    private var onGrantedCallback: ((Array<String>) -> Unit)? = null
    private var onDeniedCallback: ((Array<String>, Array<String>) -> Unit)? = null
    private var onPermanentlyDeniedCallback: ((Array<String>) -> Unit)? = null
    
    // 处理器
    private var rationaleHandler: RationaleHandler? = null
    private var settingsHandler: SettingsHandler? = null
    
    /**
     * 添加单个权限
     */
    fun permission(permission: String): PermissionRequestBuilder {
        permissions.add(permission)
        return this
    }
    
    /**
     * 添加多个权限
     */
    fun permissions(vararg permissions: String): PermissionRequestBuilder {
        this.permissions.addAll(permissions)
        return this
    }
    
    /**
     * 添加权限列表
     */
    fun permissions(permissions: List<String>): PermissionRequestBuilder {
        this.permissions.addAll(permissions)
        return this
    }
    
    /**
     * 添加权限组
     */
    fun permissionGroup(group: Array<String>): PermissionRequestBuilder {
        this.permissions.addAll(group)
        return this
    }
    
    /**
     * 添加位置权限组
     */
    fun locationPermissions(): PermissionRequestBuilder {
        return permissionGroup(PermissionGroups.LOCATION_PERMISSIONS)
    }
    
    /**
     * 添加存储权限组
     */
    fun storagePermissions(): PermissionRequestBuilder {
        return permissionGroup(PermissionGroups.STORAGE_PERMISSIONS)
    }
    
    /**
     * 添加媒体权限组（Android 13+）
     */
    fun mediaPermissions(): PermissionRequestBuilder {
        return permissionGroup(PermissionGroups.MEDIA_PERMISSIONS_ANDROID_13)
    }
    
    /**
     * 添加相机和音频权限组
     */
    fun cameraAndAudioPermissions(): PermissionRequestBuilder {
        return permissionGroup(PermissionGroups.CAMERA_AND_AUDIO_PERMISSIONS)
    }
    
    /**
     * 设置权限解释文案
     */
    fun rationale(rationale: String): PermissionRequestBuilder {
        this.rationale = rationale
        return this
    }
    
    /**
     * 设置权限解释标题
     */
    fun rationaleTitle(title: String): PermissionRequestBuilder {
        this.rationaleTitle = title
        return this
    }
    
    /**
     * 设置确认按钮文案
     */
    fun positiveButtonText(text: String): PermissionRequestBuilder {
        this.positiveButtonText = text
        return this
    }
    
    /**
     * 设置取消按钮文案
     */
    fun negativeButtonText(text: String): PermissionRequestBuilder {
        this.negativeButtonText = text
        return this
    }
    
    /**
     * 设置跳转设置页面的提示文案
     */
    fun settingsText(text: String): PermissionRequestBuilder {
        this.settingsText = text
        return this
    }
    
    /**
     * 设置跳转设置页面的标题
     */
    fun settingsTitle(title: String): PermissionRequestBuilder {
        this.settingsTitle = title
        return this
    }
    
    /**
     * 设置是否在永久拒绝后强制跳转设置页面
     */
    fun forceGoToSettings(force: Boolean = true): PermissionRequestBuilder {
        this.forceGoToSettings = force
        return this
    }
    
    /**
     * 设置权限请求前回调
     */
    fun onBeforeRequest(callback: (Array<String>) -> Unit): PermissionRequestBuilder {
        this.onBeforeRequestCallback = callback
        return this
    }
    
    /**
     * 设置权限授权成功回调
     */
    fun onGranted(callback: (Array<String>) -> Unit): PermissionRequestBuilder {
        this.onGrantedCallback = callback
        return this
    }
    
    /**
     * 设置权限被拒绝回调
     */
    fun onDenied(callback: (Array<String>, Array<String>) -> Unit): PermissionRequestBuilder {
        this.onDeniedCallback = callback
        return this
    }
    
    /**
     * 设置权限被永久拒绝回调
     */
    fun onPermanentlyDenied(callback: (Array<String>) -> Unit): PermissionRequestBuilder {
        this.onPermanentlyDeniedCallback = callback
        return this
    }
    
    /**
     * 设置简化的结果回调
     */
    fun onResult(callback: (Boolean, Array<String>, Array<String>) -> Unit): PermissionRequestBuilder {
        this.onGrantedCallback = { grantedPermissions ->
            callback(true, grantedPermissions, emptyArray())
        }
        this.onDeniedCallback = { deniedPermissions, permanentlyDeniedPermissions ->
            val allDenied = deniedPermissions + permanentlyDeniedPermissions
            callback(false, emptyArray(), allDenied)
        }
        return this
    }
    
    /**
     * 设置Java友好的权限回调
     */
    fun onJavaCallback(callback: com.cairong.permission.java.JavaPermissionCallback): PermissionRequestBuilder {
        this.onBeforeRequestCallback = { permissions -> callback.onBeforeRequest(permissions) }
        this.onGrantedCallback = { permissions -> callback.onGranted(permissions) }
        this.onDeniedCallback = { deniedPermissions, permanentlyDeniedPermissions ->
            callback.onDenied(deniedPermissions, permanentlyDeniedPermissions)
        }
        this.onPermanentlyDeniedCallback = { permanentlyDeniedPermissions ->
            callback.onPermanentlyDenied(permanentlyDeniedPermissions)
        }
        return this
    }
    
    /**
     * 设置Java友好的简化回调
     */
    fun onJavaResult(callback: com.cairong.permission.java.SimpleJavaPermissionCallback): PermissionRequestBuilder {
        this.onGrantedCallback = { grantedPermissions ->
            callback.onResult(true, grantedPermissions, emptyArray())
        }
        this.onDeniedCallback = { deniedPermissions, permanentlyDeniedPermissions ->
            val allDenied = deniedPermissions + permanentlyDeniedPermissions
            callback.onResult(false, emptyArray(), allDenied)
        }
        return this
    }
    
    /**
     * 设置自定义权限解释处理器
     */
    fun rationaleHandler(handler: RationaleHandler): PermissionRequestBuilder {
        this.rationaleHandler = handler
        return this
    }
    
    /**
     * 设置自定义设置页面处理器
     */
    fun settingsHandler(handler: SettingsHandler): PermissionRequestBuilder {
        this.settingsHandler = handler
        return this
    }
    
    /**
     * 执行权限请求
     */
    fun request() {
        if (permissions.isEmpty()) {
            throw IllegalStateException("No permissions specified")
        }
        
        val request = PermissionRequest(
            permissions = permissions.toTypedArray(),
            rationale = rationale,
            rationaleTitle = rationaleTitle,
            positiveButtonText = positiveButtonText,
            negativeButtonText = negativeButtonText,
            settingsText = settingsText,
            settingsTitle = settingsTitle,
            forceGoToSettings = forceGoToSettings,
            callback = createCallback()
        )
        
        val executor = if (activity != null) {
            PermissionRequestExecutor(activity)
        } else if (fragment != null) {
            PermissionRequestExecutor(fragment)
        } else {
            throw IllegalStateException("No activity or fragment provided")
        }
        
        // 设置自定义处理器
        rationaleHandler?.let { executor.setRationaleHandler(it) }
        settingsHandler?.let { executor.setSettingsHandler(it) }
        
        executor.execute(request)
    }
    
    /**
     * 创建权限回调
     */
    private fun createCallback(): PermissionCallback {
        return object : PermissionCallback {
            override fun onBeforeRequest(permissions: Array<String>) {
                onBeforeRequestCallback?.invoke(permissions)
            }
            
            override fun onGranted(permissions: Array<String>) {
                onGrantedCallback?.invoke(permissions)
            }
            
            override fun onDenied(
                deniedPermissions: Array<String>,
                permanentlyDeniedPermissions: Array<String>
            ) {
                onDeniedCallback?.invoke(deniedPermissions, permanentlyDeniedPermissions)
                
                // 如果有永久拒绝的权限，也调用永久拒绝回调
                if (permanentlyDeniedPermissions.isNotEmpty()) {
                    onPermanentlyDeniedCallback?.invoke(permanentlyDeniedPermissions)
                }
            }
            
            override fun onPermanentlyDenied(permanentlyDeniedPermissions: Array<String>) {
                onPermanentlyDeniedCallback?.invoke(permanentlyDeniedPermissions)
            }
        }
    }
}