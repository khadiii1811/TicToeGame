package com.example.tictoe.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Repository for managing game statistics data and persistence
 */
class StatsRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    // Statistics state flows
    private val _wins = MutableStateFlow(0)
    val wins: Flow<Int> = _wins.asStateFlow()
    
    private val _draws = MutableStateFlow(0)
    val draws: Flow<Int> = _draws.asStateFlow()
    
    private val _losses = MutableStateFlow(0)
    val losses: Flow<Int> = _losses.asStateFlow()
    
    // Initialize by loading data from shared preferences
    init {
        loadStats()
    }
    
    // Load statistics from SharedPreferences
    fun loadStats() {
        _wins.value = sharedPreferences.getInt(KEY_WINS, 0)
        _draws.value = sharedPreferences.getInt(KEY_DRAWS, 0)
        _losses.value = sharedPreferences.getInt(KEY_LOSSES, 0)
        
        Log.d(TAG, "Loaded stats - Wins: ${_wins.value}, Draws: ${_draws.value}, Losses: ${_losses.value}")
    }
    
    // Update win statistic
    suspend fun incrementWins() = withContext(Dispatchers.IO) {
        _wins.value = _wins.value + 1
        saveStats()
        Log.d(TAG, "Incremented wins to: ${_wins.value}")
    }
    
    // Update draw statistic
    suspend fun incrementDraws() = withContext(Dispatchers.IO) {
        _draws.value = _draws.value + 1
        saveStats()
        Log.d(TAG, "Incremented draws to: ${_draws.value}")
    }
    
    // Update loss statistic
    suspend fun incrementLosses() = withContext(Dispatchers.IO) {
        _losses.value = _losses.value + 1
        saveStats()
        Log.d(TAG, "Incremented losses to: ${_losses.value}")
    }
    
    // Reset all statistics
    suspend fun resetStats() = withContext(Dispatchers.IO) {
        _wins.value = 0
        _draws.value = 0
        _losses.value = 0
        saveStats()
        Log.d(TAG, "Reset all statistics")
    }
    
    // Save statistics to SharedPreferences
    private fun saveStats() {
        sharedPreferences.edit().apply {
            putInt(KEY_WINS, _wins.value)
            putInt(KEY_DRAWS, _draws.value)
            putInt(KEY_LOSSES, _losses.value)
            apply()
        }
        Log.d(TAG, "Saved stats to SharedPreferences")
    }
    
    // Calculate win rate percentage
    fun getWinRate(): Int {
        val totalGames = _wins.value + _draws.value + _losses.value
        return if (totalGames > 0) (_wins.value * 100) / totalGames else 0
    }
    
    companion object {
        private const val TAG = "StatsRepository"
        private const val PREF_NAME = "tictoe_stats"
        private const val KEY_WINS = "wins"
        private const val KEY_DRAWS = "draws"
        private const val KEY_LOSSES = "losses"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: StatsRepository? = null
        
        fun getInstance(context: Context): StatsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = StatsRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
} 