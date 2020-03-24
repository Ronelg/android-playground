package com.worldturtlemedia.playground.photos.auth.data.token

import com.google.auth.oauth2.AccessToken
import com.worldturtlemedia.playground.common.ktx.cast
import org.joda.time.DateTime

// TODO: We're going to have to refactor this to use refresh_tokens instead
data class AccessTokens(
    val accessToken: String?,
    val expiry: DateTime?,
    val refreshToken: String?
) {

    val accessTokenIfValid: AccessToken?
        get() {
            if (accessToken.isNullOrBlank() || expiry == null || expiry.isBeforeNow) return null

            return AccessToken(accessToken, expiry.toDate())
        }

    val hasValidToken: Boolean
        get() = /* !refreshToken.isNullOrBlank() || */ accessTokenIfValid != null

    companion object {

        private const val KEY_ACCESS_TOKEN = "accessToken"
        private const val KEY_EXPIRY = "expiry"
        private const val KEY_REFRESH_TOKEN = "refreshToken"

        fun from(data: HashMap<String, Any>): AccessTokens? {
            val refreshToken = data[KEY_REFRESH_TOKEN] as String?
            val accessToken = data[KEY_ACCESS_TOKEN] as String
            val expiry = data[KEY_EXPIRY]?.cast<Int>()
                ?.let { value -> DateTime.now().plusSeconds(value) }
                ?: return null

            return AccessTokens(accessToken, expiry, refreshToken)
        }
    }
}