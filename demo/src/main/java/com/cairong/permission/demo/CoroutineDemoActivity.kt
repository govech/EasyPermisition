package com.cairong.permission.demo

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cairong.permission.PermissionManager
import com.cairong.permission.coroutine.await
import com.cairong.permission.coroutine.awaitResult
import com.cairong.permission.coroutine.observePermissionState
import com.cairong.permission.demo.databinding.ActivityCoroutineDemoBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Kotlin协程权限请求演示Activity
 * 
 * 演示如何在Kotlin协程中使用权限框架
 */
class CoroutineDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCoroutineDemoBinding
    
    companion object {
        private const val TAG = "CoroutineDemoActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoroutineDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        setupClickListeners()
        observePermissionStates()
    }
    
    private fun setupClickListeners() {
        binding.btnRequestSingle.setOnClickListener {
            requestSinglePermissionWithCoroutine()
        }
        
        binding.btnRequestMultiple.setOnClickListener {
            requestMultiplePermissionsWithCoroutine()
        }
        
        binding.btnRequestDetailed.setOnClickListener {
            requestWithDetailedResult()
        }
        
        binding.btnObserveState.setOnClickListener {
            observeCameraPermissionState()
        }
    }
    
    /**
     * 使用协程请求单个权限
     */
    private fun requestSinglePermissionWithCoroutine() {
        Log.d(TAG, "使用协程请求单个权限")
        binding.tvStatus.text = "正在请求相机权限..."
        
        lifecycleScope.launch {
            try {
                val granted = PermissionManager.with(this@CoroutineDemoActivity)
                    .permission(Manifest.permission.CAMERA)
                    .rationale("需要相机权限来拍照")
                    .rationaleTitle("权限说明")
                    .await()
                
                if (granted) {
                    Log.d(TAG, "相机权限已授权")
                    binding.tvStatus.text = "相机权限已授权！\n可以开始使用相机功能"
                    Toast.makeText(this@CoroutineDemoActivity, "相机权限已授权", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "相机权限被拒绝")
                    binding.tvStatus.text = "相机权限被拒绝\n无法使用相机功能"
                    Toast.makeText(this@CoroutineDemoActivity, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "权限请求出错", e)
                binding.tvStatus.text = "权限请求出错: ${e.message}"
            }
        }
    }
    
    /**
     * 使用协程请求多个权限
     */
    private fun requestMultiplePermissionsWithCoroutine() {
        Log.d(TAG, "使用协程请求多个权限")
        binding.tvStatus.text = "正在请求相机和麦克风权限..."
        
        lifecycleScope.launch {
            try {
                val granted = PermissionManager.with(this@CoroutineDemoActivity)
                    .cameraAndAudioPermissions() // 使用权限组方法
                    .rationale("需要相机和麦克风权限来录制视频")
                    .rationaleTitle("权限说明")
                    .settingsText("权限被永久拒绝，请到设置页面手动开启")
                    .await()
                
                if (granted) {
                    Log.d(TAG, "所有权限已授权")
                    binding.tvStatus.text = "所有权限已授权！\n可以开始录制视频"
                    Toast.makeText(this@CoroutineDemoActivity, "所有权限已授权", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "部分权限被拒绝")
                    binding.tvStatus.text = "部分权限被拒绝\n无法完整使用录制功能"
                    Toast.makeText(this@CoroutineDemoActivity, "部分权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "权限请求出错", e)
                binding.tvStatus.text = "权限请求出错: ${e.message}"
            }
        }
    }
    
    /**
     * 使用协程获取详细的权限请求结果
     */
    private fun requestWithDetailedResult() {
        Log.d(TAG, "使用协程获取详细权限结果")
        binding.tvStatus.text = "正在请求位置权限..."
        
        lifecycleScope.launch {
            try {
                val result = PermissionManager.with(this@CoroutineDemoActivity)
                    .locationPermissions() // 使用位置权限组
                    .rationale("需要位置权限来获取您的当前位置")
                    .rationaleTitle("位置权限说明")
                    .settingsText("位置权限被永久拒绝，请到设置页面手动开启")
                    .awaitResult()
                
                Log.d(TAG, "权限请求结果: $result")
                
                val statusText = buildString {
                    append("权限请求完成\n")
                    append("全部授权: ${result.allGranted}\n")
                    
                    if (result.grantedPermissions.isNotEmpty()) {
                        append("已授权: ${result.grantedPermissions.joinToString()}\n")
                    }
                    
                    if (result.deniedPermissions.isNotEmpty()) {
                        append("被拒绝: ${result.deniedPermissions.joinToString()}\n")
                    }
                    
                    if (result.permanentlyDeniedPermissions.isNotEmpty()) {
                        append("永久拒绝: ${result.permanentlyDeniedPermissions.joinToString()}\n")
                    }
                    
                    result.error?.let { error ->
                        append("错误: ${error.message}")
                    }
                }
                
                binding.tvStatus.text = statusText
                
                when {
                    result.allGranted -> {
                        Toast.makeText(this@CoroutineDemoActivity, "位置权限已授权", Toast.LENGTH_SHORT).show()
                    }
                    result.hasPermanentlyDeniedPermissions -> {
                        Toast.makeText(this@CoroutineDemoActivity, "权限被永久拒绝，请到设置页面开启", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this@CoroutineDemoActivity, "权限被拒绝", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "权限请求出错", e)
                binding.tvStatus.text = "权限请求出错: ${e.message}"
            }
        }
    }
    
    /**
     * 观察相机权限状态变化
     */
    private fun observeCameraPermissionState() {
        Log.d(TAG, "开始观察相机权限状态")
        binding.tvStatus.text = "开始观察相机权限状态变化...\n请到设置中手动修改权限状态来测试"
        
        observePermissionState(this, Manifest.permission.CAMERA)
            .onEach { state ->
                Log.d(TAG, "相机权限状态变化: $state")
                val statusText = "相机权限状态: $state\n" +
                        when (state) {
                            com.cairong.permission.PermissionState.GRANTED -> "✅ 已授权"
                            com.cairong.permission.PermissionState.DENIED -> "❌ 被拒绝（可以显示解释）"
                            com.cairong.permission.PermissionState.PERMANENTLY_DENIED -> "🚫 被永久拒绝"
                        }
                binding.tvStatus.text = statusText
            }
            .launchIn(lifecycleScope)
        
        Toast.makeText(this, "已开始观察权限状态，请到设置中修改权限", Toast.LENGTH_LONG).show()
    }
    
    /**
     * 观察多个权限状态（演示用）
     */
    private fun observePermissionStates() {
        // 这里可以添加对多个权限状态的观察
        // 作为演示，我们只在日志中记录
        Log.d(TAG, "Activity创建，可以在这里设置权限状态观察")
    }
}