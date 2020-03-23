package com.worldturtlemedia.playground.photos.googlephotos.data

import android.content.Context
import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiException
import com.google.auth.oauth2.UserCredentials
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.common.ktx.emitAndLog
import com.worldturtlemedia.playground.photos.BuildConfig
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepo
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthRepoFactory
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import com.worldturtlemedia.playground.photos.config.PhotosConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

interface PhotosClient {
    suspend fun <T> safeApiCall(
        block: FlowCollector<PhotosResult<T>>.(client: PhotosLibraryClient) -> T
    ): Flow<PhotosResult<T>>
}

class PhotosClientFactory(
    private val context: Context,
    private val photosConfig: PhotosConfig,
    private val googleAuthRepo: GoogleAuthRepo
) : PhotosClient {

    companion object Factory {
        val instance by lazy {
            PhotosClientFactory(
                FakeDI.applicationContext,
                PhotosConfig.instance,
                GoogleAuthRepoFactory.instance
            )
        }
    }

    private var libraryClient: PhotosLibraryClient? = null

    private suspend fun ensureClient(): PhotosLibraryClient? {
        return libraryClient ?: createPhotosAPIClient(context)?.also { libraryClient = it }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun <T> safeApiCall(
        block: FlowCollector<PhotosResult<T>>.(client: PhotosLibraryClient) -> T
    ): Flow<PhotosResult<T>> = flow<PhotosResult<T>> {
        val client = ensureClient() ?: return@flow emitAndLog(PhotosResult.ClientFailure)

        emitAndLog(PhotosResult.Loading)

        try {
            val result = block.invoke(this, client)
            i { "Received: $result" }

            emitAndLog(PhotosResult.Success(result))
        } catch (error: ApiException) {
            emitAndLog(PhotosResult.Fail(error))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun createPhotosAPIClient(context: Context): PhotosLibraryClient? {
        val authState = googleAuthRepo.getPreviousAuthState(context)
        if (authState !is GoogleAuthState.Authenticated) return null

        return try {
            libraryClient = withContext(Dispatchers.IO) {
                val credentials = UserCredentials.newBuilder()
                    .setClientId(BuildConfig.GOOGLE_API_CLIENT_ID)
                    .setClientSecret(photosConfig.clientSecretKey)
                    .setAccessToken(authState.accessToken)
                    .build()

                e { "accessTokenIfValid: ${authState.accessToken}" }
                val settings = PhotosLibrarySettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build()

                PhotosLibraryClient.initialize(settings)
            }

            return libraryClient
        } catch (error: IOException) {
            e(error) { "Failed to create the Photos client!" }
            null
        } catch (error: Throwable) {
            e(error) { "Generic error" }
            throw error
        }
    }
}

