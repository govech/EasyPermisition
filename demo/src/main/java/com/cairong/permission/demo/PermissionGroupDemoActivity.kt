package com.cairong.permission.demo

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cairong.permission.PermissionGroups
import com.cairong.permission.PermissionManager
import com.cairong.permission.PermissionCallback
import com.cairong.permission.demo.databinding.ActivityPermissionGroupDemoBinding

/**
 * 权限组批量申请演示
 * 
 * 展示如何使用权限组功能一次性申请相关权限
 */
class PermissionGroupDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPermissionGroupDemoBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionGroupDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // 存储权限组演示
        binding.btnRequestStorageGroup.setOnClickListener {
            requestStoragePermissions()
        }
        
        // 位置权限组演示
        binding.btnRequestLocationGroup.setOnClickListener {
            requestLocationPermissions()
        }
        
        // 媒体权限组演示（Android 13+）
        binding.btnRequestMediaGroup.setOnClickListener {
            requestMediaPermissions()
        }
        
        // 传感器权限组演示
        binding.btnRequestSensorGroup.setOnClickListener {
            requestSensorPermissions()
        }
    }
    
    private fun requestStoragePermissions() {
        val storagePermissions = PermissionGroups.STORAGE_PERMISSIONS
        
        PermissionManager.with(this)
            .permissions(*storagePermissions)
            .rationale("需要存储权限来保存和读取文件")
            .onBeforeRequest { 
                binding.tvStorageStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("存储权限组已全部授权")
                binding.tvStorageStatus.text = "状态：已授权"
            }
            .onDenied { _, deniedPermissions ->
                showToast("部分存储权限被拒绝：${deniedPermissions.joinToString()}")
                binding.tvStorageStatus.text = "状态：部分拒绝"
            }
            .onPermanentlyDenied { permanentlyDeniedPermissions ->
                showToast("存储权限被永久拒绝，请到设置页面手动开启")
                binding.tvStorageStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun requestLocationPermissions() {
        val locationPermissions = PermissionGroups.LOCATION_PERMISSIONS
        
        PermissionManager.with(this)
            .permissions(*locationPermissions)
            .rationale("需要位置权限来获取精确位置和后台位置")
            .onBeforeRequest { 
                binding.tvLocationStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("位置权限组已全部授权")
                binding.tvLocationStatus.text = "状态：已授权"
            }
            .onDenied { _, deniedPermissions ->
                showToast("部分位置权限被拒绝：${deniedPermissions.joinToString()}")
                binding.tvLocationStatus.text = "状态：部分拒绝"
            }
            .onPermanentlyDenied { permanentlyDeniedPermissions ->
                showToast("位置权限被永久拒绝，请到设置页面手动开启")
                binding.tvLocationStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun requestMediaPermissions() {
        val mediaPermissions = PermissionGroups.MEDIA_PERMISSIONS_ANDROID_13
        
        PermissionManager.with(this)
            .permissions(*mediaPermissions)
            .rationale("需要媒体权限来访问照片、视频和音频文件")
            .onBeforeRequest { 
                binding.tvMediaStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("媒体权限组已全部授权")
                binding.tvMediaStatus.text = "状态：已授权"
            }
            .onDenied { _, deniedPermissions ->
                showToast("部分媒体权限被拒绝：${deniedPermissions.joinToString()}")
                binding.tvMediaStatus.text = "状态：部分拒绝"
            }
            .onPermanentlyDenied { permanentlyDeniedPermissions ->
                showToast("媒体权限被永久拒绝，请到设置页面手动开启")
                binding.tvMediaStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun requestSensorPermissions() {
        val sensorPermissions = listOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        
        PermissionManager.with(this)
            .permissions(*sensorPermissions.toTypedArray())
            .rationale("需要传感器权限来检测身体传感器和活动识别")
            .onBeforeRequest { 
                binding.tvSensorStatus.text = "状态：请求中..."
            }
            .onGranted { 
                showToast("传感器权限组已全部授权")
                binding.tvSensorStatus.text = "状态：已授权"
            }
            .onDenied { _, deniedPermissions ->
                showToast("部分传感器权限被拒绝：${deniedPermissions.joinToString()}")
                binding.tvSensorStatus.text = "状态：部分拒绝"
            }
            .onPermanentlyDenied { permanentlyDeniedPermissions ->
                showToast("传感器权限被永久拒绝，请到设置页面手动开启")
                binding.tvSensorStatus.text = "状态：永久拒绝"
            }
            .request()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}