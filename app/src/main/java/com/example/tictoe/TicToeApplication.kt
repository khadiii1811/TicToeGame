package com.example.tictoe

import android.app.Application
import android.util.Log
import com.example.tictoe.model.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TicToeApplication : Application() {
    // Application scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Lazy initialize repositories
    val statsRepository: StatsRepository by lazy { 
        StatsRepository.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        
        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> 
            Log.e("TicToe", "Unhandled exception: ${throwable.message}", throwable)
        }
        
        Log.d("TicToe", "Application initialized")
    }
    
    override fun onTerminate() {
        super.onTerminate()
    }
} 