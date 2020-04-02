package com.worldturtlemedia.playground.photos.googlephotos.model.filter

import com.google.photos.library.v1.proto.Filters

interface Filter {

    fun build(builder: Filters.Builder): Filters.Builder
}

fun List<Filter>.build(builder: Filters.Builder = Filters.newBuilder()): Filters {
    forEach { filter -> filter.build(builder) }
    return builder.build()
}

fun buildFilters(vararg filters: Filter): Filters {
    val builder = Filters.newBuilder()
    filters.forEach { it.build(builder) }
    return builder.build()
}