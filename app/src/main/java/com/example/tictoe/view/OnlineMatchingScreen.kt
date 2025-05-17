package com.example.tictoe.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tictoe.model.ConnectionState
import com.example.tictoe.model.GameState
import com.example.tictoe.model.GameStatus
import com.example.tictoe.model.OnlineGameRepository
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect
import com.example.tictoe.ui.theme.PulseEffect
import com.example.tictoe.viewmodel.MenuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Define element data classes outside the composable function
private data class FloatingElementSpec(
    val size: Dp,
    val floatAnimSpec: InfiniteRepeatableSpec<Float>,
    val alphaAnimSpec: InfiniteRepeatableSpec<Float>,
    val offset: Offset,
    val index: Int
)

private data class FloatingElementAnim(
    val size: Dp,
    val floatAnim: Float,
    val alpha: Float,
    val offset: Offset
)

@Composable
fun OnlineMatchingScreen(
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onMatchFound: (String) -> Unit = {},
    viewModel: MenuViewModel? = null,
    repository: OnlineGameRepository? = null
) {
    // Get player name from viewModel if available
    val playerName by viewModel?.playerName?.collectAsState() ?: remember { mutableStateOf("") }
    
    // Observe connection state
    val connectionState by repository?.connectionState?.collectAsState() ?: remember { mutableStateOf<ConnectionState>(ConnectionState.Disconnected) }
    
    // Observe game state
    val gameState by repository?.gameState?.collectAsState() ?: remember { mutableStateOf<GameState?>(null) }
    
    // Status message state
    var statusMessage by remember { mutableStateOf("Searching for opponent...") }
    var hostingMode by remember { mutableStateOf(false) }
    
    // Error dialog state
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Check if connection is in error state
    LaunchedEffect(connectionState) {
        hostingMode = connectionState is ConnectionState.Hosting
        
        statusMessage = when (connectionState) {
            is ConnectionState.Connected -> "Connected to server. Waiting for opponent..."
            is ConnectionState.Connecting -> "Connecting to server..."
            is ConnectionState.Hosting -> "Hosting game. Waiting for opponent to join..."
            is ConnectionState.Error -> {
                val error = (connectionState as ConnectionState.Error).message
                errorMessage = "Error: $error"
                showErrorDialog = true
                "Error: $error"
            }
            is ConnectionState.Reconnecting -> "Reconnecting..."
            else -> "Searching for opponent..."
        }
    }
    
    // Check if opponent found
    LaunchedEffect(gameState) {
        if (gameState != null && gameState?.gameStatus == GameStatus.PLAYING && gameState?.player2?.isNotEmpty() == true) {
            // Found an opponent
            val opponentName = if (gameState?.player1 == playerName) gameState?.player2 else gameState?.player1
            opponentName?.let {
                onMatchFound(it)
            }
        }
    }
    
    // Animation states
    val coroutineScope = rememberCoroutineScope()
    val pulsateAnimation = rememberInfiniteTransition()
    val pulsateScale by pulsateAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = AppColors.MainGradient
            )
    ) {
        // Add glowing background effect
        GlowingBackgroundEffect(modifier = Modifier.fillMaxSize())
        
        // Header with back button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Use the AppHeader component
            AppHeader(
                title = "Online Matching",
                onBackClick = onBack
            )
            
            // Your name indicator
            if (playerName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(size = 48)
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Your Name",
                                color = AppColors.OnSurface.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        
                        Text(
                                text = playerName,
                                color = AppColors.OnSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            }
            
            // Rest of the online matching content
            Spacer(modifier = Modifier.height(48.dp))
            
            // Animated searching indicator with pulsating effect
            PulseEffect(
                pulseFraction = 1.2f,
                durationMillis = 1500
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = AppColors.AccentPurple.copy(alpha = 0.5f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColors.AccentYellow.copy(alpha = 0.3f),
                                    AppColors.AccentPurple.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = AppColors.AccentYellow,
                        strokeWidth = 6.dp
                    )
                    
                    // Add HOST badge for hosting mode
                    if (hostingMode) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-12).dp)
                                .background(
                                    color = AppColors.AccentYellow,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "HOST",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                text = statusMessage,
                color = AppColors.OnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
                    )
                    
            Spacer(modifier = Modifier.height(16.dp))
            
                        Text(
                text = if (hostingMode) 
                    "You've created a room. Waiting for another player to join" 
                else 
                    "Waiting for another player to connect",
                color = AppColors.OnSurface.copy(alpha = 0.7f),
                fontSize = 14.sp
                        )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Cancel button using GradientButton
            GradientButton(
                text = "Cancel Matchmaking",
                onClick = {
                    repository?.disconnect()
                    onCancel()
                },
                    modifier = Modifier
                        .fillMaxWidth()
                    .padding(bottom = 24.dp),
                brush = Brush.linearGradient(
                                    colors = listOf(
                        AppColors.Error.copy(alpha = 0.8f),
                        AppColors.Error
                    )
                ),
                textColor = Color.White
            )
        }
        
        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showErrorDialog = false
                    onCancel()
                },
                title = {
                    Text(
                        text = "Matchmaking Error",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = errorMessage,
                        color = Color.White
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showErrorDialog = false
                            repository?.disconnect()
                            onCancel()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = AppColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Return to Menu")
                    }
                },
                backgroundColor = AppColors.Surface,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnlineMatchingScreenPreview() {
    OnlineMatchingScreen()
} 