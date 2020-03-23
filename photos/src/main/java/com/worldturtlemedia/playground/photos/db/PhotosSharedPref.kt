package com.worldturtlemedia.playground.photos.db

import com.chibatching.kotpref.KotprefModel
import com.google.auth.oauth2.AccessToken
import com.worldturtlemedia.playground.photos.auth.data.token.AccessTokens
import org.joda.time.DateTime

object PhotosSharedPref : KotprefModel() {

    var refreshToken: String? by nullableStringPref()

    var accessToken: String? by nullableStringPref()

    var accessTokenExpiry: Long by longPref(-1L)
}

fun PhotosSharedPref.hasRefreshToken() = !refreshToken.isNullOrBlank()

fun PhotosSharedPref.hasValidToken() = retrieveTokens().hasValidToken

fun PhotosSharedPref.storeTokens(tokens: AccessTokens) {
    accessToken = tokens.accessToken
    accessTokenExpiry = tokens.expiry?.millis ?: -1
    refreshToken = refreshToken
}

fun PhotosSharedPref.retrieveTokens() = AccessTokens(
    accessToken = accessToken,
    expiry = accessTokenExpiry.takeIf { it > 0 }?.let { DateTime(it) },
    refreshToken = refreshToken
)