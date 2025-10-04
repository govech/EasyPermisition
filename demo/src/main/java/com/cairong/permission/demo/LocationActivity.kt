package com.cairong.permission.demo

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cairong.permission.PermissionManager
import com.cairong.permission.demo.databinding.ActivityLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * 位置权限演示Activity
 * 
 * 演示如何使用权限框架请求位置权限，包括前台和后台位置权限
 */
class LocationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    companion object {
        private const val TAG = "LocationActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setupClickListeners()
        checkLocationPermissions()
    }
    
    private fun setupClickListeners() {
        binding.btnRequestLocation.setOnClickListener {
            requestLocationPermissions()
        }
        
        binding.btnRequestBackground.setOnClickListener {
            requestBackgroundLocationPermission()
        }
        
        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }
    }
    
    /**
     * 检查位置权限状态
     */
    private fun checkLocationPermissions() {
        val hasFineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasBackgroundLocation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 10以下不需要后台位置权限
        }
        
        updateStatusText(hasFineLocation, hasCoarseLocation, hasBackgroundLocation)
    }
    
    /**
     * 更新状态文本
     */
    private fun updateStatusText(hasFine: Boolean, hasCoarse: Boolean, hasBackground: Boolean) {
        val status = buildString {
            append("权限状态:\n")
            append("精确位置: ${if (hasFine) "已授权" else "未授权"}\n")
            append("大致位置: ${if (hasCoarse) "已授权" else "未授权"}\n")
            append("后台位置: ${if (hasBackground) "已授权" else "未授权"}")
        }
        binding.tvStatus.text = status
        
        // 更新按钮状态
        binding.btnGetLocation.isEnabled = hasFine || hasCoarse
        binding.btnRequestBackground.isEnabled = hasFine || hasCoarse
    }
    
    /**
     * 请求位置权限
     */
    private fun requestLocationPermissions() {
        PermissionManager.with(this)
            .locationPermissions() // 使用权限组方法
            .rationale("需要位置权限来获取您的当前位置，以便提供基于位置的服务")
            .rationaleTitle("位置权限说明")
            .settingsText("位置权限被永久拒绝，请到设置页面手动开启")
            .settingsTitle("权限设置")
            .onBeforeRequest { permissions ->
                Log.d(TAG, "即将请求位置权限: ${permissions.joinToString()}")
                binding.tvStatus.text = "正在请求位置权限..."
            }
            .onGranted { permissions ->
                Log.d(TAG, "位置权限已授权: ${permissions.joinToString()}")
                Toast.makeText(this, "位置权限已授权", Toast.LENGTH_SHORT).show()
                checkLocationPermissions()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "位置权限被拒绝: ${deniedPermissions.joinToString()}")
                Log.d(TAG, "永久拒绝权限: ${permanentlyDeniedPermissions.joinToString()}")
                
                val message = if (permanentlyDeniedPermissions.isNotEmpty()) {
                    "位置权限被永久拒绝，请到设置页面手动开启"
                } else {
                    "位置权限被拒绝，无法获取位置信息"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                checkLocationPermissions()
            }
            .onPermanentlyDenied { permanentlyDeniedPermissions ->
                Log.d(TAG, "位置权限被永久拒绝: ${permanentlyDeniedPermissions.joinToString()}")
                binding.tvStatus.text = "位置权限被永久拒绝，请到设置页面手动开启"
            }
            .request()
    }
    
    /**
     * 请求后台位置权限
     */
    private fun requestBackgroundLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            Toast.makeText(this, "当前Android版本不需要后台位置权限", Toast.LENGTH_SHORT).show()
            return
        }
        
        PermissionManager.with(this)
            .permission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            .rationale("需要后台位置权限来在应用不在前台时继续获取位置信息")
            .rationaleTitle("后台位置权限说明")
            .settingsText("后台位置权限被永久拒绝，请到设置页面手动开启")
            .onBeforeRequest { permissions ->
                Log.d(TAG, "即将请求后台位置权限: ${permissions.joinToString()}")
                binding.tvStatus.text = "正在请求后台位置权限..."
            }
            .onGranted { permissions ->
                Log.d(TAG, "后台位置权限已授权: ${permissions.joinToString()}")
                Toast.makeText(this, "后台位置权限已授权", Toast.LENGTH_SHORT).show()
                checkLocationPermissions()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "后台位置权限被拒绝: ${deniedPermissions.joinToString()}")
                
                val message = if (permanentlyDeniedPermissions.isNotEmpty()) {
                    "后台位置权限被永久拒绝"
                } else {
                    "后台位置权限被拒绝"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                checkLocationPermissions()
            }
            .request()
    }
    
    /**
     * 获取当前位置
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val hasFineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasFineLocation && !hasCoarseLocation) {
            Toast.makeText(this, "没有位置权限，无法获取位置", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.tvStatus.text = "正在获取位置..."
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val locationText = "当前位置:\n" +
                            "纬度: ${location.latitude}\n" +
                            "经度: ${location.longitude}\n" +
                            "精度: ${location.accuracy}米"
                    binding.tvStatus.text = locationText
                    Log.d(TAG, "获取到位置: $locationText")
                } else {
                    binding.tvStatus.text = "无法获取位置信息"
                    Log.d(TAG, "位置信息为空")
                }
            }
            .addOnFailureListener { exception ->
                val errorText = "获取位置失败: ${exception.message}"
                binding.tvStatus.text = errorText
                Log.e(TAG, errorText, exception)
            }
    }
}