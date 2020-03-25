package com.worldturtlemedia.playground.photos.googlephotos.model.filter

import com.google.photos.library.v1.proto.Filters

interface Filter {

    fun build(builder: Filters.Builder): Filters.Builder
}

fun List<Filter>.build(): Filters {
    val builder = Filters.newBuilder()
    forEach { filter -> filter.build(builder) }
    return builder.build()
}