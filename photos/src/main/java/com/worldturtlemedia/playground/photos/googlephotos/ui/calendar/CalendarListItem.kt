package com.worldturtlemedia.playground.photos.googlephotos.ui.calendar

import android.view.View
import com.worldturtlemedia.playground.common.ktx.context
import com.worldturtlemedia.playground.common.ktx.string
import com.worldturtlemedia.playground.common.ktx.toUpperCase
import com.worldturtlemedia.playground.common.ktx.visibleOrGone
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.CalenderListItemBinding
import com.worldturtlemedia.playground.photos.databinding.CalenderListItemBinding.bind
import com.xwray.groupie.viewbinding.BindableItem
import org.joda.time.LocalDate

data class CalendarListItem(
    val date: LocalDate,
    private val isToday: Boolean
) : BindableItem<CalenderListItemBinding>() {

    override fun getLayout(): Int = R.layout.calender_list_item

    override fun initializeViewBinding(view: View): CalenderListItemBinding = bind(view)

    override fun bind(viewBinding: CalenderListItemBinding, position: Int) {
        with(viewBinding) {
            // TODO: Load thumbnail
            // TODO: Type indicator

            val isYearStart = !isToday && date.dayOfYear == 1
            val isMonthStart = !isToday && date.dayOfMonth == 1

            when {
                isYearStart -> txtMonthOrYear.text = date.year().asText
                isMonthStart -> txtMonthOrYear.text = date.monthOfYear().asShortText.toUpperCase()
                else -> {
                    txtMonthOrYear.text = ""
                    txtDayOfWeek.text =
                        if (isToday) context.string(R.string.today)
                        else date.dayOfWeek().asShortText.toUpperCase()

                    setDayOfMonthText()
                }
            }

            txtMonthOrYear.visibleOrGone = isYearStart || isMonthStart
            groupDayViews.visibleOrGone = isToday || (!isMonthStart && !isYearStart)
        }
    }

    private fun CalenderListItemBinding.setDayOfMonthText() {
        txtDayOfMonth.text = date.dayOfMonth.toString().padStart(2, '0')
    }

    override fun getId(): Long = date.toString().hashCode().toLong()
}

fun createCalendarListItems(list: List<LocalDate>, today: LocalDate) =
    list.map { item ->
        CalendarListItem(
            date = item,
            isToday = today.isEqual(item)
        )
    }