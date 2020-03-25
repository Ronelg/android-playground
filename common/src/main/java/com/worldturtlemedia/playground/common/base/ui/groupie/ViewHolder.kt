package com.worldturtlemedia.playground.common.base.ui.groupie

import androidx.viewbinding.ViewBinding
import com.xwray.groupie.GroupieViewHolder

class ViewHolder<T : ViewBinding>(val binding: T) : GroupieViewHolder(binding.root)