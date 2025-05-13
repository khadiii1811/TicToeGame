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
fun LeaderboardScreen(
    onBack: () -> Unit = {}
) {
    // Màn hình này đơn giản nên không cần ViewModel riêng
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF19104B),
                        Color(0xFF25175B),
                        Color(0xFF2F1F65)
                    )
                )
            )
    ) {
        // Header with back button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFF3D2C6D),
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Leaderboard",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Rest of the leaderboard content
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