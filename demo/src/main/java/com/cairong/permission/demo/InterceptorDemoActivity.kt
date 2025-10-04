package com.cairong.permission.demo

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cairong.permission.PermissionManager
import com.cairong.permission.PermissionRequest
import com.cairong.permission.demo.databinding.ActivityInterceptorDemoBinding
import com.cairong.permission.interceptor.AnalyticsPermissionInterceptor
import com.cairong.permission.interceptor.LoggingPermissionInterceptor
import com.cairong.permission.interceptor.PermissionInterceptor
import com.cairong.permission.interceptor.PermissionInterceptorRegistry

/**
 * 拦截器演示Activity
 * 
 * 演示如何使用权限拦截器进行埋点、日志等扩展功能
 */
class InterceptorDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityInterceptorDemoBinding
    
    companion object {
        private const val TAG = "InterceptorDemoActivity"
    }
    
    // 自定义拦截器
    private val customInterceptor = object : PermissionInterceptor {
        override fun beforeRequest(request: PermissionRequest): Boolean {
            Log.d(TAG, "🔍 自定义拦截器：权限请求前检查")
            binding.tvLog.append("🔍 权限请求前检查: ${request.permissions.joinToString()}\n")
            
            // 可以在这里添加业务逻辑，比如检查用户状态
            return true // 返回true继续请求，false则拦截
        }
        
        override fun onGranted(request: PermissionRequest, grantedPermissions: Array<String>) {
            Log.d(TAG, "✅ 自定义拦截器：权限授权成功")
            binding.tvLog.append("✅ 权限授权成功: ${grantedPermissions.joinToString()}\n")
        }
        
        override fun onDenied(
            request: PermissionRequest,
            deniedPermissions: Array<String>,
            permanentlyDeniedPermissions: Array<String>
        ) {
            Log.d(TAG, "❌ 自定义拦截器：权限被拒绝")
            binding.tvLog.append("❌ 权限被拒绝: ${deniedPermissions.joinToString()}\n")
            if (permanentlyDeniedPermissions.isNotEmpty()) {
                binding.tvLog.append("🚫 永久拒绝: ${permanentlyDeniedPermissions.joinToString()}\n")
            }
        }
        
        override fun onError(request: PermissionRequest, exception: Throwable) {
            Log.e(TAG, "💥 自定义拦截器：权限请求出错", exception)
            binding.tvLog.append("💥 权限请求出错: ${exception.message}\n")
        }
    }
    
    // 埋点拦截器
    private val analyticsInterceptor = AnalyticsPermissionInterceptor { event, params ->
        Log.d(TAG, "📊 埋点事件: $event")
        binding.tvLog.append("📊 埋点事件: $event\n")
        params.forEach { (key, value) ->
            Log.d(TAG, "   $key: $value")
        }
    }
    
    // 日志拦截器
    private val loggingInterceptor = LoggingPermissionInterceptor { tag, message ->
        Log.d(tag, message)
        binding.tvLog.append("📝 $tag: $message\n")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterceptorDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        setupClickListeners()
        setupInterceptors()
    }
    
    private fun setupClickListeners() {
        binding.btnRegisterInterceptors.setOnClickListener {
            registerInterceptors()
        }
        
        binding.btnRequestWithInterceptors.setOnClickListener {
            requestPermissionWithInterceptors()
        }
        
        binding.btnUnregisterInterceptors.setOnClickListener {
            unregisterInterceptors()
        }
        
        binding.btnClearLog.setOnClickListener {
            clearLog()
        }
    }
    
    private fun setupInterceptors() {
        binding.tvStatus.text = "拦截器演示\n点击按钮注册拦截器并测试权限请求"
        binding.tvLog.text = "日志输出:\n"
    }
    
    /**
     * 注册拦截器
     */
    private fun registerInterceptors() {
        PermissionInterceptorRegistry.register(customInterceptor)
        PermissionInterceptorRegistry.register(analyticsInterceptor)
        PermissionInterceptorRegistry.register(loggingInterceptor)
        
        val count = PermissionInterceptorRegistry.getInterceptors().size
        binding.tvStatus.text = "已注册 $count 个拦截器\n- 自定义业务拦截器\n- 埋点统计拦截器\n- 日志记录拦截器"
        binding.tvLog.append("🔧 已注册 $count 个拦截器\n")
        
        Toast.makeText(this, "拦截器注册成功", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 使用拦截器请求权限
     */
    private fun requestPermissionWithInterceptors() {
        binding.tvStatus.text = "正在请求权限...\n拦截器将记录整个过程"
        binding.tvLog.append("\n🚀 开始权限请求流程\n")
        
        PermissionManager.with(this)
            .permission(Manifest.permission.CAMERA)
            .rationale("需要相机权限来拍照，拦截器将记录这个请求过程")
            .rationaleTitle("拦截器演示")
            .onBeforeRequest { permissions ->
                binding.tvLog.append("📋 即将请求权限: ${permissions.joinToString()}\n")
            }
            .onGranted { permissions ->
                binding.tvStatus.text = "权限请求成功！\n拦截器已记录授权事件"
                binding.tvLog.append("🎉 权限请求流程完成\n\n")
                Toast.makeText(this, "相机权限已授权", Toast.LENGTH_SHORT).show()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                binding.tvStatus.text = "权限被拒绝\n拦截器已记录拒绝事件"
                binding.tvLog.append("🎯 权限请求流程完成\n\n")
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
            }
            .request()
    }
    
    /**
     * 注销拦截器
     */
    private fun unregisterInterceptors() {
        PermissionInterceptorRegistry.unregister(customInterceptor)
        PermissionInterceptorRegistry.unregister(analyticsInterceptor)
        PermissionInterceptorRegistry.unregister(loggingInterceptor)
        
        val count = PermissionInterceptorRegistry.getInterceptors().size
        binding.tvStatus.text = "已注销拦截器\n当前注册的拦截器数量: $count"
        binding.tvLog.append("🗑️ 已注销所有演示拦截器\n")
        
        Toast.makeText(this, "拦截器已注销", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 清空日志
     */
    private fun clearLog() {
        binding.tvLog.text = "日志输出:\n"
        Toast.makeText(this, "日志已清空", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理拦截器，避免内存泄漏
        PermissionInterceptorRegistry.unregister(customInterceptor)
        PermissionInterceptorRegistry.unregister(analyticsInterceptor)
        PermissionInterceptorRegistry.unregister(loggingInterceptor)
    }
}