package com.example.tictoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.tictoe.view.GameBoardScreen
import com.example.tictoe.view.MenuScreen
import com.example.tictoe.view.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("menu") }
            var gameMode by remember { mutableStateOf("") }
            
            when (currentScreen) {
                "menu" -> {
                    MenuScreen(
                        onSettingsClick = { currentScreen = "settings" },
                        onVsBotClick = { 
                            gameMode = "bot"
                            currentScreen = "game" 
                        },
                        onVsPlayerClick = { 
                            gameMode = "player"
                            currentScreen = "game" 
                        }
                    )
                }
                "settings" -> {
                    SettingsScreen(onBack = { currentScreen = "menu" })
                }
                "game" -> {
                    GameBoardScreen(
                        onBackClick = { currentScreen = "menu" },
                        onPlayAgain = { /* Reset game state if needed */ }
                    )
                }
            }
        }
    }
}