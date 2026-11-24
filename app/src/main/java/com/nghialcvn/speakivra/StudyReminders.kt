package com.nghialcvn.speakivra

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/** Local, opt-in study reminders. WorkManager survives app restarts and device reboot. */
internal object StudyReminders {
    private const val CHANNEL = "speakivra-study-reminder"
    private const val WORK = "speakivra-evening-practice"
    private const val ENABLED = "study_reminder_enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences("speakivra_progress", 0).getBoolean(ENABLED, false)

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun setEnabled(context: Context, enabled: Boolean) {
        if (enabled) require(hasPermission(context)) { "Chưa cấp quyền thông báo trên Android." }
        val app = context.applicationContext
        check(app.getSharedPreferences("speakivra_progress", 0).edit()
            .putBoolean(ENABLED, enabled).commit())
        if (!enabled) {
            WorkManager.getInstance(app).cancelUniqueWork(WORK)
            return
        }
        createChannel(app)
        val now = LocalDateTime.now()
        val tonight = now.toLocalDate().atTime(LocalTime.of(19, 0))
        val next = if (now.isBefore(tonight)) tonight else tonight.plusDays(1)
        val delay = Duration.between(now, next).toMinutes().coerceAtLeast(1)
        val work = PeriodicWorkRequestBuilder<StudyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .addTag("speakivra-study")
            .build()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, work)
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, "Nhắc luyện nói Speakivra",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Nhắc luyện nói tiếng Anh mỗi tối khi bạn đã bật lựa chọn"
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    @android.annotation.SuppressLint("MissingPermission") // Explicit runtime grant check below.
    fun deliver(context: Context) {
        if (!isEnabled(context) || !hasPermission(context)) return
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val action = PendingIntent.getActivity(context, 301, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Speakivra • Đến giờ luyện nói")
            .setContentText("Dành vài phút luyện tiếng Anh hôm nay nhé!")
            .setContentIntent(action)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(301, notification) }
    }
}

class StudyReminderWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    override fun doWork(): Result {
        StudyReminders.deliver(applicationContext)
        return Result.success()
    }
}
