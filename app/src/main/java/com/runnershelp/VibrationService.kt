package com.runnershelp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.health.connect.datatypes.units.Power
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log


class VibrationService :Service () {
    private lateinit var vib: Vibrator
    private lateinit var vibrationObject: VibrationObject

    private lateinit var wakeLock : WakeLock

    override fun onCreate() {
        super.onCreate()
        vib = getSystemService(VIBRATOR_SERVICE) as Vibrator
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run { newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ":VibrationService") }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        val CHANNELID = "Foreground Service ID"
        val channel = NotificationChannel(
            CHANNELID,
            CHANNELID,
            NotificationManager.IMPORTANCE_HIGH
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification = Notification.Builder(this, CHANNELID)
            .setContentText("Vibration is Running")
            .setContentTitle("Runner's Vibration")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()

        if (action == "startVibration") {
            acquireWakeLock(wakeLock)
            // Extract the parameter (e.g., BPM) from the intent
            val bpm = intent.getIntExtra("bpm", 120)

            // Create a vibration effect based on the parameter
            vibrationObject = VibrationObject(bpm)
            val vibrationEffect = VibrationEffect.createWaveform(vibrationObject.getTimings(), 1)
            vib.vibrate(vibrationEffect)

            Log.i("Notification", "START")
            startForeground(1, notification)
        } else if (action == "stopVibration") {
            releaseWakeLock(wakeLock)
            // Stop the vibration
            vib.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }





        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        //releaseWakeLock(wakeLock)
        vib.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun acquireWakeLock(wakeLock: WakeLock) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
            updateUI("Wake Lock successfully acquired")
        } else {
            updateUI("Wake Lock is already acquired")
        }
    }

    private fun releaseWakeLock(wakeLock: WakeLock) {
        if (wakeLock.isHeld) {
            wakeLock.release()
            updateUI("Wake Lock successfully released")
        } else {
            updateUI("Wake Lock is not acquired")
        }
    }

    private fun updateUI(msg: String) {
        Log.i("WAKELOCK", msg)
    }

}