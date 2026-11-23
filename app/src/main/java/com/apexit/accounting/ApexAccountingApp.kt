package com.apexit.accounting

import android.app.Application
import androidx.room.Room

class ApexAccountingApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "apex_accounting.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}
