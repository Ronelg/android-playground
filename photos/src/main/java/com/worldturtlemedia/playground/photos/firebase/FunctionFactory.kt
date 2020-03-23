package com.worldturtlemedia.playground.photos.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.worldturtlemedia.playground.common.core.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

abstract class FunctionFactory {

    protected val functions by lazy { Firebase.functions }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected suspend fun <R> safeCall(function: String, data: Any? = null): Result<R> =
        suspendCancellableCoroutine { continuation ->
            functions
                .getHttpsCallable(function)
                .call(data)
                .addOnCompleteListener { task ->
                    continuation.resume(getResult(function, task))
                }
        }

    protected suspend fun hashMapCall(
        function: String,
        data: Any? = null
    ): Result<HashMap<String, Any>> = safeCall(function, data)

    private fun <R> getResult(function: String, task: Task<HttpsCallableResult>): Result<R> =
        if (task.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            Result.Success(task.result?.data as R)
        } else Result.Error(
            task.exception ?: Throwable("Unable to complete '$function`")
        )
}