package com.worldturtlemedia.playground.photos.googlephotos.model.filter

import com.google.photos.library.v1.proto.Filters
import com.google.photos.types.proto.DateRange
import com.google.type.Date
import com.worldturtlemedia.playground.common.ktx.createInterval
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate
import com.google.photos.library.v1.proto.DateFilter as GDateFilter

sealed class DateFilter : Filter {

    data class Year(val value: LocalDate) : DateFilter()
    data class YearMonth(val value: LocalDate) : DateFilter()
    data class YearMonthDay(val value: LocalDate) : DateFilter()
    data class Month(val value: LocalDate) : DateFilter()
    data class MonthDay(val value: LocalDate) : DateFilter()

    data class Range(val interval: Interval) : DateFilter() {

        override fun toString(): String {
            val start = interval.start
            val end = interval.end
            return "${start.monthOfYear().asShortText} ${start.dayOfMonth().asText}, ${start.year().asText} " +
                    "${end.monthOfYear().asShortText} ${end.dayOfMonth().asText}, ${end.year().asText} "
        }
    }

    override fun build(builder: Filters.Builder): Filters.Builder {
        val dateFilterBuilder = GDateFilter.newBuilder()
        if (this !is Range) dateFilterBuilder.addDates(buildDate(this))
        else {
            dateFilterBuilder.addRanges(
                DateRange.newBuilder()
                    .setStartDate(interval.start.toDateFilter())
                    .setEndDate(interval.end.toDateFilter())
            )
        }

        return builder.mergeDateFilter(dateFilterBuilder.build())
    }

    private fun buildDate(filter: DateFilter) = with(Date.newBuilder()) {
        when (filter) {
            is Year -> setYear(filter.value.year)
            is YearMonth -> setYear(filter.value.year).setMonth(filter.value.monthOfYear)
            is YearMonthDay -> setYear(filter.value.year)
                .setMonth(filter.value.monthOfYear)
                .setDay(filter.value.dayOfMonth)
            is Month -> setMonth(filter.value.monthOfYear)
            is MonthDay -> setMonth(filter.value.monthOfYear).setDay(filter.value.dayOfMonth)
            is Range -> this
        }
    }.build()

    private fun DateTime.toDateFilter() = buildDate(YearMonthDay(toLocalDate()))

    companion object {

        fun range(start: LocalDate, end: LocalDate) = DateFilter.Range(createInterval(start, end))
    }
}