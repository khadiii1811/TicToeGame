package com.example.tictoe.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.tictoe.di.AppModule
import com.example.tictoe.model.OnlineGameRepository
import com.example.tictoe.model.SoundManager
import com.example.tictoe.viewmodel.GameViewModel
import com.example.tictoe.viewmodel.LANViewModel
import com.example.tictoe.viewmodel.MenuViewModel
import com.example.tictoe.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    // ViewModels
    private lateinit var menuViewModel: MenuViewModel
    private lateinit var gameViewModel: GameViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var lanViewModel: LANViewModel

    // Repositories
    private lateinit var onlineGameRepository: OnlineGameRepository

    // SoundManager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SoundManager
        soundManager = AppModule.provideSoundManager(applicationContext)

        // Initialize ViewModels
        menuViewModel = AppModule.provideMenuViewModel(this)
        gameViewModel = AppModule.provideGameViewModel(this)
        settingsViewModel = AppModule.provideSettingsViewModel(this)
        lanViewModel = AppModule.provideLANViewModel(this)

        // Initialize OnlineGameRepository
        onlineGameRepository = AppModule.provideOnlineGameRepository(applicationContext)

        // Khởi tạo SettingsViewModel với Context
        settingsViewModel.initialize(applicationContext)

        // Kết nối SoundManager với các ViewModel
        gameViewModel.setSoundManager(soundManager)
        menuViewModel.setSoundManager(soundManager)
        lanViewModel.setSoundManager(soundManager)

        // Load game stats from repository
        val statsRepository = AppModule.provideStatsRepository(applicationContext)

        // Load stats from repository
        gameViewModel.loadStats(wins = statsRepository.getWinRate(), draws = 2, losses = 0)

        Log.d("TicToe", "MainActivity created")

        setContent {
            // Get settings from ViewModel
            val isSoundEnabled by settingsViewModel.isSoundEnabled.collectAsState()
            val aiDifficulty by settingsViewModel.aiDifficulty.collectAsState()
            val username by settingsViewModel.username.collectAsState()

            // Update SoundManager whenever sound settings change
            LaunchedEffect(isSoundEnabled) {
                soundManager.setSoundEnabled(isSoundEnabled)
                Log.d("TicToe", "Sound setting updated: $isSoundEnabled")
            }

            // Update AI difficulty in GameViewModel
            LaunchedEffect(aiDifficulty) {
                gameViewModel.setAiDifficulty(aiDifficulty)
                Log.d("TicToe", "AI difficulty updated: $aiDifficulty")
            }

            // Set username in MenuViewModel whenever it changes in settings
            LaunchedEffect(username) {
                menuViewModel.setPlayerName(username)
                onlineGameRepository.setPlayerName(username)
                lanViewModel.setPlayerName(username)
                Log.d("TicToe", "Username updated to: $username")
            }

            MaterialTheme {
                Surface(
                        color = Color(0xFF19104B),
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxSize()
                ) {
                    // Collect current state from MenuViewModel
                    val currentScreen by menuViewModel.currentScreen.collectAsState()
                    val isVsBot by menuViewModel.isVsBot.collectAsState()

                    // Phát âm thanh khi vào màn hình menu
                    LaunchedEffect(currentScreen) {
                        if (currentScreen == "menu") {
                            soundManager.startMenuMusic()
                            Log.d("TicToe", "Starting menu music when entering menu screen")
                        } else {
                            soundManager.stopMenuMusic()
                        }
                    }

                    Log.d("TicToe", "Current screen: $currentScreen, isVsBot: $isVsBot")

                    when (currentScreen) {
                        "menu" ->
                                MenuScreen(
                                        onSettingsClick = {
                                            soundManager.playClickSound()
                                            menuViewModel.navigateToSettings()
                                        },
                                        onLeaderboardClick = {
                                            soundManager.playClickSound()
                                            menuViewModel.navigateToLeaderboard()
                                        },
                                        onOnlineClick = {
                                            soundManager.playClickSound()
                                            // menuViewModel.navigateToAvailableRooms()
                                            menuViewModel.navigateToRoomMode()
                                        },
                                        onVsBotClick = {
                                            soundManager.playClickSound()
                                            Log.d(
                                                    "TicToe",
                                                    "VS Bot button clicked, switching to game screen"
                                            )
                                            menuViewModel.navigateToGameVsBot()
                                        },
                                        onVsPlayerClick = {
                                            soundManager.playClickSound()
                                            Log.d(
                                                    "TicToe",
                                                    "VS Player button clicked, switching to game screen"
                                            )
                                            menuViewModel.navigateToGameVsPlayer()
                                        },
                                        settingsViewModel = settingsViewModel
                                )
                        "settings" ->
                                SettingsScreen(
                                        onBack = {
                                            soundManager.playClickSound()
                                            menuViewModel.navigateToMenu()
                                        },
                                        viewModel = settingsViewModel
                                )
                        "leaderboard" ->
                                LeaderboardScreen(
                                        onBack = {
                                            soundManager.playClickSound()
                                            menuViewModel.navigateToMenu()
                                        }
                                )
                        "available_rooms" ->
                                AvailableRoomsScreen(
                                        onBack = {
                                            soundManager.playClickSound()
                                            menuViewModel.navigateToMenu()
                                        },
                                        onMatchFound = { opponentName ->
                                            soundManager.playClickSound()
                                            menuViewModel.onMatchFound(opponentName)
                                        },
                                        viewModel = lanViewModel
                                )
                        "create_room" ->
                                CreateRoomScreen(
                                        onBack = {
                                            soundManager.playClickSound()
                                            lanViewModel.disconnect()
                                            menuViewModel.navigateToMenu()
                                        },
                                        onMatchFound = { opponentName ->
                                            soundManager.playClickSound()
                                            menuViewModel.onMatchFound(opponentName)
                                        },
                                        lanViewModel = lanViewModel
                                )
                        // "online_matching" ->
                        //         OnlineMatchingScreen(
                        //                 onBack = {
                        //                     soundManager.playClickSound()
                        //                     menuViewModel.navigateToMenu()
                        //                 },
                        //                 onCancel = {
                        //                     soundManager.playClickSound()
                        //                     menuViewModel.navigateToMenu()
                        //                 },
                        //                 onMatchFound = { opponentName ->
                        //                     soundManager.playClickSound()
                        //                     menuViewModel.onMatchFound(opponentName)
                        //                 },
                        //                 viewModel = menuViewModel,
                        //                 repository = onlineGameRepository
                        //         )
                        "game" -> {
                            Log.d("TicToe", "Displaying game screen with isVsBot=$isVsBot")
                            GameBoardScreen(
                                    onBackClick = {
                                        soundManager.playClickSound()
                                        Log.d("TicToe", "Back button clicked, returning to menu")
                                        menuViewModel.navigateToMenu()
                                        lanViewModel.disconnect()
                                    },
                                    onPlayAgain = {
                                        soundManager.playClickSound()
                                        Log.d("TicToe", "Play again button clicked")
                                        gameViewModel.resetGame()
                                    },
                                    isVsBot = isVsBot,
                                    gameViewModel = gameViewModel,
                                    lanViewModel = lanViewModel
                            )
                        }
                        "room_mode" -> {
                            lanViewModel.setPlayerName(settingsViewModel.username.value)
                            RoomModeScreen(
                                    onHostClick = {
                                        soundManager.playClickSound()
                                        menuViewModel.navigateToCreateRoom()
                                    },
                                    onJoinClick = {
                                        soundManager.playClickSound()
                                        menuViewModel.navigateToAvailableRooms()
                                    },
                                    onBack = {
                                        soundManager.playClickSound()
                                        menuViewModel.navigateToMenu()
                                    }
                            )
                        }
                        else -> {
                            // Fallback to menu if unknown screen
                            Log.w("TicToe", "Unknown screen: $currentScreen, falling back to menu")
                            menuViewModel.navigateToMenu()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources when Activity is destroyed
        if (this::soundManager.isInitialized) {
            soundManager.release()
        }

        // Disconnect from any ongoing online game
        if (this::onlineGameRepository.isInitialized) {
            onlineGameRepository.disconnect()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MenuScreen()
}
