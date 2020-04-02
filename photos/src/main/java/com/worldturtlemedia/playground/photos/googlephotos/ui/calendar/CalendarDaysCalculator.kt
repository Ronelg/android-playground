package com.worldturtlemedia.playground.photos.googlephotos.ui.calendar

import com.worldturtlemedia.playground.photos.googlephotos.ui.Constants
import org.joda.time.Days
import org.joda.time.LocalDate

class CalendarDaysCalculator(
    val today: LocalDate = LocalDate.now(),
    initialDate: LocalDate = defaultStartTime
) {

    private val totalDays: Int

    val rangeBeginDate: LocalDate

    init {
        val daysBetween = Days.daysBetween(initialDate, today).days + 1
        val daysMissingForCompleteWeek = daysBetween % Constants.NUMBER_OF_COLUMNS

        val prependDays =
            if (daysMissingForCompleteWeek == 0) 0
            else Constants.NUMBER_OF_COLUMNS - daysMissingForCompleteWeek


        totalDays = prependDays + daysBetween
        rangeBeginDate = initialDate.minusDays(prependDays)
    }

    val listOfDays by lazy {
        (0 until totalDays).map { rangeBeginDate.plusDays(it) }
    }

    companion object {

        val instance by lazy { CalendarDaysCalculator() }

        private val defaultStartTime = LocalDate.now().minusYears(1)
    }
}