package com.trkmn.flora.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.trkmn.flora.BuildConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {
    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        remoteConfig.setDefaultsAsync(
            mapOf(
                "gemini_api_key" to ""
            )
        )
    }

    suspend fun getGeminiApiKey(): String {
        try {
            remoteConfig.fetchAndActivate().await()
            return remoteConfig.getString("gemini_api_key")
        } catch (e: Exception) {
            throw Exception("API anahtarı alınamadı: ${e.message}")
        }
    }
} 