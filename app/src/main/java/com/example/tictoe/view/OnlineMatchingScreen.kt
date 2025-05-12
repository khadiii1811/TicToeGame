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
    viewModel: MenuViewModel? = null
) {
    // Get player name from viewModel if available
    val playerName by viewModel?.playerName?.collectAsState() ?: remember { mutableStateOf("") }
    
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
                        }
                    }
                    
            Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                text = "Searching for opponent...",
                color = AppColors.OnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
                    )
                    
            Spacer(modifier = Modifier.height(16.dp))
            
                        Text(
                text = "This may take a few moments",
                color = AppColors.OnSurface.copy(alpha = 0.7f),
                fontSize = 14.sp
                        )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Simulate finding an opponent after a delay
            LaunchedEffect(Unit) {
                delay(5000) // Simulate 5 second delay before finding a match
                val randomOpponent = "Player${Random.nextInt(1, 100)}"
                onMatchFound(randomOpponent)
        }
        
            // Cancel button using GradientButton
            GradientButton(
                text = "Cancel Matchmaking",
                onClick = onCancel,
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
    }
}

@Preview(showBackground = true)
@Composable
fun OnlineMatchingScreenPreview() {
    OnlineMatchingScreen()
} 