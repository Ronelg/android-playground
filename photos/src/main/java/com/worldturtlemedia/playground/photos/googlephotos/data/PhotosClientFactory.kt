package com.worldturtlemedia.playground.photos.googlephotos.data

import android.content.Context
import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiException
import com.google.api.gax.rpc.UnauthenticatedException
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.IOException

interface PhotosClient {
    suspend fun <T> safeApiCall(
        block: suspend (client: PhotosLibraryClient) -> T
    ): ApiResult<T>

    suspend fun <T> safeApiFlow(
        block: suspend FlowCollector<ApiResult<T>>.(client: PhotosLibraryClient) -> T
    ): Flow<ApiResult<T>>
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
        val authState = googleAuthRepo.state.firstOrNull()
        if (authState == null || authState is GoogleAuthState.Unauthenticated) {
            libraryClient = null
            return null
        }

        return libraryClient
            ?: createPhotosAPIClient(context)?.also { libraryClient = it }
    }

    override suspend fun <T> safeApiCall(
        block: suspend (client: PhotosLibraryClient) -> T
    ): ApiResult<T> {
        val client = ensureClient()
            ?: return ApiResult.Fail(ApiError.ClientFailure)

        return try {
            val result = block.invoke(client)
            i { "Received: $result" }

            ApiResult.Success(result)
        } catch (error: CancellationException) {
            e(error) { "Job was cancelled" }
            throw error
        } catch (error: UnauthenticatedException) {
            e(error) { "Unauthenticated!" }
            ApiResult.Fail(ApiError.Unauthenticated)
        } catch (error: ApiException) {
            e(error) { "Failed to make API request!" }
            ApiResult.Fail(ApiError.RequestFail)
        } catch (error: Throwable) {
            e(error) { "API Request failed for unknown reason" }
            ApiResult.Fail(ApiError.Error(error))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun <T> safeApiFlow(
        block: suspend FlowCollector<ApiResult<T>>.(client: PhotosLibraryClient) -> T
    ): Flow<ApiResult<T>> = flow<ApiResult<T>> {
        val result = safeApiCall { client ->
            emitAndLog(ApiResult.Loading)
            block.invoke(this, client)
        }

        emitAndLog(result)
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

suspend fun <T> FlowCollector<ApiResult<T>>.emitSuccess(data: T) {
    emit(ApiResult.Success(data))
}
