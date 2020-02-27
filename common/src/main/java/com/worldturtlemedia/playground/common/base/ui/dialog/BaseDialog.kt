package com.worldturtlemedia.playground.common.base.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.worldturtlemedia.playground.common.R
import com.worldturtlemedia.playground.common.ktx.simpleName

typealias OnConfirm<T> = (T) -> Unit
typealias OnCancel = () -> Unit
typealias OnDismiss = () -> Unit

abstract class BaseDialog<B : ViewBinding, T>(
    @LayoutRes private val layout: Int
) : AppCompatDialogFragment() {

    abstract val binding: B

    @StyleRes
    open val animationStyleRes: Int? = R.style.AlertDialogPopOutAnimation

    @DrawableRes
    open val backgroundDrawableRes: Int? = R.drawable.round_corners_white_layout

    open val isDismissible: Boolean = true

    protected val owner: LifecycleOwner
        get() = viewLifecycleOwner

    private var onConfirmListener: OnConfirm<T> = {}
    private var onCancelListener: OnCancel = {}
    private var onDismissListener: OnDismiss = {}

    private var shouldTriggerDismissListener: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isCancelable = isDismissible
        return inflater.inflate(layout, container, false)
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        withWindow {
            animationStyleRes?.let { attributes.windowAnimations = it }
            backgroundDrawableRes?.let { setBackgroundDrawableResource(it) }
        }

        setupViews()
        initViewModel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)


        if (shouldTriggerDismissListener) {
            onDismissListener()
        }
    }

    protected open fun setupViews() {}

    protected open fun initViewModel() {}

    protected fun withBinding(block: B.() -> Unit) {
        binding.apply(block)
    }

    fun onConfirm(listener: OnConfirm<T>) = apply { onConfirmListener = listener }

    fun onCancel(listener: OnCancel) = apply { onCancelListener = listener }

    fun onDismiss(listener: OnDismiss) = apply { onDismissListener = listener }

    fun show(fragmentManager: FragmentManager) = apply { show(fragmentManager, simpleName) }

    fun show(fragment: Fragment) = apply { show(fragment.childFragmentManager) }

    fun show(activity: FragmentActivity) = apply { show(activity.supportFragmentManager) }

    protected fun confirm(result: T) {
        onConfirmListener(result)
        silentClose()
    }

    fun cancel() {
        onCancelListener()
        silentClose()
    }

    fun close() {
        dismiss()
    }

    private fun silentClose() {
        shouldTriggerDismissListener = false
        close()
    }
}

fun DialogFragment.withWindow(block: Window.() -> Unit) {
    dialog?.window?.apply(block)
}