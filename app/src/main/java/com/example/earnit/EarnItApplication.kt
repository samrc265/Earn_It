package com.example.earnit

import android.app.Application
import com.example.earnit.data.EarnItDatabase
import com.example.earnit.data.EarnItRepository

class EarnItApplication : Application() {
    // Lazy initialization of database and repository
    val database by lazy { EarnItDatabase.getDatabase(this) }
    val repository by lazy { EarnItRepository(database.dao()) }
}