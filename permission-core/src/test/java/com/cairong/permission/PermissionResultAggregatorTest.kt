package com.cairong.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
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
class PermissionResultAggregatorTest {
    
    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var activity: ComponentActivity
    
    private lateinit var aggregator: PermissionResultAggregator
    
    private val testPermissions = arrayOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION"
    )
    
    @Before
    fun setUp() {
        aggregator = PermissionResultAggregator()
    }
    
    @Test
    fun `aggregateResults should return all granted when all permissions are granted`() {
        // Given
        val results = mapOf(
            testPermissions[0] to true,
            testPermissions[1] to true,
            testPermissions[2] to true
        )
        
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            mockStatic(ActivityCompat::class.java).use { activityCompatMock ->
                // When
                val aggregatedResult = aggregator.aggregateResults(context, testPermissions, results)
                
                // Then
                assertTrue(aggregatedResult.allGranted)
                assertEquals(3, aggregatedResult.grantedPermissions.size)
                assertEquals(0, aggregatedResult.deniedPermissions.size)
                assertEquals(0, aggregatedResult.permanentlyDeniedPermissions.size)
                assertFalse(aggregatedResult.hasDeniedPermissions)
                assertFalse(aggregatedResult.hasPermanentlyDeniedPermissions)
                assertFalse(aggregatedResult.isPartiallyGranted)
            }
        }
    }
    
    @Test
    fun `aggregateResults should return partial granted when some permissions are denied`() {
        // Given
        val results = mapOf(
            testPermissions[0] to true,
            testPermissions[1] to false,
            testPermissions[2] to false
        )
        
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            mockStatic(ActivityCompat::class.java).use { activityCompatMock ->
                // Mock shouldShowRequestPermissionRationale to return true (not permanently denied)
                activityCompatMock.`when`<Boolean> {
                    ActivityCompat.shouldShowRequestPermissionRationale(any(), eq(testPermissions[1]))
                }.thenReturn(true)
                
                activityCompatMock.`when`<Boolean> {
                    ActivityCompat.shouldShowRequestPermissionRationale(any(), eq(testPermissions[2]))
                }.thenReturn(true)
                
                // When
                val aggregatedResult = aggregator.aggregateResults(context, testPermissions, results)
                
                // Then
                assertFalse(aggregatedResult.allGranted)
                assertEquals(1, aggregatedResult.grantedPermissions.size)
                assertEquals(2, aggregatedResult.deniedPermissions.size)
                assertEquals(0, aggregatedResult.permanentlyDeniedPermissions.size)
                assertTrue(aggregatedResult.hasDeniedPermissions)
                assertFalse(aggregatedResult.hasPermanentlyDeniedPermissions)
                assertTrue(aggregatedResult.isPartiallyGranted)
                assertEquals(2, aggregatedResult.temporarilyDeniedPermissions.size)
            }
        }
    }
    
    @Test
    fun `aggregateResults should identify permanently denied permissions`() {
        // Given
        val results = mapOf(
            testPermissions[0] to true,
            testPermissions[1] to false,
            testPermissions[2] to false
        )
        
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            mockStatic(ActivityCompat::class.java).use { activityCompatMock ->
                // Mock shouldShowRequestPermissionRationale
                activityCompatMock.`when`<Boolean> {
                    ActivityCompat.shouldShowRequestPermissionRationale(eq(activity), eq(testPermissions[1]))
                }.thenReturn(false) // Permanently denied
                
                activityCompatMock.`when`<Boolean> {
                    ActivityCompat.shouldShowRequestPermissionRationale(eq(activity), eq(testPermissions[2]))
                }.thenReturn(true) // Temporarily denied
                
                // When
                val aggregatedResult = aggregator.aggregateResults(activity, testPermissions, results)
                
                // Then
                assertFalse(aggregatedResult.allGranted)
                assertEquals(1, aggregatedResult.grantedPermissions.size)
                assertEquals(2, aggregatedResult.deniedPermissions.size)
                assertEquals(1, aggregatedResult.permanentlyDeniedPermissions.size)
                assertTrue(aggregatedResult.hasDeniedPermissions)
                assertTrue(aggregatedResult.hasPermanentlyDeniedPermissions)
                assertTrue(aggregatedResult.isPartiallyGranted)
                assertEquals(1, aggregatedResult.temporarilyDeniedPermissions.size)
                assertEquals(testPermissions[1], aggregatedResult.permanentlyDeniedPermissions[0])
                assertEquals(testPermissions[2], aggregatedResult.temporarilyDeniedPermissions[0])
            }
        }
    }
    
    @Test
    fun `aggregateResults should handle all denied permissions`() {
        // Given
        val results = mapOf(
            testPermissions[0] to false,
            testPermissions[1] to false,
            testPermissions[2] to false
        )
        
        mockStatic(ContextCompat::class.java).use { contextCompatMock ->
            mockStatic(ActivityCompat::class.java).use { activityCompatMock ->
                // Mock all as temporarily denied
                testPermissions.forEach { permission ->
                    activityCompatMock.`when`<Boolean> {
                        ActivityCompat.shouldShowRequestPermissionRationale(any(), eq(permission))
                    }.thenReturn(true)
                }
                
                // When
                val aggregatedResult = aggregator.aggregateResults(context, testPermissions, results)
                
                // Then
                assertFalse(aggregatedResult.allGranted)
                assertEquals(0, aggregatedResult.grantedPermissions.size)
                assertEquals(3, aggregatedResult.deniedPermissions.size)
                assertEquals(0, aggregatedResult.permanentlyDeniedPermissions.size)
                assertTrue(aggregatedResult.hasDeniedPermissions)
                assertFalse(aggregatedResult.hasPermanentlyDeniedPermissions)
                assertFalse(aggregatedResult.isPartiallyGranted)
                assertEquals(3, aggregatedResult.temporarilyDeniedPermissions.size)
            }
        }
    }
}