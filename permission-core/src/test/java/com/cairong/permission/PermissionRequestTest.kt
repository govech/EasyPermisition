package com.cairong.permission

import org.junit.Assert.*
import org.junit.Test

class PermissionRequestTest {
    
    private val testPermissions = arrayOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
    private val singlePermission = arrayOf("android.permission.CAMERA")
    
    @Test
    fun `PermissionRequest should correctly identify single permission request`() {
        // Given
        val request = PermissionRequest(permissions = singlePermission)
        
        // When & Then
        assertTrue(request.isSinglePermission)
        assertEquals("android.permission.CAMERA", request.singlePermission)
    }
    
    @Test
    fun `PermissionRequest should correctly identify multiple permission request`() {
        // Given
        val request = PermissionRequest(permissions = testPermissions)
        
        // When & Then
        assertFalse(request.isSinglePermission)
    }
    
    @Test(expected = IllegalStateException::class)
    fun `singlePermission should throw exception for multiple permissions`() {
        // Given
        val request = PermissionRequest(permissions = testPermissions)
        
        // When
        request.singlePermission // Should throw exception
    }
    
    @Test
    fun `PermissionRequest should have correct default values`() {
        // Given
        val request = PermissionRequest(permissions = testPermissions)
        
        // When & Then
        assertNull(request.rationale)
        assertNull(request.rationaleTitle)
        assertEquals("确定", request.positiveButtonText)
        assertEquals("取消", request.negativeButtonText)
        assertNull(request.settingsText)
        assertNull(request.settingsTitle)
        assertFalse(request.forceGoToSettings)
        assertNull(request.callback)
    }
    
    @Test
    fun `PermissionRequest should correctly set custom values`() {
        // Given
        val rationale = "需要相机权限"
        val rationaleTitle = "权限说明"
        val positiveText = "允许"
        val negativeText = "拒绝"
        val settingsText = "去设置"
        val settingsTitle = "设置权限"
        val forceGoToSettings = true
        
        // When
        val request = PermissionRequest(
            permissions = testPermissions,
            rationale = rationale,
            rationaleTitle = rationaleTitle,
            positiveButtonText = positiveText,
            negativeButtonText = negativeText,
            settingsText = settingsText,
            settingsTitle = settingsTitle,
            forceGoToSettings = forceGoToSettings
        )
        
        // Then
        assertEquals(rationale, request.rationale)
        assertEquals(rationaleTitle, request.rationaleTitle)
        assertEquals(positiveText, request.positiveButtonText)
        assertEquals(negativeText, request.negativeButtonText)
        assertEquals(settingsText, request.settingsText)
        assertEquals(settingsTitle, request.settingsTitle)
        assertTrue(request.forceGoToSettings)
    }
    
    @Test
    fun `PermissionRequest equals should work correctly`() {
        // Given
        val request1 = PermissionRequest(
            permissions = testPermissions,
            rationale = "需要权限"
        )
        val request2 = PermissionRequest(
            permissions = testPermissions,
            rationale = "需要权限"
        )
        val request3 = PermissionRequest(
            permissions = singlePermission,
            rationale = "需要权限"
        )
        
        // When & Then
        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
    }
    
    @Test
    fun `PermissionRequest hashCode should work correctly`() {
        // Given
        val request1 = PermissionRequest(
            permissions = testPermissions,
            rationale = "需要权限"
        )
        val request2 = PermissionRequest(
            permissions = testPermissions,
            rationale = "需要权限"
        )
        
        // When & Then
        assertEquals(request1.hashCode(), request2.hashCode())
    }
}