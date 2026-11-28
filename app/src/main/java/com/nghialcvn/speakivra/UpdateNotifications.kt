package com.nghialcvn.speakivra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

internal object UpdateNotifications {
    const val OPEN_UPDATES = "open_updates"
    const val ID = 302
    private const val CHANNEL = "speakivra-app-updates"
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<AppUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("speakivra-check-updates", ExistingPeriodicWorkPolicy.UPDATE, request)
    }
    @android.annotation.SuppressLint("MissingPermission")
    @Synchronized fun deliver(context: Context, release: AppRelease?, current: Installation) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val prefs = context.getSharedPreferences("speakivra_update_notifications", 0)
        if (release == null || release.versionCode <= current.versionCode || !release.canInstallOver(current)) {
            // An older in-flight network response must not remove a newer offer.
            val shown = manager.activeNotifications.firstOrNull { it.id == ID }
            if (shown != null && shown.notification.extras.getLong("speakivra_release_code", 0) <= current.versionCode)
                manager.cancel(ID)
            return
        }
        if (!StudyReminders.hasPermission(context) || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Mới từ Speakivra", NotificationManager.IMPORTANCE_DEFAULT))
        if (manager.getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE) return
        if (prefs.getLong("notified_version", 0) >= release.versionCode) return
        val intent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .putExtra(OPEN_UPDATES, true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val action = PendingIntent.getActivity(context, ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val text = releaseHighlights(release.notes).joinToString(" • ") + "\nChạm để xem và cập nhật."
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_update_notification)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo_clear))
            .setContentTitle("Speakivra — Có bản cập nhật mới")
            .setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(action).setAutoCancel(true).setOnlyAlertOnce(true).build()
        notification.extras.putLong("speakivra_release_code", release.versionCode)
        NotificationManagerCompat.from(context).notify(ID, notification)
        prefs.edit().putLong("notified_version", release.versionCode).apply()
    }
}
class AppUpdateWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        val current = currentInstallation(applicationContext)
        val state = checkForUpdate(current)
        // A network error is not evidence that a previously available update disappeared.
        if (state.release == null) Result.retry() else {
            UpdateNotifications.deliver(applicationContext, state.release, current)
            Result.success()
        }
    } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
      catch (_: Exception) { Result.retry() }
}
