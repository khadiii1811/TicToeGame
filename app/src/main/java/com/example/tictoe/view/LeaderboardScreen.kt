package com.example.tictoe.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LeaderboardPlayer(
    val id: Int,
    val name: String,
    val initial: Char,
    val points: Int,
    val color: Color,
    val badge: String = ""
)

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    // State for tab selection (Global or Friends)
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Sample data for the leaderboard
    val players = remember {
        listOf(
            LeaderboardPlayer(1, "Player1", 'P', 2400, Color(0xFFFFD700), "🏆"), // Gold
            LeaderboardPlayer(2, "GameMaster", 'G', 2150, Color(0xFFC0C0C0), "🥈"), // Silver
            LeaderboardPlayer(3, "TicTacPro", 'T', 1850, Color(0xFFCD7F32), "🥉"), // Bronze
            LeaderboardPlayer(4, "XOChamp", 'X', 1720, Color(0xFF9370DB), "🔥"),
            LeaderboardPlayer(5, "GridKing", 'G', 1650, Color(0xFF20B2AA), "⭐"),
            LeaderboardPlayer(6, "MoveExpert", 'M', 1580, Color(0xFF6495ED)),
            LeaderboardPlayer(7, "TicMaster", 'T', 1490, Color(0xFFFF6347)),
            LeaderboardPlayer(8, "Player8", 'P', 1350, Color(0xFF4169E1)),
            LeaderboardPlayer(9, "GameWizard", 'G', 1220, Color(0xFF32CD32)),
            LeaderboardPlayer(10, "BoardMaster", 'B', 1150, Color(0xFFFF69B4))
        )
    }
    
    // Current user's rank
    val currentUserRank = 8
    val currentUserPoints = 1350

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E124D),
                        Color(0xFF2D1863),
                        Color(0xFF3A256A)
                    )
                )
            )
    ) {
        // Background decoration - top left
        Box(
            modifier = Modifier
                .size(180.dp)
                .alpha(0.12f)
                .offset(x = (-60).dp, y = (-60).dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .align(Alignment.TopStart)
        )
        
        // Background decoration - bottom right
        Box(
            modifier = Modifier
                .size(220.dp)
                .alpha(0.10f)
                .offset(x = 80.dp, y = 80.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .align(Alignment.BottomEnd)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(8.dp, CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4E3A8C),
                                        Color(0xFF7B6FC6)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Tic Tac Toe",
                        color = Color(0xFFFFD600),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = Shadow(
                                color = Color(0xFF000000).copy(alpha = 0.25f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 4.dp,
                    backgroundColor = Color(0xFF4E3A8C).copy(alpha = 0.8f),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "Leaderboard",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tabs
            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = 8.dp,
                backgroundColor = Color(0xFF2F1F65),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(4.dp)
                ) {
                    val tabs = listOf("Global", "Friends")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val tabWidth by animateDpAsState(
                            targetValue = if (isSelected) 8.dp else 0.dp,
                            animationSpec = tween(300)
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(
                                    brush = if (isSelected) {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF4EE6FA),
                                                Color(0xFF4EE6FA).copy(alpha = 0.7f)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF4E3A8C).copy(alpha = 0.5f),
                                                Color(0xFF4E3A8C).copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = tabWidth,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF).copy(alpha = 0.8f),
                                            Color(0xFFFFFFFF).copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF2F1F65) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search bar
            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = 8.dp,
                backgroundColor = Color(0xFF2F1F65).copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF4EE6FA),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search players...",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title for the leaderboard section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "TOP PLAYERS",
                    color = Color(0xFF4EE6FA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "POINTS",
                    color = Color(0xFF4EE6FA),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Leaderboard entries
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                backgroundColor = Color(0xFF332058).copy(alpha = 0.8f),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(players.take(10)) { index, player ->
                        LeaderboardItem(
                            rank = index + 1,
                            player = player,
                            isCurrentUser = player.id == 8  // Assuming Player8 is the current user
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bottom bar with current user's rank
            Card(
                shape = RoundedCornerShape(50.dp),
                backgroundColor = Color(0xFF2F1F65),
                elevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(50.dp),
                        spotColor = Color(0xFF4EE6FA).copy(alpha = 0.2f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4E3A8C),
                                        Color(0xFF4169E1)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOUR RANK",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = Color(0xFF4E3A8C),
                                elevation = 0.dp,
                                modifier = Modifier.height(18.dp)
                            ) {
                                Text(
                                    text = "${currentUserRank}th",
                                    color = Color(0xFF4EE6FA),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "Player8",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Points with glowing effect
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color(0xFF4EE6FA)
                            )
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4E3A8C),
                                        Color(0xFF4169E1)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currentUserPoints",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFF4EE6FA).copy(alpha = 0.5f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 8f
                                )
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "pts",
                        color = Color(0xFF4EE6FA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    player: LeaderboardPlayer,
    isCurrentUser: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (rank <= 3) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val shadowColor = if (rank <= 3) {
        when(rank) {
            1 -> Color(0xFFFFD700).copy(alpha = 0.5f) // Gold
            2 -> Color(0xFFC0C0C0).copy(alpha = 0.5f) // Silver
            3 -> Color(0xFFCD7F32).copy(alpha = 0.5f) // Bronze
            else -> Color(0xFF4EE6FA).copy(alpha = 0.2f)
        }
    } else if (isCurrentUser) {
        Color(0xFF4EE6FA).copy(alpha = 0.3f)
    } else {
        Color(0xFF4EE6FA).copy(alpha = 0.1f)
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isCurrentUser) {
            Color(0xFF4E3A8C)
        } else if (rank <= 3) {
            Color(0xFF332058).copy(alpha = 0.95f)
        } else {
            Color(0xFF332058).copy(alpha = 0.7f)
        },
        elevation = if (rank <= 3 || isCurrentUser) 8.dp else 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .shadow(
                elevation = if (rank <= 3) 12.dp else if (isCurrentUser) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = shadowColor
            )
            .graphicsLayer {
                if (rank <= 3) {
                    scaleX = scale
                    scaleY = scale
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(4.dp, CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = when (rank) {
                                1 -> listOf(Color(0xFFFFD700), Color(0xFFFFC000)) // Gold
                                2 -> listOf(Color(0xFFC0C0C0), Color(0xFFE0E0E0)) // Silver
                                3 -> listOf(Color(0xFFCD7F32), Color(0xFFDDA15E)) // Bronze
                                else -> listOf(Color(0xFF4E3A8C), Color(0xFF7B6FC6))
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = when (rank) {
                        1, 2, 3 -> Color(0xFF2D1863)
                        else -> Color.White
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Player initial with badge
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4E3A8C),
                                    player.color.copy(alpha = 0.7f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, player.color.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.initial.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                // Badge for top players
                if (player.badge.isNotEmpty()) {
                    Text(
                        text = player.badge,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .offset(x = 24.dp, y = (-4).dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Player name
            Text(
                text = player.name,
                color = Color.White,
                fontWeight = if (isCurrentUser || rank <= 3) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            
            // Points
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = if (rank <= 3) 8.dp else 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = if (rank <= 3) player.color.copy(alpha = 0.5f) else Color(0xFF4EE6FA).copy(alpha = 0.2f)
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (rank <= 3) {
                                listOf(
                                    Color(0xFF332058),
                                    player.color.copy(alpha = 0.3f)
                                )
                            } else {
                                listOf(
                                    Color(0xFF332058),
                                    Color(0xFF4E3A8C)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${player.points}",
                        color = if (rank <= 3) player.color else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.width(2.dp))
                    
                    Text(
                        text = "pts",
                        color = if (rank <= 3) player.color.copy(alpha = 0.7f) else Color(0xFF4EE6FA),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenPreview() {
    LeaderboardScreen(onBack = {})
} 