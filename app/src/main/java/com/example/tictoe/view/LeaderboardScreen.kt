package com.example.tictoe.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.ui.theme.AppColors

data class LeaderboardEntry(
    val rank: Int,
    val playerInitial: Char,
    val playerName: String,
    val points: Int,
    val badge: String = "", // 🔥, ⭐, 👑, etc.
    val avatarBackgroundColor: Color = Color(0xFF6344A3)
)

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Global, 1 = Friends
    var searchQuery by remember { mutableStateOf("") }
    
    // Animated shine effects using infiniteTransition
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shine1Position = infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine1"
    )
    
    val shine2Position = infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1500)
        ),
        label = "shine2"
    )
    
    val pulsate = infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsate"
    )
    
    // Dummy data for leaderboard
    val leaderboardEntries = remember {
        listOf(
            LeaderboardEntry(1, 'P', "Player1", 2400, "👑", Color(0xFFE6B325)),
            LeaderboardEntry(2, 'G', "GameMaster", 2150, "", Color(0xFF7C85CE)),
            LeaderboardEntry(3, 'T', "TicTacPro", 1850, "🔥", Color(0xFFE67E22)),
            LeaderboardEntry(4, 'X', "XOChamp", 1720, "💪", Color(0xFF3498DB)),
            LeaderboardEntry(5, 'G', "GridKing", 1650, "⭐", Color(0xFF2ECC71)),
            LeaderboardEntry(6, 'M', "MasterToe", 1550, "", Color(0xFF9B59B6))
        )
    }
    
    // Current user data (for "YOUR RANK" section)
    val currentUser = remember {
        LeaderboardEntry(8, 'P', "Player8", 1350, "", Color(0xFF3498DB))
    }
    
    // Main background gradient colors
    val gradientColors = listOf(Color(0xFF191E4D), Color(0xFF131853), Color(0xFF0E1040))
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset((-50).dp, (-50).dp)
                .alpha(0.2f)
                .background(
                    Color(0xFF33BDDB),
                    CircleShape
                )
                .blur(40.dp)
        )
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(50.dp, 50.dp)
                .alpha(0.15f)
                .background(
                    Color(0xFF8B5CF6),
                    CircleShape
                )
                .blur(60.dp)
        )
        
        // Additional light beam effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = shine1Position.value.dp - 150.dp, y = 100.dp)
                .rotate(45f)
                .alpha(0.1f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 0f)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = shine2Position.value.dp - 150.dp, y = 400.dp)
                .rotate(-30f)
                .alpha(0.08f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0xFF33BDDB), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 0f)
                    )
                )
        )
        
        // Floating particles
        for (i in 0 until 10) {
            val xOffset = remember { (0..400).random().dp }
            val yOffset = remember { (0..800).random().dp }
            val particleSize = remember { (2..6).random().dp }
            val animatedAlpha = infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween((1000..3000).random(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle$i"
            )
            
            Box(
                modifier = Modifier
                    .size(particleSize)
                    .offset(x = xOffset, y = yOffset)
                    .alpha(animatedAlpha.value)
                    .background(
                        Color.White,
                        CircleShape
                    )
            )
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back button with glow
                Box {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .offset((-3).dp, (-3).dp)
                            .alpha(0.4f)
                            .blur(12.dp)
                            .background(Color(0xFF8B5CF6), CircleShape)
                    )
                    
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(10.dp, CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, Float.POSITIVE_INFINITY)
                                ),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Title with shine effect
                Box {
                    // Light beam animation across text
                    val textShinePosition = infiniteTransition.animateFloat(
                        initialValue = -50f,
                        targetValue = 250f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing, delayMillis = 1000),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "textshine"
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(40.dp)
                            .offset(x = textShinePosition.value.dp - 150.dp)
                            .rotate(15f)
                            .alpha(0.3f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                                    start = Offset(0f, 0f),
                                    end = Offset(300f, 0f)
                                )
                            )
                    )
                    
                    Text(
                        text = "Leaderboard",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Tabs with animation (Global/Friends)
            Card(
                shape = RoundedCornerShape(28.dp),
                backgroundColor = Color(0xFF2D3062).copy(alpha = 0.7f),
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Shine effect across card
                Box {
                    if (selectedTab == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .offset(x = shine1Position.value.dp - 150.dp)
                                .rotate(0f)
                                .alpha(0.15f)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                                        start = Offset(0f, 0f),
                                        end = Offset(300f, 0f)
                                    )
                                )
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        TabButton(
                            text = "Global",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        
                        TabButton(
                            text = "Friends",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Search box with glow
            Box {
                // Subtle glow effect
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(0.dp, 0.dp)
                            .alpha(0.3f)
                            .blur(16.dp)
                            .background(Color(0xFF8B5CF6), RoundedCornerShape(24.dp))
                    )
                }
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xFF2D3062).copy(alpha = 0.7f),
                    elevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Animated search icon
                            val iconScale = infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (searchQuery.isEmpty()) 1.1f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "searchIconScale"
                            )
                            
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(iconScale.value)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search players...",
                                                color = Color.Gray.copy(alpha = 0.7f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Top Players heading with glow
            Box {
                // Subtle pulsating glow
                val topPlayersGlow = infiniteTransition.animateFloat(
                    initialValue = 0.0f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "topPlayersGlow"
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        // Star icon glow
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .offset((-2).dp, (-2).dp)
                                .alpha(topPlayersGlow.value)
                                .blur(8.dp)
                                .background(Color(0xFFFFC107), CircleShape)
                        )
                        
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier
                                .size(26.dp)
                                .scale(pulsate.value)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "TOP PLAYERS",
                        color = Color(0xFF8B5CF6),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "POINTS",
                        color = Color(0xFF8B5CF6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Leaderboard list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(leaderboardEntries) { entry ->
                    LeaderboardItemCard(entry)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Current user rank
            Card(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF2D3062).copy(alpha = 0.7f),
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Card shine effect animation
                val infiniteTransition = rememberInfiniteTransition(label = "userCardShine")
                val shinePosition = infiniteTransition.animateFloat(
                    initialValue = -100f,
                    targetValue = 500f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shine"
                )
                
                Box {
                    // Animated light beam
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .offset(x = shinePosition.value.dp - 250.dp)
                            .alpha(0.15f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                                    start = Offset(0f, 0f),
                                    end = Offset(500f, 0f)
                                )
                            )
                    )
                    
                    // Subtle glow around the card
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(8.dp)
                            .alpha(0.2f)
                            .blur(16.dp)
                            .background(Color(0xFF8B5CF6), RoundedCornerShape(20.dp))
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "YOUR RANK",
                            color = Color.Gray.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Rank number with glow
                            Box {
                                // Glow effect
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .offset((-3).dp, (-3).dp)
                                        .alpha(0.4f)
                                        .blur(8.dp)
                                        .background(Color(0xFF6366F1), CircleShape)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(0xFF6366F1), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${currentUser.rank}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // User avatar with glow effect
                            Box {
                                // Pulsating glow
                                val avatarPulsate = infiniteTransition.animateFloat(
                                    initialValue = 0.0f,
                                    targetValue = 0.5f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "avatarGlow"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .offset((-3).dp, (-3).dp)
                                        .alpha(avatarPulsate.value)
                                        .blur(12.dp)
                                        .background(Color(0xFF8B5CF6), CircleShape)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .shadow(4.dp, CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
                                                start = Offset(0f, 0f),
                                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Shine effect inside avatar
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                                                    center = Offset(10f, 10f),
                                                    radius = 22f
                                                ),
                                                CircleShape
                                            )
                                    )
                                    
                                    Text(
                                        text = currentUser.playerInitial.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = currentUser.playerName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Points with glow
                            Box {
                                // Glow effect
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .offset(0.dp, 0.dp)
                                        .alpha(0.4f)
                                        .blur(12.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
                                                start = Offset(0f, 0f),
                                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
                                                start = Offset(0f, 0f),
                                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                                            ),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    // Shine effect
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                                                    start = Offset(0f, 0f),
                                                    end = Offset(80f, 0f)
                                                )
                                            )
                                    )
                                    
                                    Text(
                                        text = "${currentUser.points} pts",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated shine effect
    val infiniteTransition = rememberInfiniteTransition(label = "tabshine")
    val shinePosition = infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(if (isSelected) 0 else 750)
        ),
        label = "shine"
    )
    
    Box(
        modifier = modifier
    ) {
        // Glow effect for selected tab
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(0.dp, 0.dp)
                    .alpha(0.3f)
                    .blur(12.dp)
                    .background(Color(0xFF8B5CF6), RoundedCornerShape(24.dp))
            )
        }
        
        // Content with shine effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Shine animation
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .offset(x = shinePosition.value.dp - 100.dp)
                        .alpha(0.3f)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(200f, 0f)
                            )
                        )
                )
            }
            
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.7f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LeaderboardItemCard(entry: LeaderboardEntry) {
    // Add animated shine effect for top 3 ranks
    val infiniteTransition = rememberInfiniteTransition(label = "cardshine")
    val shinePosition = infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 250f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset((entry.rank * 500) % 1500)
        ),
        label = "shine"
    )
    
    val scale = if (entry.rank <= 3) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { derivedStateOf { 1f } }
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = when (entry.rank) {
            1 -> Color(0xFF2D3062).copy(alpha = 0.9f)
            2 -> Color(0xFF2D3062).copy(alpha = 0.8f)
            3 -> Color(0xFF2D3062).copy(alpha = 0.7f)
            else -> Color(0xFF2D3062).copy(alpha = 0.5f)
        },
        elevation = if (entry.rank <= 3) 8.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Shine line animation for top ranks
            if (entry.rank <= 3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .offset(x = shinePosition.value.dp - 150.dp)
                        .rotate(20f)
                        .alpha(0.2f)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(300f, 0f)
                            )
                        )
                )
            }
            
            // Regular content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                // Rank circle with special styling for top 3
                Box {
                    // Pulsating effect for top 3
                    val pulsate = if (entry.rank <= 3) {
                        infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulsate"
                        )
                    } else {
                        remember { derivedStateOf { 1f } }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(if (entry.rank <= 3) pulsate.value else 1f)
                            .background(
                                brush = when (entry.rank) {
                                    1 -> Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFC107)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                    2 -> Brush.linearGradient(
                                        colors = listOf(Color(0xFFC0C0C0), Color(0xFFE0E0E0)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                    3 -> Brush.linearGradient(
                                        colors = listOf(Color(0xFFCD7F32), Color(0xFFD2B48C)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                    else -> Brush.linearGradient(
                                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                    )
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.rank.toString(),
                            color = if (entry.rank <= 3) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    // Add shine effect for top 3
                    if (entry.rank <= 3) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                                        center = Offset(10f, 10f),
                                        radius = 20f
                                    ),
                                    CircleShape
                                )
                        )
                    }
                    
                    // Add glow effect for top 3
                    if (entry.rank <= 3) {
                        val glowColor = when(entry.rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> Color.Transparent
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .offset((-6).dp, (-6).dp)
                                .alpha(0.5f)
                                .blur(8.dp)
                                .background(
                                    glowColor,
                                    CircleShape
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Player avatar with badge and light effects
                Box {
                    // Glow behind avatar for top 3
                    if (entry.rank <= 3) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .offset((-4).dp, (-4).dp)
                                .alpha(0.4f)
                                .blur(12.dp)
                                .background(entry.avatarBackgroundColor, CircleShape)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        entry.avatarBackgroundColor,
                                        entry.avatarBackgroundColor.copy(alpha = 0.7f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.playerInitial.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                    
                    if (entry.badge.isNotEmpty()) {
                        val badgePulsate = infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "badgePulsate"
                        )
                        
                        Box(
                            modifier = Modifier
                                .offset(x = 30.dp, y = (-8).dp)
                                .size(26.dp)
                                .scale(badgePulsate.value)
                                .shadow(8.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFF8B5CF6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entry.badge,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Player name
                Text(
                    text = entry.playerName,
                    color = Color.White,
                    fontSize = if (entry.rank <= 3) 18.sp else 16.sp,
                    fontWeight = if (entry.rank <= 3) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                // Points with special styling and glow
                Box {
                    // Glow effect for top ranks
                    if (entry.rank <= 3) {
                        val glowColor = when(entry.rank) {
                            1 -> Color(0xFFFFD700).copy(alpha = 0.4f)
                            2 -> Color(0xFFC0C0C0).copy(alpha = 0.4f)
                            3 -> Color(0xFFCD7F32).copy(alpha = 0.4f)
                            else -> Color.Transparent
                        }
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(0.dp, 0.dp)
                                .alpha(0.4f)
                                .blur(8.dp)
                                .background(
                                    glowColor,
                                    RoundedCornerShape(16.dp)
                                )
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                brush = when (entry.rank) {
                                    1 -> Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.2f), Color(0xFFFFC107).copy(alpha = 0.2f)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                                    )
                                    2 -> Brush.linearGradient(
                                        colors = listOf(Color(0xFFC0C0C0).copy(alpha = 0.2f), Color(0xFFE0E0E0).copy(alpha = 0.2f)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                                    )
                                    3 -> Brush.linearGradient(
                                        colors = listOf(Color(0xFFCD7F32).copy(alpha = 0.2f), Color(0xFFD2B48C).copy(alpha = 0.2f)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                                    )
                                    else -> Brush.linearGradient(
                                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.2f), Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                                    )
                                },
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${entry.points} pts",
                            color = when (entry.rank) {
                                1 -> Color(0xFFFFD700)
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color(0xFF8B5CF6)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = if (entry.rank <= 3) 16.sp else 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenPreview() {
    LeaderboardScreen()
} 