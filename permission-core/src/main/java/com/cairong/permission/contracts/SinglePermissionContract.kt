package com.cairong.permission.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * 单权限请求合约
 * 
 * 基于ActivityResultContract实现的单权限请求
 */
class SinglePermissionContract : ActivityResultContract<String, Boolean>() {
    
    override fun createIntent(context: Context, input: String): Intent {
        // 使用系统的权限请求Intent
        return Intent().apply {
            action = "android.content.pm.action.REQUEST_PERMISSIONS"
            putExtra("android.content.pm.extra.REQUEST_PERMISSIONS_NAMES", arrayOf(input))
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        // 这个方法实际上不会被调用，因为我们使用的是系统内置的RequestPermission合约
        // 这里只是为了完整性而实现
        return false
    }
    
    override fun getSynchronousResult(
        context: Context,
        input: String
    ): SynchronousResult<Boolean>? {
        // 如果权限已经被授权，直接返回true
        return if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                input
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            SynchronousResult(true)
        } else {
            null
        }
    }
}