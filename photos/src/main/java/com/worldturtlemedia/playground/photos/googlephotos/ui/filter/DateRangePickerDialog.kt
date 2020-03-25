package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import androidx.core.util.Pair
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import org.joda.time.Interval
import kotlin.coroutines.resume

const val DIALOG_DATE_RANGE = "DIALOG_DATE_RANGE"

suspend fun showDateRangePickerDialog(fragmentManager: FragmentManager): Interval? {
    val dateRangeDialog = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date range")
        .build()

    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            dateRangeDialog.clearAllListeners()
            dateRangeDialog.dismissAllowingStateLoss()
        }

        val cancel = {
            dateRangeDialog.clearAllListeners()
            if (continuation.isActive) continuation.resume(null)
        }

        dateRangeDialog.apply {
            addOnDismissListener { cancel() }
            addOnNegativeButtonClickListener { cancel() }
            addOnPositiveButtonClickListener { (start, end) ->
                if (start == null || end == null) cancel()
                else {
                    clearAllListeners()
                    continuation.resume(Interval(start, end))
                }
            }
        }.show(fragmentManager, DIALOG_DATE_RANGE)
    }
}

private fun <T> MaterialDatePicker<T>.clearAllListeners() {
    clearOnCancelListeners()
    clearOnDismissListeners()
    clearOnNegativeButtonClickListeners()
    clearOnPositiveButtonClickListeners()
}

private operator fun <T1, T2> Pair<T1, T2>.component1() = first
private operator fun <T1, T2> Pair<T1, T2>.component2() = second