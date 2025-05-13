package com.example.tictoe.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tictoe.model.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel : ViewModel() {
    // Current screen being displayed
    private val _currentScreen = MutableStateFlow("menu")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()
    
    // Is the current game vs Bot
    private val _isVsBot = MutableStateFlow(false)
    val isVsBot: StateFlow<Boolean> = _isVsBot.asStateFlow()
    
    // Player's name for online games
    private val _playerName = MutableStateFlow("")
    val playerName: StateFlow<String> = _playerName.asStateFlow()
    
    // Matched player name for online games
    private val _matchedPlayerName = MutableStateFlow("")
    val matchedPlayerName: StateFlow<String> = _matchedPlayerName.asStateFlow()
    
    // SoundManager reference
    private var soundManager: SoundManager? = null
    
    // Set SoundManager
    fun setSoundManager(manager: SoundManager) {
        soundManager = manager
    }
    
    // Navigate to menu screen
    fun navigateToMenu() {
        Log.d("TicToe", "Navigating to menu screen")
        _currentScreen.value = "menu"
        // Start menu music
        soundManager?.startMenuMusic()
    }
    
    // Navigate to settings screen
    fun navigateToSettings() {
        Log.d("TicToe", "Navigating to settings screen")
        _currentScreen.value = "settings"
    }
    
    // Navigate to leaderboard screen
    fun navigateToLeaderboard() {
        Log.d("TicToe", "Navigating to leaderboard screen")
        _currentScreen.value = "leaderboard"
    }
    
    // Navigate to available rooms screen
    fun navigateToAvailableRooms() {
        Log.d("TicToe", "Navigating to available rooms screen")
        _currentScreen.value = "available_rooms"
    }
    
    // Navigate directly to online matching screen (username comes from SettingsViewModel)
    fun navigateToOnlineMatching() {
        Log.d("TicToe", "Navigating to online matching screen")
        _currentScreen.value = "online_matching"
    }
    
    // Set player name for online matchmaking
    fun setPlayerName(name: String) {
        Log.d("TicToe", "Setting player name: $name")
        _playerName.value = name
    }
    
    // Navigate to game screen with Bot
    fun navigateToGameVsBot() {
        Log.d("TicToe", "Navigating to game vs bot")
        _isVsBot.value = true
        _currentScreen.value = "game"
    }
    
    // Navigate to game screen with local Player
    fun navigateToGameVsPlayer() {
        Log.d("TicToe", "Navigating to game vs local player")
        _isVsBot.value = false
        _currentScreen.value = "game"
    }
    
    // Handle when online match is found
    fun onMatchFound(playerName: String) {
        Log.d("TicToe", "Match found with player: $playerName")
        _matchedPlayerName.value = playerName
        _isVsBot.value = false
        _currentScreen.value = "game"
    }
} 