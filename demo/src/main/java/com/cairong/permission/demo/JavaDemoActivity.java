package com.cairong.permission.demo;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cairong.permission.PermissionManager;
import com.cairong.permission.java.JavaPermissionCallback;
import com.cairong.permission.java.JavaPermissionManager;
import com.cairong.permission.java.SimpleJavaPermissionCallback;

/**
 * Java权限请求演示Activity
 * 
 * 演示如何在Java中使用权限框架
 */
public class JavaDemoActivity extends AppCompatActivity {
    
    private static final String TAG = "JavaDemoActivity";
    
    private TextView tvStatus;
    private Button btnRequestSingle;
    private Button btnRequestMultiple;
    private Button btnRequestWithCallback;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_demo);
        
        // 初始化权限管理器
        PermissionManager.initialize(this);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        btnRequestSingle = findViewById(R.id.btnRequestSingle);
        btnRequestMultiple = findViewById(R.id.btnRequestMultiple);
        btnRequestWithCallback = findViewById(R.id.btnRequestWithCallback);
        
        tvStatus.setText("Java权限请求演示\n点击按钮测试不同的权限请求方式");
    }
    
    private void setupClickListeners() {
        btnRequestSingle.setOnClickListener(v -> requestSinglePermission());
        btnRequestMultiple.setOnClickListener(v -> requestMultiplePermissions());
        btnRequestWithCallback.setOnClickListener(v -> requestWithDetailedCallback());
    }
    
    /**
     * 请求单个权限（使用简化回调）
     */
    private void requestSinglePermission() {
        Log.d(TAG, "请求单个权限");
        tvStatus.setText("正在请求相机权限...");
        
        JavaPermissionManager.with(this)
                .permission(Manifest.permission.CAMERA)
                .rationale("需要相机权限来拍照")
                .rationaleTitle("权限说明")
                .onJavaResult(new SimpleJavaPermissionCallback() {
                    @Override
                    public void onResult(boolean allGranted, String[] grantedPermissions, String[] deniedPermissions) {
                        if (allGranted) {
                            Log.d(TAG, "相机权限已授权");
                            tvStatus.setText("相机权限已授权！\n已授权权限: " + java.util.Arrays.toString(grantedPermissions));
                            Toast.makeText(JavaDemoActivity.this, "相机权限已授权", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "相机权限被拒绝");
                            tvStatus.setText("相机权限被拒绝\n被拒绝权限: " + java.util.Arrays.toString(deniedPermissions));
                            Toast.makeText(JavaDemoActivity.this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .request();
    }
    
    /**
     * 请求多个权限（使用简化回调）
     */
    private void requestMultiplePermissions() {
        Log.d(TAG, "请求多个权限");
        tvStatus.setText("正在请求相机和麦克风权限...");
        
        JavaPermissionManager.with(this)
                .cameraAndAudioPermissions() // 使用权限组方法
                .rationale("需要相机和麦克风权限来录制视频")
                .rationaleTitle("权限说明")
                .settingsText("权限被永久拒绝，请到设置页面手动开启")
                .onJavaResult(new SimpleJavaPermissionCallback() {
                    @Override
                    public void onResult(boolean allGranted, String[] grantedPermissions, String[] deniedPermissions) {
                        if (allGranted) {
                            Log.d(TAG, "所有权限已授权");
                            tvStatus.setText("所有权限已授权！\n已授权权限: " + java.util.Arrays.toString(grantedPermissions));
                            Toast.makeText(JavaDemoActivity.this, "所有权限已授权", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "部分权限被拒绝");
                            String statusText = "部分权限被拒绝\n";
                            if (grantedPermissions.length > 0) {
                                statusText += "已授权: " + java.util.Arrays.toString(grantedPermissions) + "\n";
                            }
                            if (deniedPermissions.length > 0) {
                                statusText += "被拒绝: " + java.util.Arrays.toString(deniedPermissions);
                            }
                            tvStatus.setText(statusText);
                            Toast.makeText(JavaDemoActivity.this, "部分权限被拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .request();
    }
    
    /**
     * 使用详细回调请求权限
     */
    private void requestWithDetailedCallback() {
        Log.d(TAG, "使用详细回调请求权限");
        tvStatus.setText("正在请求位置权限...");
        
        JavaPermissionManager.with(this)
                .locationPermissions() // 使用位置权限组
                .rationale("需要位置权限来获取您的当前位置")
                .rationaleTitle("位置权限说明")
                .settingsText("位置权限被永久拒绝，请到设置页面手动开启")
                .settingsTitle("权限设置")
                .onJavaCallback(new JavaPermissionCallback() {
                    @Override
                    public void onBeforeRequest(String[] permissions) {
                        Log.d(TAG, "即将请求权限: " + java.util.Arrays.toString(permissions));
                        tvStatus.setText("即将请求权限: " + java.util.Arrays.toString(permissions));
                    }
                    
                    @Override
                    public void onGranted(String[] permissions) {
                        Log.d(TAG, "权限已授权: " + java.util.Arrays.toString(permissions));
                        tvStatus.setText("权限已授权！\n" + java.util.Arrays.toString(permissions));
                        Toast.makeText(JavaDemoActivity.this, "位置权限已授权", Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onDenied(String[] deniedPermissions, String[] permanentlyDeniedPermissions) {
                        Log.d(TAG, "权限被拒绝 - 拒绝: " + java.util.Arrays.toString(deniedPermissions) + 
                                ", 永久拒绝: " + java.util.Arrays.toString(permanentlyDeniedPermissions));
                        
                        String statusText = "权限被拒绝\n";
                        if (deniedPermissions.length > 0) {
                            statusText += "临时拒绝: " + java.util.Arrays.toString(deniedPermissions) + "\n";
                        }
                        if (permanentlyDeniedPermissions.length > 0) {
                            statusText += "永久拒绝: " + java.util.Arrays.toString(permanentlyDeniedPermissions);
                        }
                        tvStatus.setText(statusText);
                        
                        if (permanentlyDeniedPermissions.length > 0) {
                            Toast.makeText(JavaDemoActivity.this, "权限被永久拒绝，请到设置页面开启", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(JavaDemoActivity.this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onPermanentlyDenied(String[] permanentlyDeniedPermissions) {
                        Log.d(TAG, "权限被永久拒绝: " + java.util.Arrays.toString(permanentlyDeniedPermissions));
                        tvStatus.setText("权限被永久拒绝\n请到设置页面手动开启权限");
                    }
                })
                .request();
    }
}