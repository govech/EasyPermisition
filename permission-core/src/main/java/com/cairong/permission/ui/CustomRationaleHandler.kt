package com.cairong.permission.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.cairong.permission.PermissionRequest
import com.cairong.permission.RationaleCallback
import com.cairong.permission.RationaleHandler

/**
 * 自定义权限解释处理器
 * 
 * 支持自定义布局和主题
 */
class CustomRationaleHandler(
    private val layoutResId: Int = 0,
    private val themeResId: Int = 0,
    private val customViewProvider: ((Context, PermissionRequest) -> View)? = null
) : RationaleHandler {
    
    override fun showRationale(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    ) {
        when {
            customViewProvider != null -> {
                showCustomViewDialog(context, request, callback)
            }
            layoutResId != 0 -> {
                showCustomLayoutDialog(context, request, callback)
            }
            else -> {
                showDefaultDialog(context, request, callback)
            }
        }
    }
    
    /**
     * 显示自定义视图对话框
     */
    private fun showCustomViewDialog(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    ) {
        val customView = customViewProvider!!.invoke(context, request)
        
        val builder = if (themeResId != 0) {
            AlertDialog.Builder(context, themeResId)
        } else {
            AlertDialog.Builder(context)
        }
        
        val dialog = builder.setView(customView)
            .setCancelable(false)
            .create()
        
        // 查找按钮并设置点击事件
        customView.findViewById<Button>(android.R.id.button1)?.setOnClickListener {
            dialog.dismiss()
            callback.onContinue()
        }
        
        customView.findViewById<Button>(android.R.id.button2)?.setOnClickListener {
            dialog.dismiss()
            callback.onCancel()
        }
        
        dialog.show()
    }
    
    /**
     * 显示自定义布局对话框
     */
    private fun showCustomLayoutDialog(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    ) {
        val inflater = LayoutInflater.from(context)
        val customView = inflater.inflate(layoutResId, null)
        
        // 设置标题和消息
        customView.findViewById<TextView>(android.R.id.title)?.text = 
            request.rationaleTitle ?: "权限说明"
        customView.findViewById<TextView>(android.R.id.message)?.text = 
            request.rationale ?: "应用需要相关权限才能正常工作"
        
        val builder = if (themeResId != 0) {
            AlertDialog.Builder(context, themeResId)
        } else {
            AlertDialog.Builder(context)
        }
        
        val dialog = builder.setView(customView)
            .setCancelable(false)
            .create()
        
        // 设置按钮点击事件
        customView.findViewById<Button>(android.R.id.button1)?.apply {
            text = request.positiveButtonText
            setOnClickListener {
                dialog.dismiss()
                callback.onContinue()
            }
        }
        
        customView.findViewById<Button>(android.R.id.button2)?.apply {
            text = request.negativeButtonText
            setOnClickListener {
                dialog.dismiss()
                callback.onCancel()
            }
        }
        
        dialog.show()
    }
    
    /**
     * 显示默认对话框
     */
    private fun showDefaultDialog(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    ) {
        val title = request.rationaleTitle ?: "权限说明"
        val message = request.rationale ?: "应用需要相关权限才能正常工作"
        
        val builder = if (themeResId != 0) {
            AlertDialog.Builder(context, themeResId)
        } else {
            AlertDialog.Builder(context)
        }
        
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(request.positiveButtonText) { dialog, _ ->
                dialog.dismiss()
                callback.onContinue()
            }
            .setNegativeButton(request.negativeButtonText) { dialog, _ ->
                dialog.dismiss()
                callback.onCancel()
            }
            .setCancelable(false)
            .show()
    }
}

/**
 * DialogFragment版本的权限解释处理器
 * 
 * 适用于需要更复杂UI交互的场景
 */
abstract class RationaleDialogFragment : DialogFragment() {
    
    protected var callback: RationaleCallback? = null
    protected var request: PermissionRequest? = null
    
    /**
     * 设置权限请求信息和回调
     */
    fun setup(request: PermissionRequest, callback: RationaleCallback) {
        this.request = request
        this.callback = callback
    }
    
    /**
     * 用户选择继续
     */
    protected fun onContinue() {
        callback?.onContinue()
        dismiss()
    }
    
    /**
     * 用户选择取消
     */
    protected fun onCancel() {
        callback?.onCancel()
        dismiss()
    }
}

/**
 * DialogFragment权限解释处理器
 */
class DialogFragmentRationaleHandler(
    private val dialogFragmentClass: Class<out RationaleDialogFragment>
) : RationaleHandler {
    
    override fun showRationale(
        context: Context,
        request: PermissionRequest,
        callback: RationaleCallback
    ) {
        if (context !is FragmentActivity) {
            // 如果不是FragmentActivity，回退到默认处理器
            com.cairong.permission.DefaultRationaleHandler().showRationale(context, request, callback)
            return
        }
        
        try {
            val dialogFragment = dialogFragmentClass.newInstance()
            dialogFragment.setup(request, callback)
            dialogFragment.show(context.supportFragmentManager, "permission_rationale")
        } catch (e: Exception) {
            // 如果创建DialogFragment失败，回退到默认处理器
            com.cairong.permission.DefaultRationaleHandler().showRationale(context, request, callback)
        }
    }
}