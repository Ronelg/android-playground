package com.worldturtlemedia.playground.common.ktx

fun <K, V> Map<K, V>.ensureKey(target: K, defaultValue: V): Map<K, V> = toMutableMap()
    .apply { if (!containsKey(target)) put(target, defaultValue) }
    .toMap()