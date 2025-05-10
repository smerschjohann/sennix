package dev.sennix

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kitesystems.nix.frame.MotionSensor
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class SensorService : LifecycleService() {
    @Volatile
    private var mServiceLooper: Looper? = null

    @Volatile
    private var mServiceHandler: ServiceHandler? = null
    private var mWakeLock: WakeLock? = null

    private lateinit var preferenceManager: PreferenceManager
    private val motionSensor = MotionSensor()

    @Volatile
    private var mIsRunning = false
    private var idleDuration = 0
    private var lastNotification: Long = 0
    private var sleepModeActive = false
    private var durationBeforeSleep: Int = 5 * 60 * 1000

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // Perform the task here
            performTask()

            // Schedule the next run if service is still running
            if (mIsRunning) {
                sendEmptyMessageDelayed(0, INTERVAL_MS) // Send message again after interval
            }
        }
    }


    override fun onCreate() {
        super.onCreate()

        preferenceManager = PreferenceManager(this.dataStore)
        lifecycleScope.launch {
            preferenceManager.motionSensorTimeout.collect { timeoutMinutes ->
                // Update durationBeforeSleep whenever the preference changes
                this@SensorService.durationBeforeSleep = timeoutMinutes * 60 * 1000
                Log.d(TAG, "Motion sensor timeout updated to: $timeoutMinutes minutes")
            }
        }

        Log.d(TAG, "Service creating...")

        val thread = HandlerThread(
            "ServiceStartArguments",
            10
        )
        thread.start()

        mServiceLooper = thread.getLooper()
        mServiceHandler = ServiceHandler(mServiceLooper!!)

        val pm = getSystemService(POWER_SERVICE) as PowerManager?
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService::WakeLockTag")
            acquirePartialWakeLock()
        }
    }

    private fun acquirePartialWakeLock() {
        if(mWakeLock != null && !mWakeLock!!.isHeld()) {
            mWakeLock!!.acquire(10 * 60 * 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service starting...")

        if (!mIsRunning) {
            mIsRunning = true
            mServiceHandler!!.sendEmptyMessage(0)
            Log.d(TAG, "Task loop started.")
        } else {
            Log.d(TAG, "Task loop already running.")
        }

        // If we get killed, after returning from here, restart
        // START_STICKY is suitable for services that are explicitly started
        // and stopped as needed. The system will try to re-create the service
        // after it has been killed.
        return START_STICKY
    }

    private fun performTask() {
        acquirePartialWakeLock()

        if (!motionSensor.hasMotionDetected()) {
            if (this.idleDuration % 30000 == 0) {
                Log.d(
                    TAG,
                    String.format(
                        "No activity detected for %d minutes, already in sleep mode: %b",
                        this.idleDuration / 1000 / 60,
                        sleepModeActive
                    )
                )
            }
            this.idleDuration += 1000

            val noMotionForFullDuration = this.idleDuration > this.durationBeforeSleep
            if (noMotionForFullDuration && !sleepModeActive) {
                Log.d(TAG, "-------Going to sleep--------")

                val intent = Intent(this, ScreenControllerService::class.java)
                intent.action = ScreenControllerService.ACTION_SLEEP_SCREEN
                startService(intent)

                sleepModeActive = true
            }
        } else {
            if (sleepModeActive || (System.currentTimeMillis() - this.lastNotification) > 30000) { // notify at least half a minute
                Log.d(TAG, "-------Waking up--------")
                lastNotification = System.currentTimeMillis()

                val intent = Intent(this, ScreenControllerService::class.java)
                intent.action = ScreenControllerService.ACTION_WAKE_UP_SCREEN
                startService(intent)

                sleepModeActive = false
            }
            this.idleDuration = 0
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "Service destroying...")
        // Signal the loop to stop
        mIsRunning = false
        // Remove any pending messages
        if (mServiceHandler != null) {
            mServiceHandler!!.removeCallbacksAndMessages(null)
        }

        // Quit the background thread gracefully
        if (mServiceLooper != null) {
            mServiceLooper!!.quitSafely() // or quit() if API < 18
        }

        // Release wake lock if held
        if (mWakeLock != null && mWakeLock!!.isHeld()) {
            try {
                mWakeLock!!.release()
            } catch (e: RuntimeException) {
                Log.e(TAG, "WakeLock release error during destroy", e)
            }
        }

        super.onDestroy()
        Log.d(TAG, "Service destroyed.")
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    companion object {
        const val TAG = "SensorService"
        private const val INTERVAL_MS: Long = 1000 // 1 second
    }
}