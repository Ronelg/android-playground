package com.worldturtlemedia.playground.photos.googlephotos.data

import android.content.Context
import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiException
import com.google.auth.oauth2.UserCredentials
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.common.ktx.emitAndLog
import com.worldturtlemedia.playground.photos.BuildConfig
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.auth.data.RefreshTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class PhotosClientFactory(
    private val context: Context,
    private val refreshTokenSource: RefreshTokenSource
) {

    companion object Factory {
        val instance by lazy {
            PhotosClientFactory(
                FakeDI.applicationContext,
                RefreshTokenSource.instance
            )
        }
    }

    private var _client: PhotosLibraryClient? = null
    val client: PhotosLibraryClient?
        get() = _client
            ?: createPhotosAPIClient(context).also { _client = it }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> safeApiCall(
        block: FlowCollector<PhotosResult<T>>.(client: PhotosLibraryClient) -> T
    ): Flow<PhotosResult<T>> = flow<PhotosResult<T>> {
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
    }.flowOn(Dispatchers.IO)

    private fun createPhotosAPIClient(context: Context): PhotosLibraryClient? {
        val authState = GoogleAuthState.from(GoogleSignIn.getLastSignedInAccount(context))
        if (authState !is GoogleAuthState.Authenticated) return null



        return try {

            e { "auth: ${GoogleSignIn.getLastSignedInAccount(context)?.id}" }

            val credentials = UserCredentials.newBuilder()
                .setClientId(BuildConfig.GOOGLE_API_CLIENT_ID)
                .setClientSecret(BuildConfig.GOOGLE_API_CLIENT_SECRET)
                .setRefreshToken("blah")
                .build()

            e { "server code: ${authState.getServerAuthCode(context)}" }
            val settings = PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build()

            PhotosLibraryClient.initialize(settings)
        } catch (error: IOException) {
            e(error) { "Failed to create the Photos client!" }
            null
        } catch (error: Throwable) {
            e(error) { "Generic error" }
            throw error
        }
    }
}
