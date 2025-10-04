package com.cairong.permission.demo

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.cairong.permission.PermissionManager
import com.cairong.permission.demo.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 相机权限演示Activity
 * 
 * 演示如何使用权限框架请求相机权限，并在获得权限后启动相机预览
 */
class CameraActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    
    companion object {
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        setupClickListeners()
        checkCameraPermission()
    }
    
    private fun setupClickListeners() {
        binding.btnRequestCamera.setOnClickListener {
            requestCameraPermission()
        }
        
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }
    }
    
    /**
     * 检查相机权限状态
     */
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            binding.tvStatus.text = "相机权限已授权"
            startCamera()
        } else {
            binding.tvStatus.text = "请先请求相机权限"
        }
    }
    
    /**
     * 请求相机权限
     */
    private fun requestCameraPermission() {
        PermissionManager.with(this)
            .permission(Manifest.permission.CAMERA)
            .rationale(getString(R.string.camera_permission_rationale))
            .rationaleTitle("权限说明")
            .settingsText(getString(R.string.camera_permission_permanently_denied))
            .settingsTitle("权限设置")
            .onBeforeRequest { permissions ->
                Log.d(TAG, "即将请求权限: ${permissions.joinToString()}")
                binding.tvStatus.text = "正在请求相机权限..."
            }
            .onGranted { permissions ->
                Log.d(TAG, "权限已授权: ${permissions.joinToString()}")
                binding.tvStatus.text = "相机权限已授权"
                Toast.makeText(this, "相机权限已授权", Toast.LENGTH_SHORT).show()
                startCamera()
            }
            .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                Log.d(TAG, "权限被拒绝: ${deniedPermissions.joinToString()}")
                Log.d(TAG, "永久拒绝权限: ${permanentlyDeniedPermissions.joinToString()}")
                
                if (permanentlyDeniedPermissions.isNotEmpty()) {
                    binding.tvStatus.text = "相机权限被永久拒绝"
                    Toast.makeText(this, getString(R.string.camera_permission_permanently_denied), Toast.LENGTH_LONG).show()
                } else {
                    binding.tvStatus.text = "相机权限被拒绝"
                    Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
            .onPermanentlyDenied { permanentlyDeniedPermissions ->
                Log.d(TAG, "权限被永久拒绝: ${permanentlyDeniedPermissions.joinToString()}")
                binding.tvStatus.text = "相机权限被永久拒绝，请到设置页面手动开启"
            }
            .request()
    }
    
    /**
     * 启动相机
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            try {
                // 获取相机提供者
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                
                // 创建预览
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
                
                // 创建图像捕获
                imageCapture = ImageCapture.Builder().build()
                
                // 选择后置相机
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    // 解绑所有用例
                    cameraProvider.unbindAll()
                    
                    // 绑定用例到相机
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                    )
                    
                    // 显示预览视图和拍照按钮
                    binding.previewView.visibility = View.VISIBLE
                    binding.btnTakePhoto.isEnabled = true
                    
                } catch (exc: Exception) {
                    Log.e(TAG, "用例绑定失败", exc)
                    binding.tvStatus.text = "相机启动失败"
                }
                
            } catch (exc: Exception) {
                Log.e(TAG, "相机启动失败", exc)
                binding.tvStatus.text = "相机启动失败"
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    /**
     * 拍照
     */
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        // 创建输出文件
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "$name.jpg"
        )
        
        // 创建输出选项
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // 拍照
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "拍照失败: ${exception.message}", exception)
                    Toast.makeText(this@CameraActivity, "拍照失败", Toast.LENGTH_SHORT).show()
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "拍照成功: ${photoFile.absolutePath}"
                    Toast.makeText(this@CameraActivity, "拍照成功", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}