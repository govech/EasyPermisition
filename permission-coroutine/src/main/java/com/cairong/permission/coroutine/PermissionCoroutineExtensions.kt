package com.cairong.permission.coroutine

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.cairong.permission.PermissionRequestBuilder
import com.cairong.permission.PermissionState
import com.cairong.permission.PermissionStateChecker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 协程扩展：等待权限请求结果
 * 
 * 使用示例：
 * ```kotlin
 * val granted = PermissionManager.with(this)
 *     .permission(Manifest.permission.CAMERA)
 *     .await()
 * 
 * if (granted) {
 *     openCamera()
 * }
 * ```
 * 
 * @return 是否所有权限都已授权
 */
suspend fun PermissionRequestBuilder.await(): Boolean = suspendCancellableCoroutine { continuation ->
    var resumed = false
    
    onResult { allGranted, _, _ ->
        if (!resumed) {
            resumed = true
            continuation.resume(allGranted)
        }
    }
    
    // 设置取消处理
    continuation.invokeOnCancellation {
        // 权限请求被取消时的清理逻辑
        // 注意：这里无法直接取消已经启动的权限请求
    }
    
    try {
        request()
    } catch (e: Exception) {
        if (!resumed) {
            resumed = true
            continuation.resume(false)
        }
    }
}

/**
 * 协程扩展：等待权限请求详细结果
 * 
 * @return 权限请求结果详情
 */
suspend fun PermissionRequestBuilder.awaitResult(): PermissionResult = suspendCancellableCoroutine { continuation ->
    var resumed = false
    
    onGranted { grantedPermissions ->
        if (!resumed) {
            resumed = true
            continuation.resume(
                PermissionResult(
                    allGranted = true,
                    grantedPermissions = grantedPermissions,
                    deniedPermissions = emptyArray(),
                    permanentlyDeniedPermissions = emptyArray()
                )
            )
        }
    }
    
    onDenied { deniedPermissions, permanentlyDeniedPermissions ->
        if (!resumed) {
            resumed = true
            continuation.resume(
                PermissionResult(
                    allGranted = false,
                    grantedPermissions = emptyArray(),
                    deniedPermissions = deniedPermissions,
                    permanentlyDeniedPermissions = permanentlyDeniedPermissions
                )
            )
        }
    }
    
    continuation.invokeOnCancellation {
        // 取消处理
    }
    
    try {
        request()
    } catch (e: Exception) {
        if (!resumed) {
            resumed = true
            continuation.resume(
                PermissionResult(
                    allGranted = false,
                    grantedPermissions = emptyArray(),
                    deniedPermissions = emptyArray(),
                    permanentlyDeniedPermissions = emptyArray(),
                    error = e
                )
            )
        }
    }
}

/**
 * 权限请求结果数据类
 */
data class PermissionResult(
    /**
     * 是否所有权限都已授权
     */
    val allGranted: Boolean,
    
    /**
     * 已授权的权限列表
     */
    val grantedPermissions: Array<String>,
    
    /**
     * 被拒绝的权限列表
     */
    val deniedPermissions: Array<String>,
    
    /**
     * 被永久拒绝的权限列表
     */
    val permanentlyDeniedPermissions: Array<String>,
    
    /**
     * 请求过程中的错误（如果有）
     */
    val error: Throwable? = null
) {
    
    /**
     * 是否有权限被拒绝
     */
    val hasDeniedPermissions: Boolean
        get() = deniedPermissions.isNotEmpty()
    
    /**
     * 是否有权限被永久拒绝
     */
    val hasPermanentlyDeniedPermissions: Boolean
        get() = permanentlyDeniedPermissions.isNotEmpty()
    
    /**
     * 是否部分权限已授权
     */
    val isPartiallyGranted: Boolean
        get() = grantedPermissions.isNotEmpty() && (deniedPermissions.isNotEmpty() || permanentlyDeniedPermissions.isNotEmpty())
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionResult

        if (allGranted != other.allGranted) return false
        if (!grantedPermissions.contentEquals(other.grantedPermissions)) return false
        if (!deniedPermissions.contentEquals(other.deniedPermissions)) return false
        if (!permanentlyDeniedPermissions.contentEquals(other.permanentlyDeniedPermissions)) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = allGranted.hashCode()
        result = 31 * result + grantedPermissions.contentHashCode()
        result = 31 * result + deniedPermissions.contentHashCode()
        result = 31 * result + permanentlyDeniedPermissions.contentHashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

/**
 * 观察权限状态变化的Flow
 * 
 * 使用示例：
 * ```kotlin
 * observePermissionState(this, Manifest.permission.CAMERA)
 *     .collect { state ->
 *         when (state) {
 *             PermissionState.GRANTED -> showCameraUI()
 *             PermissionState.DENIED -> showRationale()
 *             PermissionState.PERMANENTLY_DENIED -> showSettingsDialog()
 *         }
 *     }
 * ```
 * 
 * @param context 上下文
 * @param permission 要观察的权限
 * @return 权限状态Flow
 */
fun observePermissionState(context: Context, permission: String): Flow<PermissionState> = callbackFlow {
    // 发送初始状态
    val initialState = PermissionStateChecker.checkPermissionState(context, permission)
    trySend(initialState)
    
    // 注意：这里是一个简化实现
    // 实际应用中可能需要监听系统权限变化的广播
    // 或者定期检查权限状态
    
    awaitClose {
        // 清理资源
    }
}

/**
 * 观察权限状态变化的Flow（Fragment版本）
 * 
 * @param fragment Fragment实例
 * @param permission 要观察的权限
 * @return 权限状态Flow
 */
fun observePermissionState(fragment: Fragment, permission: String): Flow<PermissionState> = callbackFlow {
    // 发送初始状态
    val initialState = PermissionStateChecker.checkPermissionState(fragment, permission)
    trySend(initialState)
    
    awaitClose {
        // 清理资源
    }
}

/**
 * 观察多个权限状态变化的Flow
 * 
 * @param context 上下文
 * @param permissions 要观察的权限列表
 * @return 权限状态映射Flow
 */
fun observePermissionsState(context: Context, permissions: Array<String>): Flow<Map<String, PermissionState>> = callbackFlow {
    // 发送初始状态
    val initialStates = PermissionStateChecker.checkPermissionsState(context, permissions)
    trySend(initialStates)
    
    awaitClose {
        // 清理资源
    }
}

/**
 * 观察多个权限状态变化的Flow（Fragment版本）
 * 
 * @param fragment Fragment实例
 * @param permissions 要观察的权限列表
 * @return 权限状态映射Flow
 */
fun observePermissionsState(fragment: Fragment, permissions: Array<String>): Flow<Map<String, PermissionState>> = callbackFlow {
    // 发送初始状态
    val initialStates = PermissionStateChecker.checkPermissionsState(fragment, permissions)
    trySend(initialStates)
    
    awaitClose {
        // 清理资源
    }
}