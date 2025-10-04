package com.cairong.permission.config

import com.cairong.permission.DefaultRationaleHandler
import com.cairong.permission.DefaultSettingsHandler
import com.cairong.permission.RationaleHandler
import com.cairong.permission.SettingsHandler

/**
 * 权限框架全局配置
 * 
 * 提供全局默认设置和配置选项
 */
object PermissionConfig {
    
    /**
     * 默认的权限解释处理器
     */
    var defaultRationaleHandler: RationaleHandler = DefaultRationaleHandler()
    
    /**
     * 默认的设置页面处理器
     */
    var defaultSettingsHandler: SettingsHandler = DefaultSettingsHandler()
    
    /**
     * 是否启用日志输出
     */
    var enableLogging: Boolean = false
    
    /**
     * 权限请求超时时间（毫秒）
     */
    var requestTimeout: Long = 30_000L
    
    /**
     * 默认的权限解释标题
     */
    var defaultRationaleTitle: String = "权限说明"
    
    /**
     * 默认的权限解释消息
     */
    var defaultRationaleMessage: String = "应用需要相关权限才能正常工作"
    
    /**
     * 默认的确认按钮文案
     */
    var defaultPositiveButtonText: String = "确定"
    
    /**
     * 默认的取消按钮文案
     */
    var defaultNegativeButtonText: String = "取消"
    
    /**
     * 默认的设置页面标题
     */
    var defaultSettingsTitle: String = "权限设置"
    
    /**
     * 默认的设置页面消息
     */
    var defaultSettingsMessage: String = "权限被永久拒绝，请到设置页面手动开启"
    
    /**
     * 默认的跳转设置按钮文案
     */
    var defaultGoToSettingsText: String = "去设置"
    
    /**
     * 是否在永久拒绝后强制跳转设置页面
     */
    var forceGoToSettingsOnPermanentDenial: Boolean = false
    
    /**
     * 默认主题资源ID
     */
    var defaultThemeResId: Int = 0
    
    /**
     * 权限请求频率限制间隔（毫秒）
     */
    var rateLimitInterval: Long = 5_000L
    
    /**
     * 每小时最大权限请求次数
     */
    var maxRequestsPerHour: Int = 10
    
    /**
     * 重置所有配置为默认值
     */
    fun resetToDefaults() {
        defaultRationaleHandler = DefaultRationaleHandler()
        defaultSettingsHandler = DefaultSettingsHandler()
        enableLogging = false
        requestTimeout = 30_000L
        defaultRationaleTitle = "权限说明"
        defaultRationaleMessage = "应用需要相关权限才能正常工作"
        defaultPositiveButtonText = "确定"
        defaultNegativeButtonText = "取消"
        defaultSettingsTitle = "权限设置"
        defaultSettingsMessage = "权限被永久拒绝，请到设置页面手动开启"
        defaultGoToSettingsText = "去设置"
        forceGoToSettingsOnPermanentDenial = false
        defaultThemeResId = 0
        rateLimitInterval = 5_000L
        maxRequestsPerHour = 10
    }
}

/**
 * 权限框架国际化字符串配置
 * 
 * 支持多语言文案配置
 */
object PermissionStrings {
    
    /**
     * 权限解释相关文案
     */
    object Rationale {
        var title: String = "权限说明"
        var cameraMessage: String = "需要相机权限来拍照和录制视频"
        var locationMessage: String = "需要位置权限来获取您的当前位置"
        var storageMessage: String = "需要存储权限来保存和读取文件"
        var audioMessage: String = "需要麦克风权限来录制音频"
        var contactsMessage: String = "需要联系人权限来访问您的联系人信息"
        var calendarMessage: String = "需要日历权限来管理您的日程安排"
        var smsMessage: String = "需要短信权限来发送和接收短信"
        var phoneMessage: String = "需要电话权限来拨打电话和查看通话记录"
    }
    
    /**
     * 设置页面相关文案
     */
    object Settings {
        var title: String = "权限设置"
        var message: String = "权限被永久拒绝，请到设置页面手动开启"
        var goToSettingsText: String = "去设置"
        var cancelText: String = "取消"
    }
    
    /**
     * 按钮文案
     */
    object Buttons {
        var allow: String = "允许"
        var deny: String = "拒绝"
        var ok: String = "确定"
        var cancel: String = "取消"
        var continueText: String = "继续"
        var settings: String = "设置"
    }
    
    /**
     * 错误消息
     */
    object Errors {
        var permissionDenied: String = "权限被拒绝"
        var permissionPermanentlyDenied: String = "权限被永久拒绝"
        var requestTimeout: String = "权限请求超时"
        var requestCancelled: String = "权限请求被取消"
        var unknownError: String = "未知错误"
    }
    
    /**
     * 根据权限名称获取默认解释消息
     */
    fun getDefaultRationaleMessage(permission: String): String {
        return when {
            permission.contains("CAMERA") -> Rationale.cameraMessage
            permission.contains("LOCATION") -> Rationale.locationMessage
            permission.contains("STORAGE") || permission.contains("MEDIA") -> Rationale.storageMessage
            permission.contains("RECORD_AUDIO") -> Rationale.audioMessage
            permission.contains("CONTACTS") -> Rationale.contactsMessage
            permission.contains("CALENDAR") -> Rationale.calendarMessage
            permission.contains("SMS") -> Rationale.smsMessage
            permission.contains("PHONE") -> Rationale.phoneMessage
            else -> PermissionConfig.defaultRationaleMessage
        }
    }
    
    /**
     * 设置语言
     */
    fun setLanguage(language: String) {
        when (language.lowercase()) {
            "en", "english" -> setEnglishStrings()
            "zh", "chinese" -> setChineseStrings()
            else -> setChineseStrings() // 默认中文
        }
    }
    
    /**
     * 设置英文文案
     */
    private fun setEnglishStrings() {
        Rationale.title = "Permission Required"
        Rationale.cameraMessage = "Camera permission is required to take photos and record videos"
        Rationale.locationMessage = "Location permission is required to get your current location"
        Rationale.storageMessage = "Storage permission is required to save and read files"
        Rationale.audioMessage = "Microphone permission is required to record audio"
        Rationale.contactsMessage = "Contacts permission is required to access your contact information"
        Rationale.calendarMessage = "Calendar permission is required to manage your schedule"
        Rationale.smsMessage = "SMS permission is required to send and receive text messages"
        Rationale.phoneMessage = "Phone permission is required to make calls and view call logs"
        
        Settings.title = "Permission Settings"
        Settings.message = "Permission has been permanently denied, please enable it in settings"
        Settings.goToSettingsText = "Go to Settings"
        Settings.cancelText = "Cancel"
        
        Buttons.allow = "Allow"
        Buttons.deny = "Deny"
        Buttons.ok = "OK"
        Buttons.cancel = "Cancel"
        Buttons.continueText = "Continue"
        Buttons.settings = "Settings"
        
        Errors.permissionDenied = "Permission denied"
        Errors.permissionPermanentlyDenied = "Permission permanently denied"
        Errors.requestTimeout = "Permission request timeout"
        Errors.requestCancelled = "Permission request cancelled"
        Errors.unknownError = "Unknown error"
        
        PermissionConfig.defaultRationaleTitle = Rationale.title
        PermissionConfig.defaultRationaleMessage = "The app needs relevant permissions to work properly"
        PermissionConfig.defaultPositiveButtonText = Buttons.ok
        PermissionConfig.defaultNegativeButtonText = Buttons.cancel
        PermissionConfig.defaultSettingsTitle = Settings.title
        PermissionConfig.defaultSettingsMessage = Settings.message
        PermissionConfig.defaultGoToSettingsText = Settings.goToSettingsText
    }
    
    /**
     * 设置中文文案
     */
    private fun setChineseStrings() {
        Rationale.title = "权限说明"
        Rationale.cameraMessage = "需要相机权限来拍照和录制视频"
        Rationale.locationMessage = "需要位置权限来获取您的当前位置"
        Rationale.storageMessage = "需要存储权限来保存和读取文件"
        Rationale.audioMessage = "需要麦克风权限来录制音频"
        Rationale.contactsMessage = "需要联系人权限来访问您的联系人信息"
        Rationale.calendarMessage = "需要日历权限来管理您的日程安排"
        Rationale.smsMessage = "需要短信权限来发送和接收短信"
        Rationale.phoneMessage = "需要电话权限来拨打电话和查看通话记录"
        
        Settings.title = "权限设置"
        Settings.message = "权限被永久拒绝，请到设置页面手动开启"
        Settings.goToSettingsText = "去设置"
        Settings.cancelText = "取消"
        
        Buttons.allow = "允许"
        Buttons.deny = "拒绝"
        Buttons.ok = "确定"
        Buttons.cancel = "取消"
        Buttons.continueText = "继续"
        Buttons.settings = "设置"
        
        Errors.permissionDenied = "权限被拒绝"
        Errors.permissionPermanentlyDenied = "权限被永久拒绝"
        Errors.requestTimeout = "权限请求超时"
        Errors.requestCancelled = "权限请求被取消"
        Errors.unknownError = "未知错误"
        
        PermissionConfig.defaultRationaleTitle = Rationale.title
        PermissionConfig.defaultRationaleMessage = "应用需要相关权限才能正常工作"
        PermissionConfig.defaultPositiveButtonText = Buttons.ok
        PermissionConfig.defaultNegativeButtonText = Buttons.cancel
        PermissionConfig.defaultSettingsTitle = Settings.title
        PermissionConfig.defaultSettingsMessage = Settings.message
        PermissionConfig.defaultGoToSettingsText = Settings.goToSettingsText
    }
}