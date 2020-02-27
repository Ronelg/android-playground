package com.worldturtlemedia.playground

import android.util.Log
import com.google.android.play.core.splitcompat.SplitCompatApplication
import timber.log.Timber

class PlaygroundApp : SplitCompatApplication() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else releaseTree)
    }

    private val releaseTree = object: Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            Log.println(priority, tag, t?.let { "$message\n$it" } ?: message)
        }
    }
}