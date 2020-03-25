package com.worldturtlemedia.playground.common.base.ui.groupie

import android.view.View
import androidx.viewbinding.ViewBinding
import com.xwray.groupie.Item

// TODO: Temporary until the ViewBinding functionality is merged into groupie
// see: https://github.com/lisawray/groupie/pull/325
abstract class ViewBindingItem<T : ViewBinding> : Item<ViewHolder<T>> {

    constructor() : super()

    constructor(id: Long) : super(id)

    override fun createViewHolder(itemView: View): ViewHolder<T> {
        return ViewHolder(inflate(itemView))
    }

    override fun bind(viewHolder: ViewHolder<T>, position: Int) {
        throw RuntimeException("Doesn't get called")
    }

    override fun bind(viewHolder: ViewHolder<T>, position: Int, payloads: MutableList<Any>) {
        bind(viewHolder.binding, position, payloads)
    }

    abstract fun inflate(itemView: View): T

    abstract fun bind(viewBinding: T, position: Int)

    private fun bind(viewBinding: T, position: Int, payloads: List<Any>) {
        bind(viewBinding, position)
    }
}