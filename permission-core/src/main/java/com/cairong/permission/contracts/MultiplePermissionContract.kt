package com.cairong.permission.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * 多权限请求合约
 * 
 * 基于ActivityResultContract实现的多权限批量请求
 */
class MultiplePermissionContract : ActivityResultContract<Array<String>, Map<String, Boolean>>() {
    
    override fun createIntent(context: Context, input: Array<String>): Intent {
        // 使用系统的权限请求Intent
        return Intent().apply {
            action = "android.content.pm.action.REQUEST_PERMISSIONS"
            putExtra("android.content.pm.extra.REQUEST_PERMISSIONS_NAMES", input)
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
        // 这个方法实际上不会被调用，因为我们使用的是系统内置的RequestMultiplePermissions合约
        // 这里只是为了完整性而实现
        return emptyMap()
    }
    
    override fun getSynchronousResult(
        context: Context,
        input: Array<String>
    ): SynchronousResult<Map<String, Boolean>>? {
        // 检查所有权限的状态
        val results = mutableMapOf<String, Boolean>()
        var allGranted = true
        
        input.forEach { permission ->
            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            results[permission] = isGranted
            if (!isGranted) {
                allGranted = false
            }
        }
        
        // 如果所有权限都已授权，直接返回结果
        return if (allGranted) {
            SynchronousResult(results)
        } else {
            null
        }
    }
}