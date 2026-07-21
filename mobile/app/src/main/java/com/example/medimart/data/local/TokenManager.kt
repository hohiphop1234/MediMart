package com.example.medimart.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    data class StoredSession(
        val accessToken: String,
        val refreshToken: String?
    )

    val sessionFlow: Flow<StoredSession?> = context.dataStore.data.map { preferences ->
        val accessToken = preferences[TOKEN_KEY]
        if (accessToken.isNullOrBlank()) {
            null
        } else {
            StoredSession(accessToken, preferences[REFRESH_TOKEN_KEY])
        }
    }

    val tokenFlow: Flow<String?> = sessionFlow.map { it?.accessToken }

    suspend fun saveSession(accessToken: String, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = accessToken
            if (refreshToken.isNullOrBlank()) {
                preferences.remove(REFRESH_TOKEN_KEY)
            } else {
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}
