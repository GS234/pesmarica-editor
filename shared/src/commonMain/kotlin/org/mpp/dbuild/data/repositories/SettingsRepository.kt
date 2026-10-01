package org.mpp.dbuild.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mpp.dbuild.data.datastore.PrefsDataStore

class SettingsRepository(private val dataStore: PrefsDataStore) {
    private object PreferencesKeys {
        val DB_PATH = stringPreferencesKey("db_path")
    }

    // Expose settings properties as Flows
    val dbPath: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DB_PATH] ?: "" // default fallback
    }

    // Suspend functions to asynchronously write modifications
    suspend fun setDbPath(path: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DB_PATH] = path
        }
    }
}