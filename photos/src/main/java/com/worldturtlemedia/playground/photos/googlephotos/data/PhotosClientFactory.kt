package com.worldturtlemedia.playground.photos.googlephotos.data

import android.content.Context
import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiException
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.UserCredentials
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.common.ktx.emitAndLog
import com.worldturtlemedia.playground.photos.BuildConfig
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

class PhotosClientFactory(private val context: Context) {

    companion object Factory {
        val instance by lazy { PhotosClientFactory(FakeDI.applicationContext) }
    }

    private var _client: PhotosLibraryClient? = null
    val client: PhotosLibraryClient?
        get() = _client
            ?: createPhotosAPIClient(context).also { _client = it }

    suspend fun <T> safeApiCall(
        block: FlowCollector<PhotosResult<T>>.(client: PhotosLibraryClient) -> T
    ): Flow<PhotosResult<T>> = withContext(Dispatchers.IO) {
        flow {
            val client = this@PhotosClientFactory.client
                ?: return@flow emitAndLog(PhotosResult.NotAuthenticated)

            emitAndLog(PhotosResult.Loading)

            try {
                val result = block.invoke(this, client)
                i { "Received: $result" }

                emitAndLog(PhotosResult.Success(result))
            } catch (error: ApiException) {
                emitAndLog(PhotosResult.Fail(error))
            }
        }
    }
}

internal fun createPhotosAPIClient(context: Context): PhotosLibraryClient? {
    val authState = GoogleAuthState.from(GoogleSignIn.getLastSignedInAccount(context))
    if (authState !is GoogleAuthState.Authenticated) return null

    val credentials = UserCredentials.newBuilder()
        .setClientId(BuildConfig.GOOGLE_API_AUTH_CODE)
        .setAccessToken(AccessToken(authState.serverCode, null))
        .build()

    val settings = PhotosLibrarySettings.newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
        .build()

    return try {
        PhotosLibraryClient.initialize(settings)
    } catch (error: IOException) {
        e(error) { "Failed to create the Photos client!" }
        null
    }
}