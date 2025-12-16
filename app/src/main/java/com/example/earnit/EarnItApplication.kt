package com.example.earnit

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.earnit.data.EarnItDatabase
import com.example.earnit.data.EarnItRepository
import com.example.earnit.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class EarnItApplication : Application() {
    // Lazy initialization of database and repository
    val database by lazy { EarnItDatabase.getDatabase(this) }
    val repository by lazy { EarnItRepository(database.dao()) }

    override fun onCreate() {
        super.onCreate()
        setupWorker()
    }

    private fun setupWorker() {
        // Run check every 12 hours
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PlantReminder",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            workRequest
        )
    }
}