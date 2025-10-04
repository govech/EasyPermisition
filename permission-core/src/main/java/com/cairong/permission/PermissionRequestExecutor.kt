package com.cairong.permission

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.cairong.permission.config.PermissionConfig
import com.cairong.permission.interceptor.PermissionInterceptorRegistry
import com.cairong.permission.ratelimit.GlobalPermissionRateLimiter
import com.cairong.permission.exceptions.PermissionRateLimitException

/**
 * 权限请求执行器
 * 
 * 负责执行实际的权限请求逻辑
 */
class PermissionRequestExecutor {
    
    private val activity: ComponentActivity?
    private val fragment: Fragment?
    private val context: Context
    
    private var singlePermissionLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    private var appSettingsLauncher: AppSettingsLauncher? = null
    
    private var rationaleHandler: RationaleHandler = DefaultRationaleHandler()
    private var settingsHandler: SettingsHandler = DefaultSettingsHandler()
    
    private var currentRequest: PermissionRequest? = null
    
    constructor(activity: ComponentActivity) {
        this.activity = activity
        this.fragment = null
        this.context = activity
        initializeLaunchers()
    }
    
    constructor(fragment: Fragment) {
        this.activity = null
        this.fragment = fragment
        this.context = fragment.requireContext()
        initializeLaunchers()
    }
    
    /**
     * 初始化权限请求启动器
     */
    private fun initializeLaunchers() {
        if (activity != null) {
            initializeForActivity()
        } else if (fragment != null) {
            initializeForFragment()
        }
    }
    
    /**
     * 为Activity初始化启动器
     */
    private fun initializeForActivity() {
        val activity = this.activity!!
        
        // 单权限请求启动器
        singlePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleSinglePermissionResult(isGranted)
        }
        
        // 多权限请求启动器
        multiplePermissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handleMultiplePermissionsResult(permissions)
        }
        
        // 设置页面启动器
        appSettingsLauncher = AppSettingsLauncher().apply {
            initialize(activity) {
                handleSettingsResult()
            }
        }
    }
    
    /**
     * 为Fragment初始化启动器
     */
    private fun initializeForFragment() {
        val fragment = this.fragment!!
        
        // 单权限请求启动器
        singlePermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleSinglePermissionResult(isGranted)
        }
        
        // 多权限请求启动器
        multiplePermissionsLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handleMultiplePermissionsResult(permissions)
        }
        
        // 设置页面启动器
        appSettingsLauncher = AppSettingsLauncher().apply {
            initialize(fragment) {
                handleSettingsResult()
            }
        }
    }
    
    /**
     * 设置自定义权限解释处理器
     */
    fun setRationaleHandler(handler: RationaleHandler) {
        this.rationaleHandler = handler
    }
    
    /**
     * 设置自定义设置页面处理器
     */
    fun setSettingsHandler(handler: SettingsHandler) {
        this.settingsHandler = handler
    }
    
    /**
     * 执行权限请求
     */
    fun execute(request: PermissionRequest) {
        this.currentRequest = request
        
        try {
            // 执行拦截器前置检查
            if (!PermissionInterceptorRegistry.executeBeforeRequest(request)) {
                // 被拦截器拦截，不执行权限请求
                return
            }
            
            // 检查频率限制
            val blockedPermissions = request.permissions.filter { permission ->
                !GlobalPermissionRateLimiter.canRequest(permission)
            }
            
            if (blockedPermissions.isNotEmpty()) {
                // 有权限被频率限制阻止
                val exception = PermissionRateLimitException("权限请求被频率限制阻止: ${blockedPermissions.joinToString()}")
                PermissionInterceptorRegistry.executeOnError(request, exception)
                return
            }
            
            // 记录权限请求
            request.permissions.forEach { permission ->
                GlobalPermissionRateLimiter.recordRequest(permission)
            }
            
            // 调用请求前回调
            request.callback?.onBeforeRequest(request.permissions)
            
            // 检查权限状态
            val permissionStates = if (fragment != null) {
                PermissionStateChecker.checkPermissionsState(fragment, request.permissions)
            } else {
                PermissionStateChecker.checkPermissionsState(context, request.permissions)
            }
            
            val grantedPermissions = permissionStates.filter { it.value == PermissionState.GRANTED }.keys.toTypedArray()
            val deniedPermissions = permissionStates.filter { it.value == PermissionState.DENIED }.keys.toTypedArray()
            val permanentlyDeniedPermissions = permissionStates.filter { it.value == PermissionState.PERMANENTLY_DENIED }.keys.toTypedArray()
            
            // 如果所有权限都已授权，直接回调成功
            if (grantedPermissions.size == request.permissions.size) {
                request.callback?.onGranted(grantedPermissions)
                PermissionInterceptorRegistry.executeOnGranted(request, grantedPermissions)
                return
            }
            
            // 如果有永久拒绝的权限，处理永久拒绝逻辑
            if (permanentlyDeniedPermissions.isNotEmpty()) {
                handlePermanentlyDenied(permanentlyDeniedPermissions, deniedPermissions)
                return
            }
            
            // 如果有需要解释的权限，显示解释
            if (deniedPermissions.isNotEmpty() && request.rationale != null) {
                showRationale(request) {
                    // 用户选择继续请求权限
                    requestPermissions(deniedPermissions)
                }
            } else {
                // 直接请求权限
                requestPermissions(deniedPermissions)
            }
        } catch (e: Exception) {
            PermissionInterceptorRegistry.executeOnError(request, e)
        }
        
        // 检查权限状态
        val permissionStates = if (fragment != null) {
            PermissionStateChecker.checkPermissionsState(fragment, request.permissions)
        } else {
            PermissionStateChecker.checkPermissionsState(context, request.permissions)
        }
        
        val grantedPermissions = permissionStates.filter { it.value == PermissionState.GRANTED }.keys.toTypedArray()
        val deniedPermissions = permissionStates.filter { it.value == PermissionState.DENIED }.keys.toTypedArray()
        val permanentlyDeniedPermissions = permissionStates.filter { it.value == PermissionState.PERMANENTLY_DENIED }.keys.toTypedArray()
        
        // 如果所有权限都已授权，直接回调成功
        if (grantedPermissions.size == request.permissions.size) {
            request.callback?.onGranted(grantedPermissions)
            PermissionInterceptorRegistry.executeOnGranted(request, grantedPermissions)
            return
        }
        
        // 如果有永久拒绝的权限，处理永久拒绝逻辑
        if (permanentlyDeniedPermissions.isNotEmpty()) {
            handlePermanentlyDenied(permanentlyDeniedPermissions, deniedPermissions)
            return
        }
        
        // 如果有需要解释的权限，显示解释
        if (deniedPermissions.isNotEmpty() && request.rationale != null) {
            showRationale(request) {
                // 用户选择继续请求权限
                requestPermissions(deniedPermissions)
            }
        } else {
            // 直接请求权限
            requestPermissions(deniedPermissions)
        }
    }
    
    /**
     * 显示权限解释
     */
    private fun showRationale(request: PermissionRequest, onContinue: () -> Unit) {
        rationaleHandler.showRationale(context, request, object : RationaleCallback {
            override fun onContinue() {
                onContinue()
            }
            
            override fun onCancel() {
                val deniedPermissions = request.permissions.filter { permission ->
                    !PermissionStateChecker.areAllPermissionsGranted(context, arrayOf(permission))
                }.toTypedArray()
                
                request.callback?.onDenied(deniedPermissions, emptyArray())
            }
        })
    }
    
    /**
     * 请求权限
     */
    private fun requestPermissions(permissions: Array<String>) {
        if (permissions.isEmpty()) return
        
        if (permissions.size == 1) {
            singlePermissionLauncher?.launch(permissions[0])
        } else {
            multiplePermissionsLauncher?.launch(permissions)
        }
    }
    
    /**
     * 处理单权限请求结果
     */
    private fun handleSinglePermissionResult(isGranted: Boolean) {
        val request = currentRequest ?: return
        val permission = request.singlePermission
        
        if (isGranted) {
            request.callback?.onGranted(arrayOf(permission))
        } else {
            // 检查是否被永久拒绝
            val state = if (fragment != null) {
                PermissionStateChecker.checkPermissionState(fragment, permission)
            } else {
                PermissionStateChecker.checkPermissionState(context, permission)
            }
            
            if (state == PermissionState.PERMANENTLY_DENIED) {
                handlePermanentlyDenied(arrayOf(permission), emptyArray())
            } else {
                request.callback?.onDenied(arrayOf(permission), emptyArray())
            }
        }
    }
    
    /**
     * 处理多权限请求结果
     */
    private fun handleMultiplePermissionsResult(permissions: Map<String, Boolean>) {
        val request = currentRequest ?: return
        
        // 使用结果聚合器处理多权限结果
        val aggregator = PermissionResultAggregator()
        val aggregatedResult = if (fragment != null) {
            aggregator.aggregateResults(fragment, request.permissions, permissions)
        } else {
            aggregator.aggregateResults(context, request.permissions, permissions)
        }
        
        if (aggregatedResult.allGranted) {
            // 所有权限都已授权
            request.callback?.onGranted(aggregatedResult.grantedPermissions)
        } else {
            // 有权限被拒绝
            if (aggregatedResult.hasPermanentlyDeniedPermissions) {
                handlePermanentlyDenied(
                    aggregatedResult.permanentlyDeniedPermissions,
                    aggregatedResult.temporarilyDeniedPermissions
                )
            } else {
                request.callback?.onDenied(
                    aggregatedResult.temporarilyDeniedPermissions,
                    emptyArray()
                )
            }
        }
    }
    
    /**
     * 处理永久拒绝的权限
     */
    private fun handlePermanentlyDenied(
        permanentlyDeniedPermissions: Array<String>,
        temporarilyDeniedPermissions: Array<String>
    ) {
        val request = currentRequest ?: return
        
        // 调用永久拒绝回调
        request.callback?.onPermanentlyDenied(permanentlyDeniedPermissions)
        
        // 如果强制跳转设置或有设置文案，显示设置对话框
        if (request.forceGoToSettings || request.settingsText != null) {
            showSettingsDialog(request)
        } else {
            // 否则调用拒绝回调
            request.callback?.onDenied(temporarilyDeniedPermissions, permanentlyDeniedPermissions)
        }
    }
    
    /**
     * 显示设置对话框
     */
    private fun showSettingsDialog(request: PermissionRequest) {
        settingsHandler.showSettingsDialog(context, request, object : SettingsCallback {
            override fun onGoToSettings() {
                appSettingsLauncher?.launch(context)
            }
            
            override fun onCancel() {
                // 用户取消跳转设置
            }
        })
    }
    
    /**
     * 处理从设置页面返回的结果
     */
    private fun handleSettingsResult() {
        val request = currentRequest ?: return
        
        // 重新检查权限状态
        val grantedPermissions = PermissionStateChecker.getGrantedPermissions(context, request.permissions)
        
        if (grantedPermissions.size == request.permissions.size) {
            // 所有权限都已授权
            request.callback?.onGranted(grantedPermissions)
        } else {
            // 仍有权限未授权
            val deniedPermissions = PermissionStateChecker.getDeniedPermissions(context, request.permissions)
            val permanentlyDeniedPermissions = if (fragment != null) {
                PermissionStateChecker.getPermanentlyDeniedPermissions(fragment, deniedPermissions)
            } else {
                PermissionStateChecker.getPermanentlyDeniedPermissions(context, deniedPermissions)
            }
            
            request.callback?.onDenied(deniedPermissions, permanentlyDeniedPermissions)
        }
    }
}