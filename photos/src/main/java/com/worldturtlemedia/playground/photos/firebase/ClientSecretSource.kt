package com.worldturtlemedia.playground.photos.firebase

import com.github.ajalt.timberkt.e
import com.worldturtlemedia.playground.common.core.Result
import com.worldturtlemedia.playground.photos.db.PhotosSharedPref

class ClientSecretSource : FunctionFactory() {

    companion object {
        const val FUNCTION_GET_SECRET_KEY = "getAppSecretKey"

        val instance by lazy { ClientSecretSource() }
    }

    private val secretKey: String?
        get() = PhotosSharedPref.clientSecret

    suspend fun init() {
        if (secretKey == null) {
            getClientSecretKey()
        }
    }

    suspend fun getClientSecretKey(force: Boolean = false): String? {
        if (!force && !secretKey.isNullOrBlank()) return secretKey

        when (val result = safeCall<String>(FUNCTION_GET_SECRET_KEY)) {
            is Result.Success -> PhotosSharedPref.clientSecret = result.data
            is Result.Error -> {
                e(result.exception) { "Unable to fetch Secret key!" }
            }
        }

        return secretKey
    }
}