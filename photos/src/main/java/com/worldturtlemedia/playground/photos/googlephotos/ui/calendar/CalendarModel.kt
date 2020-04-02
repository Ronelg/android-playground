package com.worldturtlemedia.playground.photos.googlephotos.ui.calendar

import com.worldturtlemedia.playground.common.base.ui.viewmodel.State
import com.worldturtlemedia.playground.common.base.ui.viewmodel.StateViewModel
import com.worldturtlemedia.playground.common.core.SingleEvent
import org.joda.time.LocalDate

class CalendarModel : StateViewModel<CalendarState>(CalendarState()) {

    private val calendarCalculator = CalendarDaysCalculator.instance

    init {
        // TODO: Fetch media items from a "store", or just leave the calendar
        // TODO: blank, as its only a sample app
        setState {
            copy(
                scrollToBottom = SingleEvent(true),
                items = calendarCalculator.listOfDays
            )
        }
    }

    val calendarDates: Pair<LocalDate, LocalDate>
        get() = calendarCalculator.today to calendarCalculator.rangeBeginDate

    fun selectMediaForDate(date: LocalDate) = setState {
        if (!items.contains(date)) null
        else copy(navEvent = SingleEvent(date))
    }
}

data class CalendarState(
    val items: List<LocalDate> = emptyList(),
    val scrollToBottom: SingleEvent<Boolean>? = null,
    val navEvent: SingleEvent<LocalDate>? = null
) : State