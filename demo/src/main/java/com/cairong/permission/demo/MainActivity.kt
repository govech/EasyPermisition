package com.cairong.permission.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cairong.permission.demo.databinding.ActivityMainBinding

/**
 * 主Activity
 * 
 * 展示各种权限请求演示的入口
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnCameraDemo.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        
        binding.btnLocationDemo.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
        
        binding.btnMultipleDemo.setOnClickListener {
            startActivity(Intent(this, CoroutineDemoActivity::class.java))
        }
        
        binding.btnJavaDemo.setOnClickListener {
            startActivity(Intent(this, JavaDemoActivity::class.java))
        }
        
        binding.btnCustomUIDemo.setOnClickListener {
            startActivity(Intent(this, CustomUIDemoActivity::class.java))
        }
        
        binding.btnInterceptorDemo.setOnClickListener {
            startActivity(Intent(this, InterceptorDemoActivity::class.java))
        }
        
        binding.btnPermissionGroupDemo.setOnClickListener {
            startActivity(Intent(this, PermissionGroupDemoActivity::class.java))
        }
        
        binding.btnAndroid14Demo.setOnClickListener {
            startActivity(Intent(this, Android14DemoActivity::class.java))
        }
    }
}