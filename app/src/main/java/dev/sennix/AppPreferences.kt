package dev.sennix

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object AppPreferences {
    val USE_IMMICH_FRAME_DIM = booleanPreferencesKey("use_immich_frame_dim")
    val MOTION_SENSOR_TIMEOUT = intPreferencesKey("motion_sensor_timeout")
    val ACQUIRE_WAKE_LOCK = booleanPreferencesKey("acquire_wake_lock")
}