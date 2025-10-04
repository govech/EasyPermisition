package com.cairong.permission.demo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cairong.permission.PermissionManager
import com.cairong.permission.demo.databinding.ActivityAndroid14DemoBinding

/**
 * Android 14+ 权限适配演示
 * 
 * 展示 Android 14 引入的新权限和变更
 */
class Android14DemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAndroid14DemoBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAndroid14DemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        setupClickListeners()
        updateVersionInfo()
    }
    
    private fun setupClickListeners() {
        // 部分媒体权限演示 (Android 14+)
        binding.btnRequestPartialMedia.setOnClickListener {
            requestPartialMediaPermissions()
        }
        
        // 通知权限演示 (Android 13+)
        binding.btnRequestNotification.setOnClickListener {
            requestNotificationPermission()
        }
        
        // 前台服务权限演示 (Android 14+)
        binding.btnRequestForegroundService.setOnClickListener {
            requestForegroundServicePermissions()
        }
        
        // 精确闹钟权限演示 (Android 12+)
        binding.btnRequestExactAlarm.setOnClickListener {
            requestExactAlarmPermission()
        }
    }
    
    private fun updateVersionInfo() {
        val versionInfo = "当前 Android 版本：${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        binding.tvVersionInfo.text = versionInfo
        
        // 根据版本显示可用功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14
            binding.tvPartialMediaNote.text = "✅ 支持部分媒体权限"
            binding.tvForegroundServiceNote.text = "✅ 支持前台服务权限"
        } else {
            binding.tvPartialMediaNote.text = "❌ 需要 Android 14+"
            binding.tvForegroundServiceNote.text = "❌ 需要 Android 14+"
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            binding.tvNotificationNote.text = "✅ 支持通知权限"
        } else {
            binding.tvNotificationNote.text = "❌ 需要 Android 13+"
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12
            binding.tvExactAlarmNote.text = "✅ 支持精确闹钟权限"
        } else {
            binding.tvExactAlarmNote.text = "❌ 需要 Android 12+"
        }
    }
    
    private fun requestPartialMediaPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            showToast("部分媒体权限需要 Android 14+")
            return
        }
        
        // Android 14+ 支持部分媒体权限
        val partialMediaPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            listOf(
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED" // Android 14 新增
            )
        } else {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        }
        
        PermissionManager.with(this)
            .permissions(*partialMediaPermissions.toTypedArray())
            .rationale("Android 14+ 支持用户选择部分媒体文件的权限")
            .onBeforeRequest { 
                binding.tvPartialMediaStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("部分媒体权限已授权")
                binding.tvPartialMediaStatus.text = "状态：已授权"
            }
            .onDenied { _, _ ->
                showToast("部分媒体权限被拒绝")
                binding.tvPartialMediaStatus.text = "状态：被拒绝"
            }
            .onPermanentlyDenied { _ ->
                showToast("部分媒体权限被永久拒绝")
                binding.tvPartialMediaStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showToast("通知权限需要 Android 13+")
            return
        }
        
        PermissionManager.with(this)
            .permissions(Manifest.permission.POST_NOTIFICATIONS)
            .rationale("需要通知权限来发送应用通知")
            .onBeforeRequest { 
                binding.tvNotificationStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("通知权限已授权")
                binding.tvNotificationStatus.text = "状态：已授权"
            }
            .onDenied { _, _ ->
                showToast("通知权限被拒绝")
                binding.tvNotificationStatus.text = "状态：被拒绝"
            }
            .onPermanentlyDenied { _ ->
                showToast("通知权限被永久拒绝")
                binding.tvNotificationStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun requestForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            showToast("前台服务权限需要 Android 14+")
            return
        }
        
        // Android 14+ 前台服务需要特定类型权限
        val foregroundServicePermissions = listOf(
            "android.permission.FOREGROUND_SERVICE_LOCATION",
            "android.permission.FOREGROUND_SERVICE_CAMERA",
            "android.permission.FOREGROUND_SERVICE_MICROPHONE"
        )
        
        PermissionManager.with(this)
            .permissions(*foregroundServicePermissions.toTypedArray())
            .rationale("Android 14+ 前台服务需要特定类型权限")
            .onBeforeRequest { 
                binding.tvForegroundServiceStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("前台服务权限已授权")
                binding.tvForegroundServiceStatus.text = "状态：已授权"
            }
            .onDenied { _, _ ->
                showToast("前台服务权限被拒绝")
                binding.tvForegroundServiceStatus.text = "状态：被拒绝"
            }
            .onPermanentlyDenied { _ ->
                showToast("前台服务权限被永久拒绝")
                binding.tvForegroundServiceStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            showToast("精确闹钟权限需要 Android 12+")
            return
        }
        
        // 注意：SCHEDULE_EXACT_ALARM 是特殊权限，不能通过常规方式请求
        // 这里仅作演示，实际需要引导用户到设置页面
        showToast("精确闹钟权限需要用户在设置中手动开启")
        binding.tvExactAlarmStatus.text = "状态：需要手动设置"
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}