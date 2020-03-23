package com.worldturtlemedia.playground.photos.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.worldturtlemedia.playground.photos.BuildConfig

class PhotosConfig {

    companion object {

        const val KEY_CLIENT_SECRET = "client_secret"

        val instance by lazy { PhotosConfig() }
    }

    private val remoteConfig = Firebase.remoteConfig

    private val configSettings = remoteConfigSettings {
        if (BuildConfig.DEBUG) {
            minimumFetchIntervalInSeconds = 0
        }
    }

    init {
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
    }

    val clientSecretKey: String
        get() = remoteConfig.getString(KEY_CLIENT_SECRET)
}