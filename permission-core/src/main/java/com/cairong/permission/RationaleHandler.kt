package com.cairong.permission

import android.app.AlertDialog
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

/**
 * 权限解释处理接口
 * 
 * 用于处理权限被拒绝后的解释逻辑
 */
interface RationaleHandler {
    
    /**
     * 显示权限解释
     * 
     * @param context 上下文
     * @param request 权限请求信息
     * @param callback 用户选择回调
     */
    fun showRationale(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    )
}

/**
 * 权限解释回调接口
 */
interface RationaleCallback {
    /**
     * 用户选择继续请求权限
     */
    fun onContinue()
    
    /**
     * 用户选择取消权限请求
     */
    fun onCancel()
}

/**
 * 默认的权限解释处理器
 * 使用系统AlertDialog显示权限解释
 */
class DefaultRationaleHandler(
    private val themeResId: Int = 0
) : RationaleHandler {
    
    override fun showRationale(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    ) {
        val title = request.rationaleTitle ?: "权限说明"
        val message = request.rationale ?: "应用需要相关权限才能正常工作"
        
        val builder = if (themeResId != 0) {
            AlertDialog.Builder(context, themeResId)
        } else {
            AlertDialog.Builder(context)
        }
        
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(request.positiveButtonText) { dialog, _ ->
                dialog.dismiss()
                callback.onContinue()
            }
            .setNegativeButton(request.negativeButtonText) { dialog, _ ->
                dialog.dismiss()
                callback.onCancel()
            }
            .setCancelable(false)
            .show()
    }
}

/**
 * 设置页面跳转处理接口
 */
interface SettingsHandler {
    
    /**
     * 显示跳转设置页面的提示
     * 
     * @param context 上下文
     * @param request 权限请求信息
     * @param callback 用户选择回调
     */
    fun showSettingsDialog(
        context: Context,
        request: PermissionRequest,
        callback: SettingsCallback
    )
}

/**
 * 设置页面跳转回调接口
 */
interface SettingsCallback {
    /**
     * 用户选择跳转到设置页面
     */
    fun onGoToSettings()
    
    /**
     * 用户选择取消
     */
    fun onCancel()
}

/**
 * 默认的设置页面处理器
 */
class DefaultSettingsHandler(
    private val themeResId: Int = 0
) : SettingsHandler {
    
    override fun showSettingsDialog(
        context: Context,
        request: PermissionRequest,
        callback: SettingsCallback
    ) {
        val title = request.settingsTitle ?: "权限设置"
        val message = request.settingsText ?: "权限被永久拒绝，请到设置页面手动开启"
        
        val builder = if (themeResId != 0) {
            AlertDialog.Builder(context, themeResId)
        } else {
            AlertDialog.Builder(context)
        }
        
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("去设置") { dialog, _ ->
                dialog.dismiss()
                callback.onGoToSettings()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                callback.onCancel()
            }
            .setCancelable(false)
            .show()
    }
}