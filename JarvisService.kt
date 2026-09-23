package com.jarvis.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class JarvisService : Service() {

    companion object {

        private const val CHANNEL_ID =
            "jarvis_assistant_channel"

        private const val CHANNEL_NAME =
            "JARVIS Assistant"

        private const val NOTIFICATION_ID =
            1001

        @Volatile
        private var running = false

        // ==========================================
        // START SERVICE
        // ==========================================

        fun start(context: Context) {

            val intent = Intent(
                context,
                JarvisService::class.java
            )

            try {

                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
                ) {

                    ContextCompat.startForegroundService(
                        context,
                        intent
                    )

                } else {

                    context.startService(intent)
                }

            } catch (_: Exception) {
                // Service start failed
            }
        }

        // ==========================================
        // STOP SERVICE
        // ==========================================

        fun stop(context: Context) {

            val intent = Intent(
                context,
                JarvisService::class.java
            )

            try {

                context.stopService(intent)

            } catch (_: Exception) {
                // Ignore
            }
        }

        // ==========================================
        // CHECK STATUS
        // ==========================================

        fun isRunning(): Boolean {
            return running
        }
    }

    // ==========================================
    // SERVICE CREATED
    // ==========================================

    override fun onCreate() {

        super.onCreate()

        running = true

        createNotificationChannel()

        val notification =
            createNotification()

        startForeground(
            NOTIFICATION_ID,
            notification
        )
    }

    // ==========================================
    // SERVICE STARTED
    // ==========================================

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        running = true

        /*
         * IMPORTANT:
         *
         * Abhi yahan actual microphone
         * listening logic nahi hai.
         *
         * Phase 3/4 mein yahan:
         *
         * Voice Recognition
         * Wake Word
         * Voice Authentication
         * Command Router
         *
         * connect kiye jayenge.
         */

        return START_STICKY
    }

    // ==========================================
    // CREATE NOTIFICATION CHANNEL
    // ==========================================

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {

                description =
                    "JARVIS background assistant service"

                setShowBadge(false)
            }

            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                channel
            )
        }
    }

    // ==========================================
    // CREATE FOREGROUND NOTIFICATION
    // ==========================================

    private fun createNotification(): Notification {

        val openIntent = Intent(
            this,
            MainActivity::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                openIntent,
                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.M
                ) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                }
            )

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "JARVIS is active"
            )
            .setContentText(
                "Assistant service is running"
            )
            .setSmallIcon(
                android.R.drawable.ic_btn_speak_now
            )
            .setContentIntent(
                pendingIntent
            )
            .setOngoing(true)
            .setCategory(
                NotificationCompat.CATEGORY_SERVICE
            )
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .build()
    }

    // ==========================================
    // SERVICE DESTROYED
    // ==========================================

    override fun onDestroy() {

        running = false

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
            // Ignore
        }

        super.onDestroy()
    }

    // ==========================================
    // BINDING
    // ==========================================

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
