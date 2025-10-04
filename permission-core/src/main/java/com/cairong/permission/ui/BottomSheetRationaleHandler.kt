package com.cairong.permission.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.cairong.permission.PermissionRequest
import com.cairong.permission.RationaleCallback
import com.cairong.permission.RationaleHandler
import com.cairong.permission.DefaultRationaleHandler
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheet版本的权限解释处理器
 * 
 * 提供更现代化的UI体验
 */
class BottomSheetRationaleHandler(
    private val layoutResId: Int = 0,
    private val bottomSheetClass: Class<out RationaleBottomSheetFragment>? = null
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
            val bottomSheet = if (bottomSheetClass != null) {
                bottomSheetClass.newInstance()
            } else {
                DefaultRationaleBottomSheetFragment()
            }
            
            bottomSheet.setup(request, callback, layoutResId)
            bottomSheet.show(context.supportFragmentManager, "permission_rationale_bottom_sheet")
        } catch (e: Exception) {
            // 如果创建BottomSheet失败，回退到默认处理器
            com.cairong.permission.DefaultRationaleHandler().showRationale(context, request, callback)
        }
    }
}

/**
 * 权限解释BottomSheet基类
 */
abstract class RationaleBottomSheetFragment : BottomSheetDialogFragment() {
    
    protected var callback: RationaleCallback? = null
    protected var request: PermissionRequest? = null
    protected var customLayoutResId: Int = 0
    
    /**
     * 设置权限请求信息和回调
     */
    fun setup(request: PermissionRequest, callback: RationaleCallback, layoutResId: Int = 0) {
        this.request = request
        this.callback = callback
        this.customLayoutResId = layoutResId
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
    
    /**
     * 设置权限信息到视图
     */
    protected fun setupPermissionInfo(view: View) {
        val request = this.request ?: return
        
        view.findViewById<TextView>(android.R.id.title)?.text = 
            request.rationaleTitle ?: "权限说明"
        view.findViewById<TextView>(android.R.id.message)?.text = 
            request.rationale ?: "应用需要相关权限才能正常工作"
        
        view.findViewById<Button>(android.R.id.button1)?.apply {
            text = request.positiveButtonText
            setOnClickListener { onContinue() }
        }
        
        view.findViewById<Button>(android.R.id.button2)?.apply {
            text = request.negativeButtonText
            setOnClickListener { onCancel() }
        }
    }
}

/**
 * 默认的权限解释BottomSheet实现
 */
class DefaultRationaleBottomSheetFragment : RationaleBottomSheetFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 如果有自定义布局，使用自定义布局
        if (customLayoutResId != 0) {
            val view = inflater.inflate(customLayoutResId, container, false)
            setupPermissionInfo(view)
            return view
        }
        
        // 否则创建默认布局
        return createDefaultLayout(inflater, container)
    }
    
    /**
     * 创建默认布局
     */
    private fun createDefaultLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        // 这里应该创建一个默认的BottomSheet布局
        // 为了简化，我们创建一个简单的线性布局
        val context = requireContext()
        val linearLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        
        // 标题
        val titleView = TextView(context).apply {
            id = android.R.id.title
            text = request?.rationaleTitle ?: "权限说明"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }
        linearLayout.addView(titleView)
        
        // 消息
        val messageView = TextView(context).apply {
            id = android.R.id.message
            text = request?.rationale ?: "应用需要相关权限才能正常工作"
            textSize = 14f
            setPadding(0, 0, 0, 32)
        }
        linearLayout.addView(messageView)
        
        // 按钮容器
        val buttonContainer = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }
        
        // 取消按钮
        val negativeButton = Button(context).apply {
            id = android.R.id.button2
            text = request?.negativeButtonText ?: "取消"
            setOnClickListener { onCancel() }
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply {
                setMargins(0, 0, 16, 0)
            }
        }
        buttonContainer.addView(negativeButton)
        
        // 确认按钮
        val positiveButton = Button(context).apply {
            id = android.R.id.button1
            text = request?.positiveButtonText ?: "确定"
            setOnClickListener { onContinue() }
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        buttonContainer.addView(positiveButton)
        
        linearLayout.addView(buttonContainer)
        
        return linearLayout
    }
}