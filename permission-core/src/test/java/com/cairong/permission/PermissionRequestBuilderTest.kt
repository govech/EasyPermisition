package com.cairong.permission

import androidx.activity.ComponentActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PermissionRequestBuilderTest {
    
    @Mock
    private lateinit var activity: ComponentActivity
    
    private lateinit var builder: PermissionRequestBuilder
    
    @Before
    fun setUp() {
        builder = PermissionRequestBuilder(activity = activity)
    }
    
    @Test
    fun `builder should support method chaining`() {
        // When
        val result = builder
            .permission("android.permission.CAMERA")
            .rationale("需要相机权限")
            .onGranted { }
        
        // Then
        assertSame(builder, result)
    }
    
    @Test
    fun `builder should add single permission correctly`() {
        // Given
        val permission = "android.permission.CAMERA"
        
        // When
        builder.permission(permission)
        
        // Then - We can't directly test the internal state, but we can test the behavior
        // This would be tested through integration tests
    }
    
    @Test
    fun `builder should add multiple permissions correctly`() {
        // Given
        val permissions = arrayOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        
        // When
        builder.permissions(*permissions)
        
        // Then - We can't directly test the internal state, but we can test the behavior
        // This would be tested through integration tests
    }
    
    @Test
    fun `builder should add permission list correctly`() {
        // Given
        val permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
        
        // When
        builder.permissions(permissions)
        
        // Then - We can't directly test the internal state, but we can test the behavior
        // This would be tested through integration tests
    }
    
    @Test(expected = IllegalStateException::class)
    fun `request should throw exception when no permissions specified`() {
        // When
        builder.request() // Should throw exception
    }
    
    @Test
    fun `builder should set rationale correctly`() {
        // Given
        val rationale = "需要相机权限来拍照"
        
        // When
        val result = builder.rationale(rationale)
        
        // Then
        assertSame(builder, result)
    }
    
    @Test
    fun `builder should set rationale title correctly`() {
        // Given
        val title = "权限说明"
        
        // When
        val result = builder.rationaleTitle(title)
        
        // Then
        assertSame(builder, result)
    }
    
    @Test
    fun `builder should set button texts correctly`() {
        // Given
        val positiveText = "允许"
        val negativeText = "拒绝"
        
        // When
        val result1 = builder.positiveButtonText(positiveText)
        val result2 = builder.negativeButtonText(negativeText)
        
        // Then
        assertSame(builder, result1)
        assertSame(builder, result2)
    }
    
    @Test
    fun `builder should set settings texts correctly`() {
        // Given
        val settingsText = "请到设置页面开启权限"
        val settingsTitle = "权限设置"
        
        // When
        val result1 = builder.settingsText(settingsText)
        val result2 = builder.settingsTitle(settingsTitle)
        
        // Then
        assertSame(builder, result1)
        assertSame(builder, result2)
    }
    
    @Test
    fun `builder should set forceGoToSettings correctly`() {
        // When
        val result1 = builder.forceGoToSettings()
        val result2 = builder.forceGoToSettings(false)
        
        // Then
        assertSame(builder, result1)
        assertSame(builder, result2)
    }
    
    @Test
    fun `builder should set callbacks correctly`() {
        // Given
        var beforeRequestCalled = false
        var grantedCalled = false
        var deniedCalled = false
        var permanentlyDeniedCalled = false
        
        // When
        builder
            .onBeforeRequest { beforeRequestCalled = true }
            .onGranted { grantedCalled = true }
            .onDenied { _, _ -> deniedCalled = true }
            .onPermanentlyDenied { permanentlyDeniedCalled = true }
        
        // Then - Callbacks are set (would be tested through integration tests)
        // The builder should return itself for chaining
    }
    
    @Test
    fun `builder should set result callback correctly`() {
        // Given
        var resultCalled = false
        
        // When
        val result = builder.onResult { _, _, _ -> resultCalled = true }
        
        // Then
        assertSame(builder, result)
    }
}