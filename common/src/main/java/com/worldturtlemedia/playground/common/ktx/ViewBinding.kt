package com.worldturtlemedia.playground.common.ktx

import android.content.Context
import androidx.viewbinding.ViewBinding

val ViewBinding.context: Context
    get() = root.context