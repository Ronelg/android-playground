package com.worldturtlemedia.playground.photos.auth.data.token

import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.core.Result
import com.worldturtlemedia.playground.photos.db.PhotosSharedPref
import com.worldturtlemedia.playground.photos.db.retrieveTokens
import com.worldturtlemedia.playground.photos.db.storeTokens
import com.worldturtlemedia.playground.photos.firebase.FunctionFactory

class AccessTokenSource : FunctionFactory() {

    companion object {

        const val FUNCTION_GET_ACCESS_TOKEN = "getAccessToken"
        const val KEY_SERVER_AUTH_CODE = "serverAuthCode"

        val instance by lazy { AccessTokenSource() }
    }

    suspend fun requestAccessToken(serverAuthCode: String, force: Boolean = false): AccessTokens {
        val tokens = PhotosSharedPref.retrieveTokens()
        if (!force && tokens.hasValidToken) return tokens

        val params = hashMapOf(KEY_SERVER_AUTH_CODE to serverAuthCode)
        when (val result = hashMapCall(FUNCTION_GET_ACCESS_TOKEN, params)) {
            is Result.Error -> e(result.exception) { "Unable to fetch access key!" }
            is Result.Success -> AccessTokens.from(result.data)?.also { data ->
                PhotosSharedPref.storeTokens(data)
            }
        }

        return PhotosSharedPref.retrieveTokens()
    }
}