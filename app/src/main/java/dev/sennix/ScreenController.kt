package dev.sennix

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

internal class ScreenControllerService : LifecycleService() {

    companion object {
        const val TAG = "ScreenControllerService"
        const val ACTION_WAKE_UP_SCREEN = "dev.sennix.action.WAKE_UP_SCREEN"
        const val ACTION_SLEEP_SCREEN = "dev.sennix.action.SLEEP_SCREEN"
        const val ACTION_UPDATE_SETTINGS = "dev.sennix.action.UPDATE_SETTINGS"
    }

    private lateinit var powerUtils: PowerUtils
    private lateinit var preferenceManager: PreferenceManager

    private var acquireWakeLock = true;
    private var useImmichFrameDim = true;

    private val okHttpClient = OkHttpClient()


    override fun onCreate() {
        super.onCreate()
        powerUtils = PowerUtils(this)

        preferenceManager = PreferenceManager(this.dataStore)
        updateSettings()
    }

    private fun updateSettings() {
        lifecycleScope.launch {
            this@ScreenControllerService.acquireWakeLock =  preferenceManager.acquireWakeLock.first()
            this@ScreenControllerService.useImmichFrameDim = preferenceManager.useImmichFrameDim.first()
        }
    }

    private suspend fun callImmichFrame(functionName: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("http://127.0.0.1:53287/" + functionName)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "HTTP GET /${functionName} Response status: ${response.code}")
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string()
                Log.d(TAG, "HTTP GET /${functionName} Response body: $responseBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error making HTTP GET request to /${functionName}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_WAKE_UP_SCREEN -> {
                    // Code to wake up the screen
                    Log.d(TAG, "Screen wake up requested")
                    if(acquireWakeLock) {
                        Log.d(TAG, "acquire wakelock")
                        powerUtils.wakeUp()
                    }
                    if(useImmichFrameDim) {
                        Log.d(TAG, "tell ImmichFrame to undim")
                        lifecycleScope.launch {
                            callImmichFrame("undim")
                        }
                    }
                }
                ACTION_SLEEP_SCREEN -> {
                    Log.d(TAG, "Screen sleep requested")
                    if(acquireWakeLock) {
                        Log.d(TAG, "release wakelock")
                        powerUtils.goToSleep()
                    }
                    if(useImmichFrameDim) {
                        Log.d(TAG, "tell ImmichFrame to dim")
                        lifecycleScope.launch {
                            callImmichFrame("dim")
                        }
                    }
                }
                ACTION_UPDATE_SETTINGS -> {
                    Log.d(TAG, "Settings update requested")
                    updateSettings()
                }
                else -> Log.d(TAG, "Unknown action: $action")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        okHttpClient.dispatcher.cancelAll()
        okHttpClient.connectionPool.evictAll()
    }
}
