package com.worldturtlemedia.playground.photos.db

import com.chibatching.kotpref.KotprefModel

object PhotosSharedPref : KotprefModel() {

    var clientSecret: String? by nullableStringPref()

    var refreshToken: String? by nullableStringPref()
}