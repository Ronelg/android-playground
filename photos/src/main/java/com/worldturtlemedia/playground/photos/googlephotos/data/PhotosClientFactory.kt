package com.worldturtlemedia.playground.photos.googlephotos.data

import android.content.Context
import com.github.ajalt.timberkt.e
import com.github.ajalt.timberkt.i
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiException
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.UserCredentials
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import com.worldturtlemedia.playground.common.di.FakeDI
import com.worldturtlemedia.playground.common.ktx.emitAndLog
import com.worldturtlemedia.playground.photos.BuildConfig
import com.worldturtlemedia.playground.photos.auth.data.GoogleAuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class PhotosClientFactory(private val context: Context) {

    companion object Factory {
        val instance by lazy { PhotosClientFactory(FakeDI.applicationContext) }
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
}

internal fun createPhotosAPIClient(context: Context): PhotosLibraryClient? {
    val authState = GoogleAuthState.from(GoogleSignIn.getLastSignedInAccount(context))
    if (authState !is GoogleAuthState.Authenticated) return null

    return try {

        val credentials = UserCredentials.newBuilder()
            .setClientId(BuildConfig.GOOGLE_API_CLIENT_ID)
            .setClientSecret(BuildConfig.GOOGLE_API_CLIENT_SECRET)
            .setRefreshToken("1//0fIiTSkdKr7pmCgYIARAAGA8SNwF-L9IrnMxn03iLTETQPVhey6E37iVvlpo6cx9GQUgAtY8l3oj0bytM8CYOxxazm3F03cD0vbs")
//            .setAccessToken(AccessToken("ya29.a0Adw1xeV1ujIqjKWSfiOpMHf4Umhho--8XeBMdDq2t3pXOJmoWcWOGQVQHEUypfnWv8b8luuAxGc_vDB_VdPVvNK62xUaAPI03-EbRSNbnY15sChOuhfBwjxfkRSmemii7dGIEWI7lzKUGwpDxMJDDIe8CH5eEVw---E", null))
            .build()

        e { "server code: ${authState.serverCode}" }
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