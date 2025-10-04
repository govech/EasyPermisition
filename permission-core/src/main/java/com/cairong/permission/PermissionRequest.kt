package com.cairong.permission

/**
 * 权限请求实体类
 * 
 * 封装权限请求的所有参数和配置
 */
data class PermissionRequest(
    /**
     * 要请求的权限列表
     */
    val permissions: Array<String>,
    
    /**
     * 权限解释文案
     * 当用户首次拒绝权限时显示
     */
    val rationale: String? = null,
    
    /**
     * 权限解释标题
     */
    val rationaleTitle: String? = null,
    
    /**
     * 确认按钮文案
     */
    val positiveButtonText: String = "确定",
    
    /**
     * 取消按钮文案
     */
    val negativeButtonText: String = "取消",
    
    /**
     * 跳转设置页面的提示文案
     */
    val settingsText: String? = null,
    
    /**
     * 跳转设置页面的标题
     */
    val settingsTitle: String? = null,
    
    /**
     * 是否在永久拒绝后强制跳转设置页面
     */
    val forceGoToSettings: Boolean = false,
    
    /**
     * 权限回调接口
     */
    val callback: PermissionCallback? = null
) {
    
    /**
     * 是否为单权限请求
     */
    val isSinglePermission: Boolean
        get() = permissions.size == 1
    
    /**
     * 获取单个权限（仅在单权限请求时使用）
     */
    val singlePermission: String
        get() = if (isSinglePermission) permissions[0] else throw IllegalStateException("Not a single permission request")
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionRequest

        if (!permissions.contentEquals(other.permissions)) return false
        if (rationale != other.rationale) return false
        if (rationaleTitle != other.rationaleTitle) return false
        if (positiveButtonText != other.positiveButtonText) return false
        if (negativeButtonText != other.negativeButtonText) return false
        if (settingsText != other.settingsText) return false
        if (settingsTitle != other.settingsTitle) return false
        if (forceGoToSettings != other.forceGoToSettings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = permissions.contentHashCode()
        result = 31 * result + (rationale?.hashCode() ?: 0)
        result = 31 * result + (rationaleTitle?.hashCode() ?: 0)
        result = 31 * result + positiveButtonText.hashCode()
        result = 31 * result + negativeButtonText.hashCode()
        result = 31 * result + (settingsText?.hashCode() ?: 0)
        result = 31 * result + (settingsTitle?.hashCode() ?: 0)
        result = 31 * result + forceGoToSettings.hashCode()
        return result
    }
}