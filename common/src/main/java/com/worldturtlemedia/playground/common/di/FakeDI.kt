package com.worldturtlemedia.playground.common.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * This is not for production...
 */
object FakeDI {

    private var _applicationContext: Context? = null
    val applicationContext: Context
        get() = _applicationContext
            ?: throw IllegalStateException("FakeDI.init was never called!")

    fun init(context: Context) {
        _applicationContext = context
    }

    var authenticatedUser: GoogleSignInAccount? = null
}