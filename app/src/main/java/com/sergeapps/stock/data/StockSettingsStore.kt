package com.sergeapps.stock.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "stock_settings")

data class StockSettings(
    val baseUrl: String,
    val port: String,
    val deviceId: String,
    val apiKey: String
) {
    companion object {
        val Default = StockSettings(
            baseUrl = "https://homeapi.ddns.net",
            port = "443",
            deviceId = "",
            apiKey = ""
        )
    }
}

class StockSettingsStore(private val context: Context) {

    private object Keys {
        val BaseUrl = stringPreferencesKey("base_url")
        val Port = stringPreferencesKey("port")
        val DeviceId = stringPreferencesKey("device_id")
        val ApiKey = stringPreferencesKey("api_key")
    }

    val settingsFlow: Flow<StockSettings> =
        context.dataStore.data.map { prefs ->
            StockSettings(
                baseUrl = prefs[Keys.BaseUrl] ?: StockSettings.Default.baseUrl,
                port = prefs[Keys.Port] ?: StockSettings.Default.port,
                deviceId = prefs[Keys.DeviceId] ?: StockSettings.Default.deviceId,
                apiKey = prefs[Keys.ApiKey] ?: StockSettings.Default.apiKey
            )
        }

    suspend fun save(newSettings: StockSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BaseUrl] = newSettings.baseUrl
            prefs[Keys.Port] = newSettings.port
            prefs[Keys.DeviceId] = newSettings.deviceId
            prefs[Keys.ApiKey] = newSettings.apiKey
        }
    }
}
