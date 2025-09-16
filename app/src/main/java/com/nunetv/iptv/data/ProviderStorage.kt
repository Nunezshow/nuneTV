package com.nunetv.iptv.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nunetv.iptv.model.ProviderCredentials
import org.json.JSONArray
import org.json.JSONObject

class ProviderStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveProvider(credentials: ProviderCredentials) {
        val providers = loadProviders().toMutableList()
        val existingIndex = providers.indexOfFirst { it.name.equals(credentials.name, ignoreCase = true) }
        if (existingIndex >= 0) {
            providers[existingIndex] = credentials
        } else {
            providers += credentials
        }
        writeProviders(providers)
    }

    fun deleteProvider(name: String) {
        val providers = loadProviders().filterNot { it.name.equals(name, ignoreCase = true) }
        writeProviders(providers)
        val active = loadActiveProviderName()
        if (active != null && active.equals(name, ignoreCase = true)) {
            saveActiveProvider(null)
        }
    }

    fun loadProviders(): List<ProviderCredentials> {
        val json = preferences.getString(KEY_PROVIDERS, null) ?: return emptyList()
        val array = JSONArray(json)
        val result = mutableListOf<ProviderCredentials>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            result += item.toCredentials()
        }
        return result
    }

    fun writeProviders(providers: List<ProviderCredentials>) {
        val array = JSONArray()
        providers.forEach { array.put(it.toJson()) }
        preferences.edit().putString(KEY_PROVIDERS, array.toString()).apply()
    }

    fun saveActiveProvider(name: String?) {
        preferences.edit().putString(KEY_ACTIVE_PROVIDER, name).apply()
    }

    fun loadActiveProviderName(): String? = preferences.getString(KEY_ACTIVE_PROVIDER, null)

    fun loadActiveProvider(): ProviderCredentials? {
        val name = loadActiveProviderName() ?: return null
        return loadProviders().firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    private fun ProviderCredentials.toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("portalUrl", portalUrl)
        put("username", username)
        put("password", password)
        put("m3uUrl", m3uUrl)
        put("epgUrl", epgUrl)
    }

    private fun JSONObject.toCredentials(): ProviderCredentials = ProviderCredentials(
        name = optString("name"),
        portalUrl = optString("portalUrl"),
        username = optString("username"),
        password = optString("password"),
        m3uUrl = optString("m3uUrl").takeIf { it.isNotBlank() },
        epgUrl = optString("epgUrl").takeIf { it.isNotBlank() }
    )

    companion object {
        private const val PREFS_NAME = "providers"
        private const val KEY_PROVIDERS = "providers_json"
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
    }
}
