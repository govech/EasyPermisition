package com.cairong.permission

import android.Manifest
import android.os.Build

/**
 * 权限组和依赖关系管理
 * 
 * 定义相关权限的组合和依赖关系
 */
object PermissionGroups {
    
    /**
     * 位置权限组
     */
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    /**
     * 后台位置权限（Android 10+）
     */
    val BACKGROUND_LOCATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    } else {
        null
    }
    
    /**
     * 存储权限组（Android 12及以下）
     */
    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    /**
     * 媒体权限组（Android 13+）
     */
    val MEDIA_PERMISSIONS_ANDROID_13 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        emptyArray()
    }
    
    /**
     * 通知权限（Android 13+）
     */
    val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }
    
    /**
     * 相机和麦克风权限组
     */
    val CAMERA_AND_AUDIO_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    
    /**
     * 联系人权限组
     */
    val CONTACTS_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS
    )
    
    /**
     * 日历权限组
     */
    val CALENDAR_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )
    
    /**
     * 短信权限组
     */
    val SMS_PERMISSIONS = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.RECEIVE_MMS
    )
    
    /**
     * 电话权限组
     */
    val PHONE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ADD_VOICEMAIL,
        Manifest.permission.USE_SIP,
        Manifest.permission.PROCESS_OUTGOING_CALLS
    )
    
    /**
     * 获取权限所属的组
     */
    fun getPermissionGroup(permission: String): Array<String>? {
        return when (permission) {
            in LOCATION_PERMISSIONS -> LOCATION_PERMISSIONS
            in STORAGE_PERMISSIONS -> STORAGE_PERMISSIONS
            in MEDIA_PERMISSIONS_ANDROID_13 -> MEDIA_PERMISSIONS_ANDROID_13
            in CAMERA_AND_AUDIO_PERMISSIONS -> CAMERA_AND_AUDIO_PERMISSIONS
            in CONTACTS_PERMISSIONS -> CONTACTS_PERMISSIONS
            in CALENDAR_PERMISSIONS -> CALENDAR_PERMISSIONS
            in SMS_PERMISSIONS -> SMS_PERMISSIONS
            in PHONE_PERMISSIONS -> PHONE_PERMISSIONS
            else -> null
        }
    }
    
    /**
     * 检查权限是否属于同一组
     */
    fun areInSameGroup(permission1: String, permission2: String): Boolean {
        val group1 = getPermissionGroup(permission1)
        val group2 = getPermissionGroup(permission2)
        return group1 != null && group2 != null && group1.contentEquals(group2)
    }
}

/**
 * 权限依赖关系管理器
 */
class PermissionDependencyManager {
    
    /**
     * 权限依赖关系映射
     * key: 依赖权限，value: 被依赖的权限列表
     */
    private val dependencyMap = mutableMapOf<String, Array<String>>()
    
    init {
        setupDefaultDependencies()
    }
    
    /**
     * 设置默认的权限依赖关系
     */
    private fun setupDefaultDependencies() {
        // 后台位置权限依赖前台位置权限
        PermissionGroups.BACKGROUND_LOCATION_PERMISSION?.let { backgroundLocation ->
            dependencyMap[backgroundLocation] = PermissionGroups.LOCATION_PERMISSIONS
        }
        
        // 可以添加更多依赖关系
        // 例如：某些厂商的特殊权限依赖关系
    }
    
    /**
     * 添加权限依赖关系
     * 
     * @param dependentPermission 依赖权限
     * @param requiredPermissions 被依赖的权限列表
     */
    fun addDependency(dependentPermission: String, requiredPermissions: Array<String>) {
        dependencyMap[dependentPermission] = requiredPermissions
    }
    
    /**
     * 获取权限的依赖权限
     * 
     * @param permission 权限名称
     * @return 依赖的权限列表，如果没有依赖则返回null
     */
    fun getDependencies(permission: String): Array<String>? {
        return dependencyMap[permission]
    }
    
    /**
     * 检查权限依赖是否满足
     * 
     * @param permission 要检查的权限
     * @param grantedPermissions 已授权的权限列表
     * @return 是否满足依赖关系
     */
    fun areDependenciesSatisfied(permission: String, grantedPermissions: Array<String>): Boolean {
        val dependencies = getDependencies(permission) ?: return true
        return dependencies.all { dependency ->
            grantedPermissions.contains(dependency)
        }
    }
    
    /**
     * 获取未满足的依赖权限
     * 
     * @param permission 要检查的权限
     * @param grantedPermissions 已授权的权限列表
     * @return 未满足的依赖权限列表
     */
    fun getUnsatisfiedDependencies(permission: String, grantedPermissions: Array<String>): Array<String> {
        val dependencies = getDependencies(permission) ?: return emptyArray()
        return dependencies.filter { dependency ->
            !grantedPermissions.contains(dependency)
        }.toTypedArray()
    }
    
    /**
     * 解析权限请求顺序
     * 根据依赖关系确定权限请求的正确顺序
     * 
     * @param permissions 要请求的权限列表
     * @return 按依赖关系排序的权限列表
     */
    fun resolveRequestOrder(permissions: Array<String>): Array<String> {
        val result = mutableListOf<String>()
        val processed = mutableSetOf<String>()
        
        fun addPermissionWithDependencies(permission: String) {
            if (processed.contains(permission)) return
            
            // 先添加依赖权限
            val dependencies = getDependencies(permission)
            dependencies?.forEach { dependency ->
                if (permissions.contains(dependency)) {
                    addPermissionWithDependencies(dependency)
                }
            }
            
            // 再添加当前权限
            if (!result.contains(permission)) {
                result.add(permission)
            }
            processed.add(permission)
        }
        
        permissions.forEach { permission ->
            addPermissionWithDependencies(permission)
        }
        
        return result.toTypedArray()
    }
    
    /**
     * 验证权限组合的有效性
     * 
     * @param permissions 权限列表
     * @return 验证结果
     */
    fun validatePermissionCombination(permissions: Array<String>): PermissionValidationResult {
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        
        permissions.forEach { permission ->
            val dependencies = getDependencies(permission)
            dependencies?.forEach { dependency ->
                if (!permissions.contains(dependency)) {
                    issues.add("权限 $permission 依赖 $dependency，但未包含在请求列表中")
                    suggestions.add("建议同时请求 $dependency 权限")
                }
            }
        }
        
        return PermissionValidationResult(
            isValid = issues.isEmpty(),
            issues = issues.toTypedArray(),
            suggestions = suggestions.toTypedArray()
        )
    }
}

/**
 * 权限验证结果
 */
data class PermissionValidationResult(
    /**
     * 是否有效
     */
    val isValid: Boolean,
    
    /**
     * 问题列表
     */
    val issues: Array<String>,
    
    /**
     * 建议列表
     */
    val suggestions: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionValidationResult

        if (isValid != other.isValid) return false
        if (!issues.contentEquals(other.issues)) return false
        if (!suggestions.contentEquals(other.suggestions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isValid.hashCode()
        result = 31 * result + issues.contentHashCode()
        result = 31 * result + suggestions.contentHashCode()
        return result
    }
}