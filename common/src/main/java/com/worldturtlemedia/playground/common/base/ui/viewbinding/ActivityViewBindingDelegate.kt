package com.worldturtlemedia.playground.common.base.ui.viewbinding

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.worldturtlemedia.playground.common.ktx.unsafeLazy

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = unsafeLazy { bindingInflater.invoke(layoutInflater) }