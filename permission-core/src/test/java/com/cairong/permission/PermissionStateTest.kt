package com.cairong.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PermissionStateTest {
    
    @Mock
    private lateinit var context: Context
    
    private val testPermission = "android.permission.CAMERA"
    private val testPermissions = arrayOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO")
    
    @Test
    fun `checkPermissionState should return GRANTED when permission is granted`() {
        // Given
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermission) 
            }.thenReturn(PackageManager.PERMISSION_GRANTED)
            
            // When
            val state = PermissionStateChecker.checkPermissionState(context, testPermission)
            
            // Then
            assertEquals(PermissionState.GRANTED, state)
        }
    }
    
    @Test
    fun `checkPermissionState should return DENIED when permission is denied`() {
        // Given
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermission) 
            }.thenReturn(PackageManager.PERMISSION_DENIED)
            
            // When
            val state = PermissionStateChecker.checkPermissionState(context, testPermission)
            
            // Then
            assertEquals(PermissionState.DENIED, state)
        }
    }
    
    @Test
    fun `areAllPermissionsGranted should return true when all permissions are granted`() {
        // Given
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            testPermissions.forEach { permission ->
                contextCompatMock.`when`<Int> { 
                    ContextCompat.checkSelfPermission(context, permission) 
                }.thenReturn(PackageManager.PERMISSION_GRANTED)
            }
            
            // When
            val result = PermissionStateChecker.areAllPermissionsGranted(context, testPermissions)
            
            // Then
            assertTrue(result)
        }
    }
    
    @Test
    fun `areAllPermissionsGranted should return false when some permissions are denied`() {
        // Given
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermissions[0]) 
            }.thenReturn(PackageManager.PERMISSION_GRANTED)
            
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermissions[1]) 
            }.thenReturn(PackageManager.PERMISSION_DENIED)
            
            // When
            val result = PermissionStateChecker.areAllPermissionsGranted(context, testPermissions)
            
            // Then
            assertFalse(result)
        }
    }
    
    @Test
    fun `getGrantedPermissions should return only granted permissions`() {
        // Given
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermissions[0]) 
            }.thenReturn(PackageManager.PERMISSION_GRANTED)
            
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermissions[1]) 
            }.thenReturn(PackageManager.PERMISSION_DENIED)
            
            // When
            val grantedPermissions = PermissionStateChecker.getGrantedPermissions(context, testPermissions)
            
            // Then
            assertEquals(1, grantedPermissions.size)
            assertEquals(testPermissions[0], grantedPermissions[0])
        }
    }
    
    @Test
    fun `getDeniedPermissions should return only denied permissions`() {
        // Given
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermissions[0]) 
            }.thenReturn(PackageManager.PERMISSION_GRANTED)
            
            contextCompatMock.`when`<Int> { 
                ContextCompat.checkSelfPermission(context, testPermissions[1]) 
            }.thenReturn(PackageManager.PERMISSION_DENIED)
            
            // When
            val deniedPermissions = PermissionStateChecker.getDeniedPermissions(context, testPermissions)
            
            // Then
            assertEquals(1, deniedPermissions.size)
            assertEquals(testPermissions[1], deniedPermissions[0])
        }
    }
}