package com.example.tictoe.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("menu") }
            var matchedPlayerName by remember { mutableStateOf("") }
            
            when (currentScreen) {
                "menu" -> MenuScreen(
                    onSettingsClick = { currentScreen = "settings" },
                    onLeaderboardClick = { currentScreen = "leaderboard" },
                    onOnlineClick = { currentScreen = "online_matching" }
                )
                "settings" -> SettingsScreen(onBack = { currentScreen = "menu" })
                "leaderboard" -> LeaderboardScreen(onBack = { currentScreen = "menu" })
                "online_matching" -> OnlineMatchingScreen(
                    onBack = { currentScreen = "menu" },
                    onCancel = { currentScreen = "menu" },
                    onMatchFound = { playerName ->
                        matchedPlayerName = playerName
                        currentScreen = "game" // You would transition to your game screen here
                    }
                )
                "game" -> {
                    // Replace this with your actual game screen when ready
                    // For now just go back to menu when clicked
                    MenuScreen(onSettingsClick = { currentScreen = "menu" })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MenuScreen()
} 