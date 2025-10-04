package com.cairong.permission.demo

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cairong.permission.PermissionManager
import com.cairong.permission.config.PermissionConfig
import com.cairong.permission.config.PermissionStrings
import com.cairong.permission.demo.databinding.ActivityCustomUiDemoBinding
import com.cairong.permission.ui.CustomRationaleHandler

/**
 * 自定义UI演示Activity
 * 
 * 演示如何使用自定义主题和UI
 */
class CustomUIDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCustomUiDemoBinding
    
    companion object {
        private const val TAG = "CustomUIDemoActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomUiDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        setupClickListeners()
        setupGlobalConfig()
    }
    
    private fun setupClickListeners() {
        binding.btnCustomTheme.setOnClickListener {
            requestWithCustomTheme()
        }
        
        binding.btnCustomRationale.setOnClickListener {
            requestWithCustomRationale()
        }
        
        binding.btnGlobalConfig.setOnClickListener {
            requestWithGlobalConfig()
        }
        
        binding.btnMultiLanguage.setOnClickListener {
            requestWithMultiLanguage()
        }
        
        binding.btnResetConfig.setOnClickListener {
            resetToDefaults()
        }
    }
    
    /**
     * 设置全局配置
     */
    private fun setupGlobalConfig() {
        // 启用日志
        PermissionConfig.enableLogging = true
        
        // 设置默认文案
        PermissionConfig.defaultRationaleTitle = "权限申请"
        PermissionConfig.defaultPositiveButtonText = "同意"
        PermissionConfig.defaultNegativeButtonText = "拒绝"
        
        binding.tvStatus.text = "全局配置已设置\n- 启用日志\n- 自定义按钮文案"
    }
    
    /**
     * 使用自定义主题请求权限
     */
    private fun requestWithCustomTheme() {
        Log.d(TAG, "使用自定义主题请求权限")
        binding.tvStatus.text = "正在使用自定义主题请求相机权限..."
        
        // 使用自定义主题的处理器
        val customHandler = CustomRationaleHandler(
            themeResId = android.R.style.Theme_Material_Dialog_Alert
        )
        
        PermissionManager.with(this)
            .permission(Manifest.permission.CAMERA)
            .rationale("这是使用自定义主题的权限解释对话框")
            .rationaleTitle("自定义主题演示")
            .rationaleHandler(customHandler)
            .onGranted { permissions ->
                Log.d(TAG, "自定义主题权限已授权: ${permissions.joinToString()}")
                binding.tvStatus.text = "自定义主题权限已授权！\n使用了Material主题样式"
                Toast.makeText(this, "自定义主题权限已授权", Toast.LENGTH_SHORT).show()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "自定义主题权限被拒绝")
                binding.tvStatus.text = "自定义主题权限被拒绝\n对话框使用了自定义主题"
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            .request()
    }
    
    /**
     * 使用自定义权限解释请求权限
     */
    private fun requestWithCustomRationale() {
        Log.d(TAG, "使用自定义权限解释")
        binding.tvStatus.text = "正在使用自定义权限解释请求麦克风权限..."
        
        // 使用自定义视图提供器
        val customHandler = CustomRationaleHandler(
            customViewProvider = { context, request ->
                // 创建自定义视图
                val linearLayout = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(48, 48, 48, 48)
                }
                
                // 添加图标
                val iconView = android.widget.ImageView(context).apply {
                    setImageResource(android.R.drawable.ic_btn_speak_now)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER_HORIZONTAL
                        bottomMargin = 32
                    }
                }
                linearLayout.addView(iconView)
                
                // 添加标题
                val titleView = android.widget.TextView(context).apply {
                    text = "🎤 麦克风权限"
                    textSize = 20f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 0, 0, 24)
                }
                linearLayout.addView(titleView)
                
                // 添加消息
                val messageView = android.widget.TextView(context).apply {
                    text = "我们需要麦克风权限来录制您的语音。\n\n这是一个完全自定义的权限解释界面，您可以添加任何UI元素。"
                    textSize = 14f
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 0, 0, 32)
                }
                linearLayout.addView(messageView)
                
                // 添加按钮容器
                val buttonContainer = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                }
                
                // 拒绝按钮
                val denyButton = android.widget.Button(context).apply {
                    id = android.R.id.button2
                    text = "暂不需要"
                    setBackgroundColor(android.graphics.Color.GRAY)
                    setTextColor(android.graphics.Color.WHITE)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    ).apply {
                        setMargins(0, 0, 16, 0)
                    }
                }
                buttonContainer.addView(denyButton)
                
                // 同意按钮
                val allowButton = android.widget.Button(context).apply {
                    id = android.R.id.button1
                    text = "立即授权"
                    setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                    setTextColor(android.graphics.Color.WHITE)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }
                buttonContainer.addView(allowButton)
                
                linearLayout.addView(buttonContainer)
                
                linearLayout
            }
        )
        
        PermissionManager.with(this)
            .permission(Manifest.permission.RECORD_AUDIO)
            .rationaleHandler(customHandler)
            .onGranted { permissions ->
                Log.d(TAG, "自定义UI权限已授权: ${permissions.joinToString()}")
                binding.tvStatus.text = "自定义UI权限已授权！\n使用了完全自定义的权限解释界面"
                Toast.makeText(this, "麦克风权限已授权", Toast.LENGTH_SHORT).show()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "自定义UI权限被拒绝")
                binding.tvStatus.text = "自定义UI权限被拒绝\n展示了自定义的权限解释界面"
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            .request()
    }
    
    /**
     * 使用全局配置请求权限
     */
    private fun requestWithGlobalConfig() {
        Log.d(TAG, "使用全局配置请求权限")
        binding.tvStatus.text = "正在使用全局配置请求存储权限..."
        
        PermissionManager.with(this)
            .storagePermissions() // 使用权限组
            .rationale("这个请求使用了全局配置的默认文案和按钮")
            // 不设置自定义文案，使用全局配置
            .onGranted { permissions ->
                Log.d(TAG, "全局配置权限已授权: ${permissions.joinToString()}")
                binding.tvStatus.text = "全局配置权限已授权！\n使用了全局配置的按钮文案"
                Toast.makeText(this, "存储权限已授权", Toast.LENGTH_SHORT).show()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "全局配置权限被拒绝")
                binding.tvStatus.text = "全局配置权限被拒绝\n对话框使用了全局配置"
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            .request()
    }
    
    /**
     * 使用多语言支持请求权限
     */
    private fun requestWithMultiLanguage() {
        Log.d(TAG, "使用多语言支持请求权限")
        
        // 切换到英文
        PermissionStrings.setLanguage("en")
        binding.tvStatus.text = "Switched to English and requesting location permission..."
        
        PermissionManager.with(this)
            .locationPermissions()
            .rationale(PermissionStrings.getDefaultRationaleMessage(Manifest.permission.ACCESS_FINE_LOCATION))
            .rationaleTitle(PermissionStrings.Rationale.title)
            .positiveButtonText(PermissionStrings.Buttons.allow)
            .negativeButtonText(PermissionStrings.Buttons.deny)
            .onGranted { permissions ->
                Log.d(TAG, "多语言权限已授权: ${permissions.joinToString()}")
                binding.tvStatus.text = "Location permission granted!\nUsed English language strings"
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "多语言权限被拒绝")
                binding.tvStatus.text = "Location permission denied\nUsed English language strings"
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            .request()
    }
    
    /**
     * 重置为默认配置
     */
    private fun resetToDefaults() {
        PermissionConfig.resetToDefaults()
        PermissionStrings.setLanguage("zh")
        binding.tvStatus.text = "配置已重置为默认值\n- 中文界面\n- 默认按钮文案\n- 关闭日志"
        Toast.makeText(this, "配置已重置", Toast.LENGTH_SHORT).show()
    }
}