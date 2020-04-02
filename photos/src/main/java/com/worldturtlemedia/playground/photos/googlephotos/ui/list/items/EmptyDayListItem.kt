package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import com.worldturtlemedia.playground.photos.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

data class EmptyDayListItem(private val date: String) : Item<GroupieViewHolder>() {

    override fun getLayout(): Int = R.layout.empty_day_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // No-op
    }

    override fun getId(): Long = date.hashCode().toLong()
}