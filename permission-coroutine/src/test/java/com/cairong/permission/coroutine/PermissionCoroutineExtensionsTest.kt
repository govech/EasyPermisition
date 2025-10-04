package com.cairong.permission.coroutine

import com.cairong.permission.PermissionRequestBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PermissionCoroutineExtensionsTest {
    
    @Mock
    private lateinit var mockBuilder: PermissionRequestBuilder
    
    @Test
    fun `PermissionResult should have correct properties`() {
        // Given
        val grantedPermissions = arrayOf("permission1", "permission2")
        val deniedPermissions = arrayOf("permission3")
        val permanentlyDeniedPermissions = arrayOf("permission4")
        
        // When
        val result = PermissionResult(
            allGranted = false,
            grantedPermissions = grantedPermissions,
            deniedPermissions = deniedPermissions,
            permanentlyDeniedPermissions = permanentlyDeniedPermissions
        )
        
        // Then
        assertFalse(result.allGranted)
        assertTrue(result.hasDeniedPermissions)
        assertTrue(result.hasPermanentlyDeniedPermissions)
        assertTrue(result.isPartiallyGranted)
        assertArrayEquals(grantedPermissions, result.grantedPermissions)
        assertArrayEquals(deniedPermissions, result.deniedPermissions)
        assertArrayEquals(permanentlyDeniedPermissions, result.permanentlyDeniedPermissions)
        assertNull(result.error)
    }
    
    @Test
    fun `PermissionResult should identify all granted state`() {
        // Given
        val grantedPermissions = arrayOf("permission1", "permission2")
        
        // When
        val result = PermissionResult(
            allGranted = true,
            grantedPermissions = grantedPermissions,
            deniedPermissions = emptyArray(),
            permanentlyDeniedPermissions = emptyArray()
        )
        
        // Then
        assertTrue(result.allGranted)
        assertFalse(result.hasDeniedPermissions)
        assertFalse(result.hasPermanentlyDeniedPermissions)
        assertFalse(result.isPartiallyGranted)
    }
    
    @Test
    fun `PermissionResult should identify all denied state`() {
        // Given
        val deniedPermissions = arrayOf("permission1", "permission2")
        
        // When
        val result = PermissionResult(
            allGranted = false,
            grantedPermissions = emptyArray(),
            deniedPermissions = deniedPermissions,
            permanentlyDeniedPermissions = emptyArray()
        )
        
        // Then
        assertFalse(result.allGranted)
        assertTrue(result.hasDeniedPermissions)
        assertFalse(result.hasPermanentlyDeniedPermissions)
        assertFalse(result.isPartiallyGranted)
    }
    
    @Test
    fun `PermissionResult should handle error state`() {
        // Given
        val error = RuntimeException("Test error")
        
        // When
        val result = PermissionResult(
            allGranted = false,
            grantedPermissions = emptyArray(),
            deniedPermissions = emptyArray(),
            permanentlyDeniedPermissions = emptyArray(),
            error = error
        )
        
        // Then
        assertFalse(result.allGranted)
        assertFalse(result.hasDeniedPermissions)
        assertFalse(result.hasPermanentlyDeniedPermissions)
        assertFalse(result.isPartiallyGranted)
        assertEquals(error, result.error)
    }
    
    @Test
    fun `PermissionResult equals should work correctly`() {
        // Given
        val result1 = PermissionResult(
            allGranted = true,
            grantedPermissions = arrayOf("permission1"),
            deniedPermissions = emptyArray(),
            permanentlyDeniedPermissions = emptyArray()
        )
        val result2 = PermissionResult(
            allGranted = true,
            grantedPermissions = arrayOf("permission1"),
            deniedPermissions = emptyArray(),
            permanentlyDeniedPermissions = emptyArray()
        )
        val result3 = PermissionResult(
            allGranted = false,
            grantedPermissions = emptyArray(),
            deniedPermissions = arrayOf("permission1"),
            permanentlyDeniedPermissions = emptyArray()
        )
        
        // When & Then
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }
    
    @Test
    fun `PermissionResult hashCode should work correctly`() {
        // Given
        val result1 = PermissionResult(
            allGranted = true,
            grantedPermissions = arrayOf("permission1"),
            deniedPermissions = emptyArray(),
            permanentlyDeniedPermissions = emptyArray()
        )
        val result2 = PermissionResult(
            allGranted = true,
            grantedPermissions = arrayOf("permission1"),
            deniedPermissions = emptyArray(),
            permanentlyDeniedPermissions = emptyArray()
        )
        
        // When & Then
        assertEquals(result1.hashCode(), result2.hashCode())
    }
}