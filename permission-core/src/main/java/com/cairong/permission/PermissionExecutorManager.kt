package com.cairong.permission

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap

/**
 * 权限执行器管理器
 * 
 * 管理权限执行器的生命周期，确保在正确的时机创建和销毁
 */
object PermissionExecutorManager {
    
    private val executors = ConcurrentHashMap<String, PermissionRequestExecutor>()
    
    /**
     * 获取或创建权限执行器
     * 
     * @param activity Activity实例
     * @return 权限执行器
     */
    fun getOrCreateExecutor(activity: ComponentActivity): PermissionRequestExecutor {
        val key = "activity_${activity.hashCode()}"
        
        return executors.getOrPut(key) {
            // 确保在Activity创建时就初始化执行器
            val executor = try {
                PermissionRequestExecutor(activity)
            } catch (e: IllegalStateException) {
                // 如果Activity已经是RESUMED状态，我们需要延迟到下次onCreate时创建
                throw IllegalStateException(
                    "权限请求器必须在Activity的onCreate()方法中初始化。" +
                    "请在Activity的onCreate()方法中调用 PermissionManager.initialize(this)，" +
                    "或者使用 PermissionManager.withLazy(this) 进行延迟初始化。", e
                )
            }
            
            // 监听Activity生命周期，在销毁时清理执行器
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    executors.remove(key)
                    super.onDestroy(owner)
                }
            })
            
            executor
        }
    }
    
    /**
     * 获取或创建权限执行器
     * 
     * @param fragment Fragment实例
     * @return 权限执行器
     */
    fun getOrCreateExecutor(fragment: Fragment): PermissionRequestExecutor {
        val key = "fragment_${fragment.hashCode()}"
        
        return executors.getOrPut(key) {
            val executor = PermissionRequestExecutor(fragment)
            
            // 监听Fragment生命周期，在销毁时清理执行器
            fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    executors.remove(key)
                    super.onDestroy(owner)
                }
            })
            
            executor
        }
    }
    
    /**
     * 清理所有执行器
     */
    fun clearAll() {
        executors.clear()
    }
    
    /**
     * 获取当前执行器数量（用于调试）
     */
    fun getExecutorCount(): Int {
        return executors.size
    }
}