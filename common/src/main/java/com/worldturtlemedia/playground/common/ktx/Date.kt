package com.worldturtlemedia.playground.common.ktx

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate

fun createInterval(start: DateTime, end: DateTime) = Interval(start, end)

fun createInterval(start: LocalDate, end: LocalDate): Interval {
    return createInterval(start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay())
}