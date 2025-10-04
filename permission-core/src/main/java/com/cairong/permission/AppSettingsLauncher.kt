package com.cairong.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * 应用设置页面启动器
 * 
 * 用于跳转到应用的权限设置页面
 */
class AppSettingsLauncher {
    
    private var settingsLauncher: ActivityResultLauncher<Intent>? = null
    private var callback: (() -> Unit)? = null
    
    /**
     * 在Activity中初始化
     */
    fun initialize(activity: ComponentActivity, callback: (() -> Unit)? = null) {
        this.callback = callback
        settingsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // 从设置页面返回后的回调
            this.callback?.invoke()
        }
    }
    
    /**
     * 在Fragment中初始化
     */
    fun initialize(fragment: Fragment, callback: (() -> Unit)? = null) {
        this.callback = callback
        settingsLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // 从设置页面返回后的回调
            this.callback?.invoke()
        }
    }
    
    /**
     * 启动应用设置页面
     */
    fun launch(context: Context) {
        val intent = createAppSettingsIntent(context)
        settingsLauncher?.launch(intent)
    }
    
    /**
     * 创建跳转到应用设置页面的Intent
     */
    private fun createAppSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    companion object {
        /**
         * 直接跳转到应用设置页面（不监听返回结果）
         */
        @JvmStatic
        fun openAppSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
        
        /**
         * 检查是否可以跳转到设置页面
         */
        @JvmStatic
        fun canOpenSettings(context: Context): Boolean {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            return intent.resolveActivity(context.packageManager) != null
        }
    }
}