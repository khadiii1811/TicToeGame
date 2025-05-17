package com.example.tictoe.utils

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserStats::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
}