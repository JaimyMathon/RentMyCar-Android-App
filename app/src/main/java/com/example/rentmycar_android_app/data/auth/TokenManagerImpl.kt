package com.example.rentmycar_android_app.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {

    private object PreferencesKeys {
        val JWT_TOKEN = stringPreferencesKey("jwt_token")
        val USERNAME = stringPreferencesKey("username")
    }

    init {
        // Migration from SharedPreferences to DataStore will happen on first access
    }

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.JWT_TOKEN] = token
        }
    }

    override suspend fun getToken(): String? {
        // Migrate from SharedPreferences if needed
        migrateFromSharedPreferences()

        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.JWT_TOKEN]
        }.first()
    }

    override fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.JWT_TOKEN]
        }
    }

    override suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    override suspend fun getUsername(): String? {
        // Migrate from SharedPreferences if needed
        migrateFromSharedPreferences()

        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }.first()
    }

    override suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Migrate tokens from SharedPreferences to DataStore.
     * This ensures backward compatibility for existing users.
     */
    private suspend fun migrateFromSharedPreferences() {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("jwt_token", null)
        val username = sharedPrefs.getString("username", null)

        // Only migrate if we have data in SharedPreferences and nothing in DataStore
        if ((token != null || username != null) && getTokenFromDataStore() == null) {
            token?.let { saveToken(it) }
            username?.let { saveUsername(it) }

            // Clear SharedPreferences after successful migration
            sharedPrefs.edit().clear().apply()
        }
    }

    /**
     * Get token directly from DataStore without triggering migration
     */
    private suspend fun getTokenFromDataStore(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.JWT_TOKEN]
        }.first()
    }
}
