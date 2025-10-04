package com.cairong.permission

import android.Manifest
import org.junit.Assert.*
import org.junit.Test

class PermissionGroupsTest {
    
    @Test
    fun `getPermissionGroup should return correct group for location permissions`() {
        // Given & When
        val fineLocationGroup = PermissionGroups.getPermissionGroup(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationGroup = PermissionGroups.getPermissionGroup(Manifest.permission.ACCESS_COARSE_LOCATION)
        
        // Then
        assertNotNull(fineLocationGroup)
        assertNotNull(coarseLocationGroup)
        assertArrayEquals(PermissionGroups.LOCATION_PERMISSIONS, fineLocationGroup)
        assertArrayEquals(PermissionGroups.LOCATION_PERMISSIONS, coarseLocationGroup)
    }
    
    @Test
    fun `getPermissionGroup should return correct group for storage permissions`() {
        // Given & When
        val readStorageGroup = PermissionGroups.getPermissionGroup(Manifest.permission.READ_EXTERNAL_STORAGE)
        val writeStorageGroup = PermissionGroups.getPermissionGroup(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        
        // Then
        assertNotNull(readStorageGroup)
        assertNotNull(writeStorageGroup)
        assertArrayEquals(PermissionGroups.STORAGE_PERMISSIONS, readStorageGroup)
        assertArrayEquals(PermissionGroups.STORAGE_PERMISSIONS, writeStorageGroup)
    }
    
    @Test
    fun `getPermissionGroup should return null for unknown permission`() {
        // Given & When
        val unknownGroup = PermissionGroups.getPermissionGroup("unknown.permission")
        
        // Then
        assertNull(unknownGroup)
    }
    
    @Test
    fun `areInSameGroup should return true for permissions in same group`() {
        // Given & When
        val result = PermissionGroups.areInSameGroup(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `areInSameGroup should return false for permissions in different groups`() {
        // Given & When
        val result = PermissionGroups.areInSameGroup(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `areInSameGroup should return false for unknown permissions`() {
        // Given & When
        val result = PermissionGroups.areInSameGroup(
            "unknown.permission1",
            "unknown.permission2"
        )
        
        // Then
        assertFalse(result)
    }
}

class PermissionDependencyManagerTest {
    
    private val dependencyManager = PermissionDependencyManager()
    
    @Test
    fun `getDependencies should return null for permission without dependencies`() {
        // Given & When
        val dependencies = dependencyManager.getDependencies(Manifest.permission.CAMERA)
        
        // Then
        assertNull(dependencies)
    }
    
    @Test
    fun `areDependenciesSatisfied should return true for permission without dependencies`() {
        // Given
        val grantedPermissions = arrayOf(Manifest.permission.CAMERA)
        
        // When
        val result = dependencyManager.areDependenciesSatisfied(
            Manifest.permission.CAMERA,
            grantedPermissions
        )
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `addDependency should add custom dependency relationship`() {
        // Given
        val dependentPermission = "custom.permission"
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        
        // When
        dependencyManager.addDependency(dependentPermission, requiredPermissions)
        val dependencies = dependencyManager.getDependencies(dependentPermission)
        
        // Then
        assertNotNull(dependencies)
        assertArrayEquals(requiredPermissions, dependencies)
    }
    
    @Test
    fun `areDependenciesSatisfied should check custom dependencies`() {
        // Given
        val dependentPermission = "custom.permission"
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        dependencyManager.addDependency(dependentPermission, requiredPermissions)
        
        val grantedPermissions = arrayOf(Manifest.permission.CAMERA) // Missing RECORD_AUDIO
        
        // When
        val result = dependencyManager.areDependenciesSatisfied(
            dependentPermission,
            grantedPermissions
        )
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getUnsatisfiedDependencies should return missing dependencies`() {
        // Given
        val dependentPermission = "custom.permission"
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        dependencyManager.addDependency(dependentPermission, requiredPermissions)
        
        val grantedPermissions = arrayOf(Manifest.permission.CAMERA) // Missing RECORD_AUDIO
        
        // When
        val unsatisfied = dependencyManager.getUnsatisfiedDependencies(
            dependentPermission,
            grantedPermissions
        )
        
        // Then
        assertEquals(1, unsatisfied.size)
        assertEquals(Manifest.permission.RECORD_AUDIO, unsatisfied[0])
    }
    
    @Test
    fun `resolveRequestOrder should order permissions by dependencies`() {
        // Given
        val dependentPermission = "custom.permission"
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
        dependencyManager.addDependency(dependentPermission, requiredPermissions)
        
        val permissions = arrayOf(dependentPermission, Manifest.permission.CAMERA)
        
        // When
        val orderedPermissions = dependencyManager.resolveRequestOrder(permissions)
        
        // Then
        assertEquals(2, orderedPermissions.size)
        assertEquals(Manifest.permission.CAMERA, orderedPermissions[0]) // Should come first
        assertEquals(dependentPermission, orderedPermissions[1]) // Should come second
    }
    
    @Test
    fun `validatePermissionCombination should identify missing dependencies`() {
        // Given
        val dependentPermission = "custom.permission"
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
        dependencyManager.addDependency(dependentPermission, requiredPermissions)
        
        val permissions = arrayOf(dependentPermission) // Missing CAMERA dependency
        
        // When
        val validation = dependencyManager.validatePermissionCombination(permissions)
        
        // Then
        assertFalse(validation.isValid)
        assertTrue(validation.issues.isNotEmpty())
        assertTrue(validation.suggestions.isNotEmpty())
    }
    
    @Test
    fun `validatePermissionCombination should pass for valid combination`() {
        // Given
        val dependentPermission = "custom.permission"
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
        dependencyManager.addDependency(dependentPermission, requiredPermissions)
        
        val permissions = arrayOf(dependentPermission, Manifest.permission.CAMERA) // All dependencies included
        
        // When
        val validation = dependencyManager.validatePermissionCombination(permissions)
        
        // Then
        assertTrue(validation.isValid)
        assertEquals(0, validation.issues.size)
        assertEquals(0, validation.suggestions.size)
    }
}