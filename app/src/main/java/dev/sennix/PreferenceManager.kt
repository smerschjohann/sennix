package dev.sennix

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PreferenceManager(private val dataStore: DataStore<Preferences>) {

    val useImmichFrameDim: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[AppPreferences.USE_IMMICH_FRAME_DIM] ?: true // Default value
        }

    val motionSensorTimeout: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[AppPreferences.MOTION_SENSOR_TIMEOUT] ?: 5 // Default value (in minutes)
        }

    val acquireWakeLock: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[AppPreferences.ACQUIRE_WAKE_LOCK] ?: true // Default value
        }

    suspend fun setUseImmichFrameDim(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AppPreferences.USE_IMMICH_FRAME_DIM] = enabled
        }
    }

    suspend fun setMotionSensorTimeout(timeout: Int) {
        dataStore.edit { preferences ->
            preferences[AppPreferences.MOTION_SENSOR_TIMEOUT] = timeout
        }
    }

    suspend fun setAcquireWakeLock(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AppPreferences.ACQUIRE_WAKE_LOCK] = enabled
        }
    }
}