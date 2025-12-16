package com.example.earnit.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.earnit.MainActivity
import com.example.earnit.R
import com.example.earnit.EarnItApplication
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as EarnItApplication
        val repository = application.repository

        // Get Plant Data
        val plant = repository.plantState.first()
        val tasks = repository.tasks.first()

        // Logic: Notify if plant is alive, stage > 0, and user hasn't watered today
        // (Checking lastWateredDate vs Today)
        val today = System.currentTimeMillis()
        val lastWatered = plant.lastWateredDate
        
        // Simple check: If difference is > 20 hours (approx a day)
        val diff = today - lastWatered
        val needsWater = diff > (20 * 60 * 60 * 1000) 

        if (needsWater && !plant.isDead && plant.stage > 0) {
            sendNotification("Your Plant needs you!", "Don't let your plant wilt. Complete your quests!")
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "earnit_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Uses your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}