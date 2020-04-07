package com.worldturtlemedia.playground.common.ktx

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate

fun createInterval(start: DateTime, end: DateTime) = Interval(start, end)

fun createInterval(start: LocalDate, end: LocalDate): Interval {
    return createInterval(start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay())
}

fun LocalDate.isEqualOrBefore(date: LocalDate) = isEqual(date) || isBefore(date)

fun LocalDate.isEqualOrAfter(date: LocalDate) = isEqual(date) || isAfter(date)