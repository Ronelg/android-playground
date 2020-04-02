package com.worldturtlemedia.playground.photos.googlephotos.ui.list.items

import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.MediaItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.PhotoItem
import com.worldturtlemedia.playground.photos.googlephotos.model.mediaitem.VideoItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import org.joda.time.LocalDate

class MediaItemListAdapter : GroupAdapter<GroupieViewHolder>() {

    private val sections = mutableListOf<MediaItemSection>()

    fun updateGroupedMediaItems(
        list: List<Pair<LocalDate, List<MediaItem>>>,
        onUpdate: () -> Unit = {}
    ) {
        val newSections = list.map { (date, itemsForDate) ->
            MediaItemSection(date, itemsForDate)
        }

        sections.clear()
        sections.addAll(newSections)
        updateAsync(sections) { onUpdate() }
    }

    fun getPositionOfTargetDate(date: LocalDate): Int? {
        val section = sections.find { it.date == date } ?: return null
        return getPositionOfSection(section)
    }

    private fun getPositionOfSection(section: MediaItemSection): Int? {
        return getAdapterPosition(section).takeIf { it > -1 }
    }
}

data class MediaItemSection(
    val date: LocalDate,
    val items: List<MediaItem>
) : Section() {

    private val isToday: Boolean
        get() = date.isEqual(LocalDate.now())

    init {
        if (isToday) {
            setPlaceholder(EmptyDayListItem(date.toString()))
        }

        setHideWhenEmpty(!isToday)
        setHeader(MediaItemListHeader(date = date))
        update(createListItems(items))
    }

    private fun createListItems(items: List<MediaItem>) = items.map { item ->
        when (item) {
            is VideoItem -> VideoListItem(item, false)
            is PhotoItem -> PhotoListItem(item, false)
        }
    }
}