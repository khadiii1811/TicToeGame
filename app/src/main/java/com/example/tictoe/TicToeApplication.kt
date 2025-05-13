package com.example.tictoe

import android.app.Application
import android.util.Log
import com.example.tictoe.model.StatsRepository

class TicToeApplication : Application() {
    // Lazy initialize stats repository
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
} 