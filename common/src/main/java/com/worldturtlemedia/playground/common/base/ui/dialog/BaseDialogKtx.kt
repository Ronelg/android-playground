package com.worldturtlemedia.playground.common.base.ui.dialog

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun <B : ViewBinding, T> Fragment.showDialog(dialog: BaseDialog<B, T>) {
    dialog.show(this)
}

suspend fun <B : ViewBinding, T> Fragment.showDialogForResult(
    dialog: BaseDialog<B, T>
): DialogResult<T> = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { dialog.close() }

    dialog
        .onConfirm { continuation.resume(DialogResult.Confirmed(it)) }
        .onCancel { continuation.resume(DialogResult.Cancelled) }
        .show(this)
}

sealed class DialogResult<out T> {
    data class Confirmed<T>(val data: T) : DialogResult<T>()
    object Cancelled : DialogResult<Nothing>()
}