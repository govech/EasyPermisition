package com.cairong.permission.memory

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 内存泄漏检测器
 * 
 * 检测和防止权限框架中的内存泄漏
 */
object MemoryLeakDetector {
    
    private val trackedReferences = ConcurrentHashMap<String, WeakReference<Any>>()
    private val lifecycleObservers = ConcurrentHashMap<String, DefaultLifecycleObserver>()
    
    /**
     * 跟踪对象引用
     * 
     * @param key 唯一标识
     * @param obj 要跟踪的对象
     * @param lifecycleOwner 生命周期拥有者
     */
    fun trackReference(key: String, obj: Any, lifecycleOwner: LifecycleOwner) {
        trackedReferences[key] = WeakReference(obj)
        
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                cleanupReference(key)
                super.onDestroy(owner)
            }
        }
        
        lifecycleObservers[key] = observer
        lifecycleOwner.lifecycle.addObserver(observer)
    }
    
    /**
     * 清理引用
     */
    private fun cleanupReference(key: String) {
        trackedReferences.remove(key)
        lifecycleObservers.remove(key)
    }
    
    /**
     * 检查内存泄漏
     * 
     * @return 泄漏的对象列表
     */
    fun checkMemoryLeaks(): List<String> {
        val leaks = mutableListOf<String>()
        
        // 强制垃圾回收
        System.gc()
        System.runFinalization()
        
        // 等待一段时间让垃圾回收完成
        Thread.sleep(100)
        
        trackedReferences.forEach { (key, weakRef) ->
            if (weakRef.get() != null) {
                // 对象仍然存在，可能存在内存泄漏
                leaks.add(key)
            }
        }
        
        return leaks
    }
    
    /**
     * 获取当前跟踪的对象数量
     */
    fun getTrackedObjectCount(): Int {
        return trackedReferences.size
    }
    
    /**
     * 清理所有引用
     */
    fun clearAll() {
        trackedReferences.clear()
        lifecycleObservers.clear()
    }
    
    /**
     * 检查 Context 是否安全
     * 
     * @param context 要检查的 Context
     * @return 是否安全使用
     */
    fun isContextSafe(context: Context?): Boolean {
        if (context == null) return false
        
        return when (context) {
            is Activity -> !context.isDestroyed && !context.isFinishing
            else -> true
        }
    }
    
    /**
     * 检查 Fragment 是否安全
     * 
     * @param fragment 要检查的 Fragment
     * @return 是否安全使用
     */
    fun isFragmentSafe(fragment: Fragment?): Boolean {
        if (fragment == null) return false
        
        return fragment.isAdded && 
               !fragment.isDetached && 
               !fragment.isRemoving &&
               fragment.activity != null &&
               isContextSafe(fragment.activity)
    }
}

/**
 * 内存安全的回调包装器
 */
class MemorySafeCallback<T>(
    private val callback: T,
    private val contextProvider: () -> Context?
) {
    
    fun execute(action: T.() -> Unit) {
        if (MemoryLeakDetector.isContextSafe(contextProvider())) {
            callback.action()
        }
    }
}

/**
 * 内存安全的扩展函数
 */
fun <T> T.memorySafe(contextProvider: () -> Context?): MemorySafeCallback<T> {
    return MemorySafeCallback(this, contextProvider)
}