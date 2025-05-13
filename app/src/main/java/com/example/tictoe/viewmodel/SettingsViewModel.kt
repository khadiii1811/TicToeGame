package com.example.tictoe.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private var sharedPreferences: SharedPreferences? = null
    
    // Sound settings
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()
    
    // Vibration settings
    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()
    
    // Difficulty level for AI (1-Easy, 2-Medium, 3-Hard)
    private val _aiDifficulty = MutableStateFlow(2)
    val aiDifficulty: StateFlow<Int> = _aiDifficulty.asStateFlow()
    
    // Username for online play
    private val _username = MutableStateFlow("Player")
    val username: StateFlow<String> = _username.asStateFlow()
    
    // Khởi tạo SharedPreferences
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadSettings()
    }
    
    // Toggle sound setting
    fun toggleSound() {
        _isSoundEnabled.value = !_isSoundEnabled.value
        Log.d("TicToe", "Sound toggled: ${_isSoundEnabled.value}")
        saveSettings()
    }
    
    // Toggle vibration setting
    fun toggleVibration() {
        _isVibrationEnabled.value = !_isVibrationEnabled.value
        Log.d("TicToe", "Vibration toggled: ${_isVibrationEnabled.value}")
        saveSettings()
    }
    
    // Set AI difficulty level
    fun setAiDifficulty(level: Int) {
        if (level in 1..3) {
            _aiDifficulty.value = level
            Log.d("TicToe", "AI difficulty set to: $level")
            saveSettings()
        }
    }
    
    // Set username
    fun setUsername(name: String) {
        if (name.isNotBlank()) {
            _username.value = name
            Log.d("TicToe", "Username set to: $name")
            saveSettings()
        }
    }
    
    // Save settings to persistent storage
    fun saveSettings() {
        sharedPreferences?.edit()?.apply {
            putBoolean(KEY_SOUND, _isSoundEnabled.value)
            putBoolean(KEY_VIBRATION, _isVibrationEnabled.value)
            putInt(KEY_AI_DIFFICULTY, _aiDifficulty.value)
            putString(KEY_USERNAME, _username.value)
            apply()
        }
        Log.d("TicToe", "Settings saved to SharedPreferences")
    }
    
    // Load settings from persistent storage
    fun loadSettings() {
        sharedPreferences?.let { prefs ->
            _isSoundEnabled.value = prefs.getBoolean(KEY_SOUND, true)
            _isVibrationEnabled.value = prefs.getBoolean(KEY_VIBRATION, true)
            _aiDifficulty.value = prefs.getInt(KEY_AI_DIFFICULTY, 2)
            _username.value = prefs.getString(KEY_USERNAME, "Player") ?: "Player"
            
            Log.d("TicToe", "Settings loaded from SharedPreferences")
            Log.d("TicToe", "Sound: ${_isSoundEnabled.value}, Vibration: ${_isVibrationEnabled.value}")
            Log.d("TicToe", "AI Difficulty: ${_aiDifficulty.value}")
            Log.d("TicToe", "Username: ${_username.value}")
        }
    }
    
    companion object {
        private const val PREF_NAME = "tictoe_settings"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_AI_DIFFICULTY = "ai_difficulty"
        private const val KEY_USERNAME = "username"
    }
} 