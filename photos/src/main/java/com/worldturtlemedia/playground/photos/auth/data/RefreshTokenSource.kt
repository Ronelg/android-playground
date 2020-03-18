package com.worldturtlemedia.playground.photos.auth.data

import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.core.Result
import com.worldturtlemedia.playground.photos.db.PhotosSharedPref
import com.worldturtlemedia.playground.photos.firebase.FunctionFactory

class RefreshTokenSource : FunctionFactory() {

    companion object {
        const val FUNCTION_GET_REFRESH_TOKEN = "getRefreshToken"

        val instance by lazy { RefreshTokenSource() }
    }

    val refreshToken: String?
        get() = PhotosSharedPref.refreshToken

    suspend fun requestRefreshToken(
        userId: String,
        serverAuthCode: String,
        force: Boolean = false
    ): String? {
        if (!force && !refreshToken.isNullOrBlank()) return refreshToken

        val data = hashMapOf(
            "userId" to userId,
            "serverAuthCode" to serverAuthCode
        )

        when (val result = safeCall<String>(FUNCTION_GET_REFRESH_TOKEN, data)) {
            is Result.Success -> PhotosSharedPref.refreshToken = result.data
            is Result.Error -> {
                e(result.exception) { "Unable to fetch refresh key!" }
            }
        }

        return refreshToken
    }
}