package com.worldturtlemedia.playground.common.base.ui.util

import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

fun groupieAdapter(
    vararg group: Group,
    block: GroupAdapter<GroupieViewHolder>.() -> Unit = {}
): GroupAdapter<GroupieViewHolder> = GroupAdapter<GroupieViewHolder>().apply {
    group.forEach { add(it) }
    apply(block)
}